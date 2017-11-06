package services

import java.util
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, Props}
import models.tables.Interaccion
import org.apache.spark.SparkContext
import play.api.inject.ApplicationLifecycle
import play.api.Logger
import services.SparkCassandraUtil.{CountCourse, PageRankCentrality}
import com.datastax.spark.connector._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.graphstream.algorithm.PageRank
import org.graphstream.graph.{Edge, Node}
import org.graphstream.graph.implementations.SingleGraph
import play.api.libs.concurrent.InjectedActorSupport
import scala.collection.JavaConverters._

/**
  * This [[Actor]] would handle all the requests to spark, pool for spark jobs (?).
  *
  * @param appLifecycle life cycle of the application, useful for logging
  * @param configuration configuration parameters from `application.conf`
  */


class SparkCassandraUtil @Inject() (
  appLifecycle: ApplicationLifecycle,
  configuration: play.api.Configuration) extends Actor with InjectedActorSupport {

  /**
    * Spark necessary values to connect to Cassandra,
    * defined at application.conf
    */
  val cassandraHost = "spark.cassandra.connection.host"
  val cassandraUser = "spark.cassandra.auth.username"
  val cassandraPass = "spark.cassandra.auth.password"

  val sparkSession: SparkSession = SparkSession.builder()
    .master(configuration.underlying.getString("spark.master"))
    .appName(configuration.underlying.getString("spark.appname"))
    .config(cassandraHost, configuration.underlying.getString(cassandraHost))
    .config(cassandraUser, configuration.underlying.getString(cassandraUser))
    .getOrCreate()

  /**
    * Method to execute before starting the Actor, in this case for logging purposes
    */
  override def preStart(): Unit = {
    Logger.info("SparkCassandraUtil Started")
  }

  def getInteracciones(where: String, values: Any*): RDD[Interaccion] = {
    sparkSession.sparkContext.cassandraTable[Interaccion](configuration.underlying.getString("cassandra.keyspace"),
      configuration.underlying.getString("cassandra.interacciones")).where(where, values)
  }

  def getInteractionsArray(where: String, values: Any*): Array[Interaccion] = {
    getInteracciones(where, values).collect()
  }

  override def receive: PartialFunction[Any, Unit] = {
    case PageRankCentrality("alumno", "todo", course) =>
      //val interactions = getInteractionsArray("idcurso = ? and tipoorigen = ?", course, "alumno")
      val interactions = sparkSession.sparkContext.
        cassandraTable[Interaccion](configuration.underlying.getString("cassandra.keyspace"),
        configuration.underlying.getString("cassandra.interacciones")).where("idcurso = ?", course)
        .where("tipoorigen = ?", "alumno").collect()
      val graph = generateGraph(course, interactions)
      val pagerank: PageRank = new PageRank()
      pagerank.init(graph)
      pagerank.compute()
      sender() ! createJson(graph, "PageRank")
    /*
    case "spark-config" => sender() ! configuration.underlying.getString("spark.master")
    case CountCourse(course: String) =>
      val countResult: Long = sparkSession.sparkContext.cassandraTable(configuration.underlying.getString("cassandra.keyspace"),
        configuration.underlying.getString("cassandra.interacciones"))
        .select("idcurso").where("idcurso = ?", course).cassandraCount()
      sender() ! s"The group with id $course got $countResult interactions."
    */
  }

  def generateGraph(course: String, interactions: Array[Interaccion]): SingleGraph = {
    val graph: SingleGraph = new SingleGraph(course)
    graph.setStrict(false)
    graph.setAutoCreate(true)

    interactions.foreach(interaction => {
      graph.addEdge[Edge](interaction.idinteraccion, interaction.idorigen, interaction.iddestino)
    })

    graph
  }

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


}

object SparkCassandraUtil {

  case class CountCourse(course: String)
  case class PageRankCentrality(from: String, to: String, course: String)

}


