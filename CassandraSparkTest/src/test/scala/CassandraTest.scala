/**
  * Created by José Manuel Jiménez Ávila on 19/06/2017.
  */

import org.scalatest.FunSuite


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CassandraTest extends FunSuite {

  test("Cassandra local connection") {
    import com.datastax.driver.core.Cluster
    import com.datastax.driver.core.Session
    //Query

    val keySpaceQuery = "CREATE KEYSPACE IF NOT EXISTS diia WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};"
    val tableQuery = "CREATE TABLE IF NOT EXISTS diia.Interacciones" +
      " ( idInteraccion text PRIMARY KEY, idOrigen text, tipoOrigen text," +
      " idDestino text, tipoDestino text, tipoInteraccion text, valorInteraccion text," +
      " idPrecedente text, idCurso text, fecha text, plataforma text );"

    //creating Cluster object
    val cluster = Cluster.builder.addContactPoint("127.0.0.1").build

    //Creating Session object
    val session = cluster.connect

    //Executing the query
    session.execute(keySpaceQuery)
    println("Keyspace created")

    session.execute(tableQuery)
    println("Table created")

    //using the KeySpace
    session.execute("USE diia")
  }
}
