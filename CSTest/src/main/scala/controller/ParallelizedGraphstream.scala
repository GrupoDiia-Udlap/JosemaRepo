package controller

import java.io.{BufferedWriter, File, FileWriter}

import model.Interaccion
import org.apache.spark.{SparkContext, graphx}
import org.apache.spark.graphx.{Graph, VertexId, VertexRDD}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.graphstream.algorithm.measure.ClosenessCentrality
import org.graphstream.algorithm.{APSP, BetweennessCentrality, Eccentricity, PageRank}
import org.graphstream.graph.Node
import org.graphstream.graph.Edge
import org.graphstream.graph.implementations.SingleGraph

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by José Manuel Jiménez Ávila on 05/09/2017.
  */
object ParallelizedGraphstream extends App{
  import org.apache.log4j.Logger
  import org.apache.log4j.Level
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  ////////////////////////////////////////////////////////////////////////////////////
  //Cassandra
  //Spark context and cassandra cluster connection
  val cassandra: CassandraUtil = new CassandraUtil("local[*]",
    "SQL Cassandra Spark!",
    ("spark.cassandra.connection.host", "127.0.0.1"),
    ("spark.cassandra.auth.username", "cassandra"),
    ("spark.cassandra.auth.password", "cassandra"))

  val spark: SparkSession = cassandra.sparkSession
  val sc: SparkContext = cassandra.sparkContext

  //Create keyspace and table
  cassandra.executeOrders("CREATE KEYSPACE IF NOT EXISTS diia WITH replication = {'class':'SimpleStrategy'," +
    " 'replication_factor':1};",
    "CREATE TABLE IF NOT EXISTS diia.Interacciones ( idInteraccion text PRIMARY KEY, idOrigen text, tipoOrigen " +
      "text, idDestino text, tipoDestino text, tipoInteraccion text, valorInteraccion text, idPrecedente text, " +
      "idCurso text, fecha text, plataforma text );",
    "CREATE TABLE IF NOT EXISTS diia.Nodos ( idnodo text PRIMARY KEY, nodeAttrs map<text, text> );",
    "TRUNCATE TABLE diia.Nodos;")

  //Extracting data from Cassandra into RDD
  val data: RDD[Interaccion] = cassandra.readDataFromCassandra("diia", "interacciones")

  //Grouping by 'curso'
  val grouped: RDD[(String, Iterable[Interaccion])] = data.groupBy(
    (interaccion: Interaccion) => interaccion.idcurso)

  val graphxGraphs: Array[(String, Graph[Double, Interaccion])] = grouped.collect().map(
    curso => {
      val edges: RDD[graphx.Edge[Interaccion]] = sc.parallelize(curso._2.map(
        interaccion => {
          graphx.Edge(interaccion.idorigen.toLong, interaccion.iddestino.toLong, interaccion)
        }
      ).toSeq)

      (curso._1, Graph.fromEdges(edges,0.0))
    })


  //For each 'curso' do...
  val graphstreamGraphs: RDD[SingleGraph] = grouped.map(
    (curso: (String, Iterable[Interaccion])) => {
    //Graph creation
    val graph: SingleGraph = new SingleGraph(curso._1)
    graph.setStrict(false)
    graph.setAutoCreate(true)

    curso._2.foreach(
      interaccion =>  {
        graph.addEdge[Edge](interaccion.idinteraccion, interaccion.idorigen, interaccion.iddestino)
      })
    graph
  })

  ////////////////////////////////////////////////////////////////////////////////////
  //SQL Spark (Postgresql)
  import spark.implicits._

  val alumnoDF: DataFrame = spark.read
    .format("jdbc")
    .option("url", "jdbc:postgresql://localhost/diia?user=postgres&password=12345678")
    .option("dbtable", "alumno")
    .option("driver","org.postgresql.Driver")
    .load()

  var graphstreamArray: Array[SingleGraph] = Array()

