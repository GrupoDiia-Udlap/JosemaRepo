package controller

/**
  * Created by José Manuel Jiménez Ávila on 29/08/2017.
  */

import model.Interaccion
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Edge, Graph, VertexId, VertexRDD}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object PostgresqlConnection extends App {

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
  val grouped: Array[(String, Iterable[Interaccion])] = data.groupBy(
    (interaccion: Interaccion) => interaccion.idcurso).collect

  //Here we are going to save the graphs for each 'curso'
  var graphs: Array[(String, Graph[(String, Double), Interaccion])] = Array()

  //For each 'curso' do...
  grouped.foreach((curso: (String, Iterable[Interaccion])) => {
    //Source
    val origenes: Seq[(VertexId, String)] = curso._2
      .groupBy(
        interaccion => (interaccion.idorigen, interaccion.tipoorigen)
      ).toSeq
      .map {
        f => (f._1._1.toLong, f._1._2)
      }

    //Destination
    val destinos: Seq[(VertexId, String)] = curso._2
      .groupBy(
        interaccion => (interaccion.iddestino, interaccion.tipodestino)
      ).toSeq
      .map {
        f => (f._1._1.toLong, f._1._2)
      }

    //(Source+destination).distinct = nodes
    val nodos: RDD[(VertexId, String)] = sc.parallelize(origenes.union(destinos).distinct)

    //Edges
    val aristas: RDD[Edge[Interaccion]] = sc.parallelize(curso._2.map(
      interaccion => Edge(
        interaccion.idorigen.toLong, interaccion.iddestino.toLong, interaccion)).toSeq)

    //Graph creation
    val graph: Graph[String, Interaccion] = Graph(nodos, aristas)

    //Printing graph
    println(s"Curso ${curso._1} -> ${graph.numVertices} vértices -> ${graph.numEdges} aristas")

    //PageRank
    val pagerank: VertexRDD[Double] = graph.pageRank(0.001).vertices
    val rankedNodes: RDD[(VertexId, (String, Double))] = graph.vertices.join(pagerank)

    val rankedGraph: Graph[(String, Double), Interaccion] = Graph(rankedNodes, graph.edges)

    //Saving graph to array
    graphs +:= (curso._1, rankedGraph)

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

  alumnoDF.printSchema()

  println("Rows: " + alumnoDF.count())

  //alumnoDF.collect().foreach(row => println(row.toString()))

  /*
  val alumnoCSV: DataFrame = spark.read.format("CSV")
    .option("header", "true")
    .load("src/main/resources/alumnos.csv")

  alumnoCSV.printSchema()

  alumnoCSV.write
    .format("jdbc")
    .option("url", "jdbc:postgresql://localhost/diia?user=postgres&password=12345678")
    .option("dbtable", "alumno")
    .option("driver","org.postgresql.Driver")
    .mode(SaveMode.Append)
    .save()
  */
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

  cassandra.session.getCluster.close()
  spark.sparkContext.stop()
}
