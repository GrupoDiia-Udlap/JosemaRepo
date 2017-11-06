package controllers

import javax.inject.{Inject, Named, Singleton}

import akka.actor._
import play.api.mvc._
import akka.pattern.ask
import akka.util.Timeout
import org.apache.spark.sql.SparkSession
import services.SparkCassandraUtil.{CountCourse, PageRankCentrality}
import com.datastax.spark.connector._
import models.Directional

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * This controller is in charge of receiving requests of centrality computations
  * (Pagerank, betweenness, eccentricity, closeness)
  * @param cc default controller components
  */

@Singleton
class CentralityController @Inject()(@Named("spark-actor") sparkactor: ActorRef,
                                     cc: ControllerComponents, configuration: play.api.Configuration)
                                    (implicit ec: ExecutionContext)
  extends AbstractController(cc)  {

  val cassandraHost = "spark.cassandra.connection.host"
  val cassandraUser = "spark.cassandra.auth.username"
  val cassandraPass = "spark.cassandra.auth.password"

  lazy val sparkSession: SparkSession = SparkSession.builder()
    .master(configuration.underlying.getString("spark.master"))
    .appName(configuration.underlying.getString("spark.appname"))
    .config(cassandraHost, configuration.underlying.getString(cassandraHost))
    .config(cassandraUser, configuration.underlying.getString(cassandraUser))
    .getOrCreate()

  def pagerank = Action {
    Ok
  }

  def betweenness = Action {
    Ok
  }

  implicit val timeout: Timeout = 1.minute
  def actorTest: Action[AnyContent] = Action.async {
    (sparkactor ? "spark-config").mapTo[String].map{ message =>
      Ok(message)
    }
  }

  def count(curso: String): Action[AnyContent] = Action {
    val countresult: Long = sparkSession.sparkContext.cassandraTable(configuration.underlying.getString("cassandra.keyspace"),
      configuration.underlying.getString("cassandra.interacciones"))
      .select("idcurso").where("idcurso = ?", curso).cassandraCount()
    Ok(s"The group with id $curso got $countresult interactions.")
  }

  def countAsync(curso: String): Action[AnyContent] = Action.async {
    (sparkactor ? CountCourse(curso)).mapTo[String].map {
      message => Ok(message)
    }
  }

  def handle(centrality: String, from: String, to: String, course: String): Action[AnyContent] = Action.async {
    (sparkactor ? PageRankCentrality(from, to, course)).mapTo[String].map {
      message => Ok(message).as("application/json")
    }

  }


}
