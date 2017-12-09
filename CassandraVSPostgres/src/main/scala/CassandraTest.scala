import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import com.datastax.spark.connector._
import org.apache.spark.sql.cassandra._

/**
  * Object to test reading interactions from CSV file with Apache Spark to later store them
  * in a Cassandra table or PostgreSQL table
  */
object CassandraTest extends App {

  import org.apache.log4j.Logger
  import org.apache.log4j.Level
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  ////////////////////////////////////////////////////////////////////////////////////
  //Cassandra
  //Spark context and cassandra cluster connection
  //Here we specifies the connection parameters with spark, including the connection with the Cassandra cluster
  //(connection host, username and password)
  val connection: CassandraUtil = new CassandraUtil(
    "local[*]",
    "SQL Spark!",
    ("spark.cassandra.connection.host", "127.0.0.1"),
    ("spark.cassandra.auth.username", "cassandra"),
    ("spark.cassandra.auth.password", "cassandra"))

  val spark: SparkSession = connection.sparkSession
  val sc: SparkContext = connection.sparkContext


  try {
    // Reading the interactions from CSV file, specifying the format (csv), if the CSV file has a header
    // (true in this case) and the path to the file.
    val interactions: DataFrame = spark.read.format("csv").option("header", "true").load("src/main/res/interacciones.csv")

    // This line prints the DataFrame schema, deducted by the CSV file headers.
    interactions.printSchema()

    // Write into Cassandra format the DataFrame, in the keyspace 'diia' and the table 'interacciones'
    // Uncomment if need to test Cassandra
    interactions.write.cassandraFormat("interacciones", "diia").save()

    // Write interactions DataFrame in format jdbc, specifying the url, the table and the driver used,
    // besides the savemode.
    // Uncomment if need to test PostgreSQL

    /*
    interactions.write
      .format("jdbc")
      .option("url", "jdbc:postgresql://localhost/diia?user=postgres&password=12345678")
      .option("dbtable", "interacciones")
      .option("driver","org.postgresql.Driver")
      .mode(SaveMode.Append)
      .save()
    */

  } catch {
    case e: Exception => e.printStackTrace()
  } finally {
    //Finally close connection with Cassandra cluster and Spark context
    connection.session.getCluster.close()
    spark.sparkContext.stop()
  }

}
