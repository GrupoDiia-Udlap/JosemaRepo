import java.util.Date

import models.tables.{Interaccion, Nodo}
import org.apache.spark.sql.SparkSession
import com.datastax.spark.connector._
import org.graphstream.algorithm.PageRank
import org.graphstream.graph.{Edge, Node}
import org.graphstream.graph.implementations.SingleGraph

import scala.collection.JavaConverters._

/**
  * Class to calculate PageRank given the parameters:
  * *context: all the students or students and the teacher (students OR studentsteacher)
  * *course: ID of the course (usually an integer)
  * *normalized: normalize values? (norm)
  *  *dates: date1 < date2, format ISO 8061 (ex. 2017-11-18)
  * Example: degree context course date1 date2 [normalized]
  *         pagerankcalc students 5 2015-10-10 2015-10-17
  *         pagerankcalc studensteacher 2 2015-10-10 2015-10-17 norm
  */

object PageRankCalc extends CalculoI {

  def main(args: Array[String]): Unit = {

    parameters = Array("students", "1", "2005-01-01", "2010-12-31")
    /**
      *
      */

    println(parameters(0) + " " + parameters(1) + "----------------------------------------------_----------------------------")

    val interactions: Array[Interaccion] = sparkSession.sparkContext.
      cassandraTable[Interaccion]("diia", "interacciones").where("idcurso = ?", parameters(1)).collect()
    //.where("fecha >= ?", parameters(2)).where("fecha <= ?", parameters(3)).collect()
    /*
    .filter(interaction => {
      if (parameters(0).equalsIgnoreCase("students")) {
        interaction.tipoorigen.equalsIgnoreCase("alumno") && interaction.tipodestino.equalsIgnoreCase("docente")
      } else if (parameters(0).equalsIgnoreCase("studentsteacher")) {
        (interaction.tipoorigen.equalsIgnoreCase("alumno") || interaction.tipoorigen.equalsIgnoreCase("docente")) &&
          (interaction.tipodestino.equalsIgnoreCase("alumno") || interaction.tipodestino.equalsIgnoreCase("docente"))
      } else {
        throw new IllegalArgumentException(s"The parameter isn't correct ('${parameters(0)}').")
      }
    })
    */

    val graph: SingleGraph = generateGraph(parameters(1), interactions)

    //PageRank
    val pagerank: PageRank = new PageRank()
    pagerank.init(graph)
    pagerank.compute()

    println(createJson(graph, "PageRank"))
  }

  var parameters: Array[String] = Array()

  val sparkSession: SparkSession = SparkSession.builder()
    .master("local[*]")
    .appName("Graphstream Spark")
    .config("spark.cassandra.connection.host", "127.0.0.1")
    .config("spark.cassandra.auth.username", "cassandra")
    .getOrCreate()

  /**
    * Method to set the job parameters
    * @param args all the arguments of the current job
    */
  override def setParameters(args: Array[String]): Unit = {

    try {
      if(args.length < 4 || args.length > 5) {
        throw new IllegalArgumentException("Usage: PageRankCalc context course initdate enddate [norm]")
      } else parameters = args

      val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
      val date1: Date = format.parse(parameters(2))
      val date2: Date = format.parse(parameters(3))

      if (1 == date1.compareTo(date2)) throw new IllegalArgumentException("The first date is greater than the second.")

    } catch {
      case e: Exception => e.printStackTrace()
        System.exit(1)
    }

  }

  /**
    * Method to run the job
    */
  override def run(): Unit = {
    /**
      *
      */
    val sparkSession: SparkSession = SparkSession.builder()
      .master("local[*]")
      .appName("Graphstream Spark")
      .config("spark.cassandra.connection.host", "127.0.0.1")
      .config("spark.cassandra.auth.username", "cassandra")
      .getOrCreate()

    println(parameters(0) + " " + parameters(1) + "----------------------------------------------_----------------------------")

    val interactions: Array[Interaccion] = sparkSession.sparkContext.
      cassandraTable[Interaccion]("diia", "interacciones").where("idcurso = ?", parameters(1)).collect()
      //.where("fecha >= ?", parameters(2)).where("fecha <= ?", parameters(3)).collect()
      /*
      .filter(interaction => {
        if (parameters(0).equalsIgnoreCase("students")) {
          interaction.tipoorigen.equalsIgnoreCase("alumno") && interaction.tipodestino.equalsIgnoreCase("docente")
        } else if (parameters(0).equalsIgnoreCase("studentsteacher")) {
          (interaction.tipoorigen.equalsIgnoreCase("alumno") || interaction.tipoorigen.equalsIgnoreCase("docente")) &&
            (interaction.tipodestino.equalsIgnoreCase("alumno") || interaction.tipodestino.equalsIgnoreCase("docente"))
        } else {
          throw new IllegalArgumentException(s"The parameter isn't correct ('${parameters(0)}').")
        }
      })
      */

    val graph: SingleGraph = generateGraph(parameters(1), interactions)

    //PageRank
    val pagerank: PageRank = new PageRank()
    pagerank.init(graph)
    pagerank.compute()

    println(createJson(graph, "PageRank"))

    sparkSession.sparkContext.stop()
    sparkSession.stop()
  }

  /**
    * Method to create a SingleGraph
    * @param course The course to create the graph
    * @param interactions The interactions to generate the graph
    * @return a SingleGraph object
    */
  def generateGraph(course: String, interactions: Array[Interaccion]): SingleGraph = {
    val graph: SingleGraph = new SingleGraph(course)
    graph.setStrict(false)
    graph.setAutoCreate(true)

    interactions.foreach(interaction => {
      graph.addEdge[Edge](interaction.idinteraccion, interaction.idorigen, interaction.iddestino)
    })

    graph
  }

  /**
    * Method that creates a JSON string given a SingleGraph object
    * @param graph the graph to be converted to format JSON
    * @param mainAttr the main attribute to search for in each node (pagerank in this case)
    * @return a JSON string representing all the nodes of the graph and the main centrality computed
    */
  def createJson(graph: SingleGraph, mainAttr: String): String = {
    val nodes: Iterable[Node] = graph.getNodeSet[Node].asScala

    def helper(iterable: Iterable[Node], actualNode: Node, json: String): String = {
      val newJson = json + s"""|{
                               |  "id": "${actualNode.getId}",
                               |  "$mainAttr": ${actualNode.getAttribute[Double](mainAttr)}
                               |}""".stripMargin
      if(iterable.nonEmpty) {
        helper(iterable.tail, iterable.head, newJson+",")
      } else {
        newJson
      }
    }

    val start = s""""|{  "course": "${graph.getId}",
                   |   "nodes": [""".stripMargin

    val end = """|   ]
                 |}""".stripMargin

    if(nodes.nonEmpty) {
      helper(nodes.tail, nodes.head, start) + end
    } else {
      start + end
    }
  }

  def normalize(array: Array[Double]) : Array[Double] = {

    array.map( x => {
      (x - array.min) / (array.max - array.min)
    })

  }


}
