package controller

import model.Interaccion
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Graph, VertexId, VertexRDD}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.graphstream.algorithm.measure.ClosenessCentrality
import org.graphstream.algorithm.{APSP, BetweennessCentrality, Eccentricity, PageRank}
import org.graphstream.graph.Node
import org.graphstream.graph.Edge
import org.graphstream.graph.implementations.SingleGraph

import scala.collection.JavaConverters._

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

  //For each 'curso' do...
  val parallelizedGrouped: RDD[SingleGraph] = grouped.map(
    (curso: (String, Iterable[Interaccion])) => {
    //Graph creation
    val graph: SingleGraph = new SingleGraph(curso._1)
    graph.setStrict(false)
    graph.setAutoCreate(true)

    curso._2.foreach(
      interaccion =>  {
        graph.addEdge[Edge](interaccion.idinteraccion, interaccion.idorigen, interaccion.iddestino)
      }
    )

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

  //var graphs: Array[SingleGraph] = Array()

  val graphs = parallelizedGrouped.map(
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

      graph
    }
  )

  //val graph: SingleGraph = graphs.head

  graphs.foreach(graph => {
    println(s"Curso ${graph.getId}: ${graph.getNodeCount} nodos.")

    graph.getNodeSet[Node]
      .asScala.toArray.sortBy(-_.getAttribute[Double]("PageRank")).take(10).foreach(
      (node: Node) => {
        println(s"-->Nodo ${node.getId} -> pagerank: ${node.getAttribute("PageRank")}")
      }
    )
  })

  /*
  println(s"Curso ${graph.getId}: ${graph.getNodeCount} nodos.")

  graph.getNodeSet[Node]
    .asScala.toArray.sortBy(-_.getAttribute[Double]("PageRank")).take(10).foreach(
      (node: Node) => {
        println(s"<->Nodo ${node.getId} -> pagerank: ${node.getAttribute("PageRank")}")
      }
    )
  val rankedGraph: Graph[(String, Double), Interaccion] = graphs.head._2

  println(s"Curso ${graphs.head._1}: ${rankedGraph.vertices.count()} vértices y ${rankedGraph.edges.count()} aristas...")
  rankedGraph.vertices.sortBy(_._2._2, ascending = false).collect().foreach(
    vertice => {
      println(s" id: ${vertice._1} | tipo: ${vertice._2._1} | pagerank: ${vertice._2._2}")
    }
  )

  rankedGraph.vertices.sortBy(_._2._2, ascending = false).collect().take(15).foreach(
    vertice => {
      val rows = alumnoDF.filter(row => {
        row.getAs[String]("id_alumno").equalsIgnoreCase(vertice._1.toString)
      })
      rows.foreach(firstRow => {
        println(s"${vertice._1}: pagerank ${vertice._2._2}, ${firstRow.getAs("nombre")} ${firstRow.getAs("apellido")}")
      })
    })

  //some.collect().foreach( row => println(row.toString()))
  */
  cassandra.session.getCluster.close()
  spark.sparkContext.stop()
}
