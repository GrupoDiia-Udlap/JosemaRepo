package controller

import com.datastax.driver.core.Session
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnector
import com.datastax.spark.connector.rdd.CassandraTableScanRDD
import model.Interaccion
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * Created by José Manuel Jiménez Ávila on 07/08/2017.
  */
class CassandraUtil(master: String, appName: String, configuration: (String, String)*) {

  val sparkSession: SparkSession = {
    val sparkSession: SparkSession.Builder = SparkSession.builder()
      .master(master)
      .appName(appName)

    for(i <- configuration) {
      sparkSession.config(i._1,i._2)
    }

    sparkSession.getOrCreate()
  }
  //Private spark context
  lazy val sparkContext: SparkContext = sparkSession.sparkContext


  def readDataFromCassandra(keyspace: String, table: String): RDD[Interaccion] = sparkContext.cassandraTable[Interaccion](keyspace, table)
  //def readDataFromCassandra(keyspace: String, table: String): RDD[CassandraRow] = sparkContext.cassandraTable(keyspace, table)

  def executeOrders(orders: String*): Unit = {
    try{
      orders.foreach(order => session.execute(order))
      session.close()
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  val session: Session = {
    val connector: CassandraConnector = CassandraConnector.apply(sparkContext.getConf)
    connector.openSession()
  }

  /*
  def createSparkContext(master: String, appName: String, configuration: Map[String, String]): SparkContext ={
    val sparkSession: SparkSession.Builder = SparkSession.builder()
      .master(master)
      .appName(appName)

    for(i <- configuration) {
      sparkSession.config(i._1,i._2)
    }

    sparkSession.getOrCreate().sparkContext
  }
  */

}
