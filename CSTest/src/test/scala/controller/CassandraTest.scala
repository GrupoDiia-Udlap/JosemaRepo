package controller

/**
  * Created by José Manuel Jiménez Ávila on 08/08/2017.
  */

import java.io.{BufferedWriter, File, FileWriter}

import model.{Interaccion, Nodo}
import org.apache.spark.SparkContext
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import com.datastax.spark.connector._
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CassandraTest extends FunSuite {

  test("Cassandra successful connection?") {
    // Logger settings
    import org.apache.log4j.Logger
    import org.apache.log4j.Level
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    // Spark context and cassandra cluster connection
    val cassandra: CassandraUtil = new CassandraUtil("local[*]",
      "CassandraTest",
      ("spark.cassandra.connection.host", "127.0.0.1"),
        ("spark.cassandra.auth.username", "cassandra"),
        ("spark.cassandra.auth.password", "cassandra"))

    val sc: SparkContext = cassandra.sparkContext

    //Create keyspace and table
    cassandra.executeOrders("CREATE KEYSPACE IF NOT EXISTS diia WITH replication = {'class':'SimpleStrategy'," +
      " 'replication_factor':1};",
    "CREATE TABLE IF NOT EXISTS diia.Interacciones ( idInteraccion text PRIMARY KEY, idOrigen text, tipoOrigen " +
      "text, idDestino text, tipoDestino text, tipoInteraccion text, valorInteraccion text, idPrecedente text, " +
      "idCurso text, fecha text, plataforma text );",
    "CREATE TABLE IF NOT EXISTS diia.Nodos ( idnodo text PRIMARY KEY, nodeAttrs map<text, text> );",
    "TRUNCATE TABLE diia.Nodos;")

    // Extracting data from Cassandra into RDD
    val data: RDD[Interaccion] = cassandra.readDataFromCassandra("diia", "interacciones")

    // Grouping by 'curso'
    val grouped: Array[(String, Iterable[Interaccion])] = data.groupBy(
      (interaccion: Interaccion) => interaccion.idcurso).collect

    var graphs: Array[(String, Graph[String, Interaccion])] = Array()

    // For each 'curso' do...
    grouped.foreach((curso: (String, Iterable[Interaccion])) => {
      // Source
      val origenes: Seq[(VertexId, (String, String))] = curso._2.groupBy(
        interaccion => (interaccion.idorigen, interaccion.tipoorigen)).
        toSeq.map(f => (f._1._1.toLong, f._1))

      // Destination
      val destinos: Seq[(VertexId, (String, String))] = curso._2
        .groupBy(
          interaccion => (interaccion.iddestino, interaccion.tipodestino)
        ).toSeq
        .map {
          f => (f._1._1.toLong, f._1)
        }

      // (Source+destination).distinct = nodes
      val nodos: RDD[(VertexId, (String, String))] = sc.parallelize(origenes.union(destinos).distinct)

      // Edges
      val aristas: RDD[Edge[Interaccion]] = sc.parallelize(curso._2.map(
        interaccion => Edge(
          interaccion.idorigen.toLong, interaccion.iddestino.toLong, interaccion)).toSeq)

      // Graph creation
      val graph: Graph[(String, String), Interaccion] = Graph(nodos, aristas)

      //val graph: Graph[String, Interaccion] = Graph.fromEdges(aristas, defaultValue = "1")

      //graphs +:= (curso._1, graph)

      println(s"Grupo ${curso._1} -> ${graph.numVertices} vértices -> ${graph.numEdges} aristas")


      // Triplets
      /*
      graph.triplets.map(
        triplet =>
          s"  ${triplet.srcAttr} ${triplet.attr.tipoorigen} ->  ${triplet.dstAttr} ${triplet.attr.tipodestino}").
        collect().take(5).foreach(println(_))
      */


      // PageRank
      /*
      val pagerank: VertexRDD[Double] = graph.pageRank(0.85).vertices

      pagerank.join(graph.vertices).sortBy(_._2._1, ascending = false).take(5).foreach(
        x => println(s" ${x._1} -> ${x._2._1}")
      )

      // Joining results
      val holi: RDD[(VertexId, (String, Double))] = graph.vertices.join(pagerank)

      */
      /*
      Graph(holi, aristas).triplets.
        sortBy(_.srcAttr._2, ascending = false).
        map[String](triplet => s"${triplet.srcAttr._1}: ${triplet.srcAttr._2}")

      //triplets.saveAsTextFile("Triplets")

      */


    })

    /*
    // Modifying all Interaccion objects
    val some: RDD[Interaccion] = data.map(interaccion => {
      if(interaccion.tipoorigen == "null") {
        // Probably 'curso' material
        interaccion.tipoorigen = "material"
      } else if(interaccion.tipodestino == "null") {
        interaccion.tipodestino = "material"
      }
      interaccion
    })

    // Saving to Cassandra
    some.saveToCassandra("diia","interacciones")
    */

    val graphToSave = graphs.head

    // Saving new table in Cassandra
    val pageranked: VertexRDD[Double] = graphToSave._2.pageRank(0.085).vertices
    val indegrees: VertexRDD[PartitionID] = graphToSave._2.inDegrees
    val outdegrees: VertexRDD[PartitionID] = graphToSave._2.outDegrees

    val joined: RDD[(VertexId, (Double, (PartitionID, PartitionID)))] = pageranked.join(indegrees.join(outdegrees))

    val algo: RDD[Nodo] = joined.map(f => {
      new Nodo(f._1.toString, ("pagerank", f._2._1.toString), ("indegree", f._2._2._1.toString), ("outdegree", f._2._2._2.toString))
    })

    algo.saveToCassandra("diia", "nodos")

    /*
    // Saving graph to textfile
    val nodesFile = new File(graphToSave._1+"_nodes.csv")
    val nodesBw = new BufferedWriter(new FileWriter(nodesFile))

    val edgesFile = new File(graphToSave._1+"_edges.csv")
    val edgesBw = new BufferedWriter(new FileWriter(edgesFile))

    nodesBw.write("Id,Label")
    graphToSave._2.vertices.collect().foreach(
      node => {
        nodesBw.newLine()
        nodesBw.write(s"${node._1.toInt + 1},${node._2.toInt + 1}")
      })
    nodesBw.close()

    edgesBw.write("Source,Target,Type,Id")
    graphToSave._2.edges.collect().foreach(
      edge => {
        edgesBw.newLine()
        edgesBw.write(s"${edge.srcId.toInt + 1},${edge.dstId.toInt + 1},directed,${edge.attr.idinteraccion}")
      }
    )
    edgesBw.close()

    */

    // Closing cassandra cluster and spark context
    cassandra.session.getCluster.close()
    cassandra.sparkContext.stop()

  }
}