  val computedGraphstream: RDD[SingleGraph] = graphstreamGraphs.map(
    (graph: SingleGraph) => {
      //Betweenness centrality
      val betweennessCentrality: BetweennessCentrality = new BetweennessCentrality()
      betweennessCentrality.init(graph)
      betweennessCentrality.compute()

      //Pagerank
      val pagerank: PageRank = new PageRank()
      pagerank.init(graph)
      pagerank.compute()

      //All Pair Shortest Path
      val apsp: APSP = new APSP()
      apsp.init(graph)
      apsp.setDirected(true)
      apsp.compute()

      //Eccentricity
      val eccentricity: Eccentricity = new Eccentricity()
      eccentricity.init(graph)
      eccentricity.compute()

      //CLoseness
      val closeness: ClosenessCentrality = new ClosenessCentrality()
      closeness.init(graph)
      closeness.compute()

      graphstreamArray +:= graph

      graph
    }
  )

  val computedGraphx: Array[(String, Graph[Double, Interaccion])] = graphxGraphs.map(
    idgraph => {
      val pagerank = idgraph._2.pageRank(tol = 1.0e-5, resetProb = 0.85).vertices
      (idgraph._1, idgraph._2.joinVertices(pagerank)(
        (id: VertexId, default: Double, pr: Double) => pr
      ))
    }
  )

  val graphtoCSV = computedGraphx.head
  val optionalGraph: RDD[SingleGraph] = computedGraphstream.filter(sg => sg.getId.equalsIgnoreCase(graphtoCSV._1))
  val extracted: (String, Array[(String, String)]) = optionalGraph.map(sg => {
    val returnSeq = sg.getNodeSet[Node].asScala.toArray.map(node => (node.getId, node.getAttribute[Double]("PageRank").toString))
    (sg.getId, returnSeq)
  }).collect().head
  val nodeArray: Array[Seq[String]] = graphtoCSV._2.vertices.map(f => {
    val nodeId = f._1.toString
    val pageRankGraphx = f._2.toString
    val pageRankGraphS = extracted._2.find(p => p._1.equalsIgnoreCase(nodeId)).get._2

    //Id, label, graphx PR, graphstream PR
    Seq(nodeId, nodeId, pageRankGraphx, pageRankGraphS)
  }).collect()

  val edgeArray: Array[Seq[String]] = graphtoCSV._2.edges.collect().map(edge => {
    Seq(edge.srcId.toString, edge.dstId.toString, "directed", edge.attr.idinteraccion)
  })

  nodesEdgesCSVCreator(Set("Id", "Label", "PRGraphX", "PRGraphS"), nodeArray, edgeArray,graphtoCSV._1+"_")


  /*
  println("GraphX ////////////////////////////////////////////////////////////////////////////////////////////////////")
  computedGraphx.foreach(idgraph => {
    println(s"Curso ${idgraph._1}: ${idgraph._2.numVertices} nodos.")

    idgraph._2.vertices.sortBy(_._2.toDouble, ascending = false).take(10).foreach(
      tupla => {
        println(s"-->Nodo ${tupla._1} -> pagerank: ${tupla._2}")
      }
    )
  })

  println("Graphstream ///////////////////////////////////////////////////////////////////////////////////////////////")
  computedGraphstream.foreach(graph => {
    println(s"Curso ${graph.getId}: ${graph.getNodeCount} nodos.")

    graph.getNodeSet[Node]
      .asScala.toArray.sortBy(-_.getAttribute[Double]("PageRank")).take(10).foreach(
      (node: Node) => {
        println(s"-->Nodo ${node.getId} -> pagerank: ${node.getAttribute("PageRank")}")
      }
    )
  })
  */

  cassandra.session.getCluster.close()
  spark.sparkContext.stop()

  def nodesEdgesCSVCreator(nodesLabels: Set[String], nodes: Array[Seq[String]], edges: Array[Seq[String]],
                           filePrefix: String = "", sep: String = ","):Unit = {

    def newLineWriter(bw: BufferedWriter, seq: Seq[String]): Unit = {
      bw.newLine()
      bw.write(seq.mkString(sep))
    }
    val nodesFile = new File("nodes.csv")
    val nodesBw = new BufferedWriter(new FileWriter(nodesFile))

    val edgesFile = new File("edges.csv")
    val edgesBw = new BufferedWriter(new FileWriter(edgesFile))

    nodesBw.write(nodesLabels.mkString(sep))
    nodes.foreach( seq => newLineWriter(nodesBw, seq))
    nodesBw.close()

    edgesBw.write("Source,Target,Type,Id".replace(",", sep))
    edges.foreach(seq => newLineWriter(edgesBw, seq))
    edgesBw.close()

  }
}
