package model

import com.datastax.spark.connector.mapper.DefaultColumnMapper

/**
  * Created by José Manuel Jiménez Ávila on 24/08/2017.
  */
class Nodo extends Serializable {

  var idnodo: String = _
  //var date: Date = _
  var nodeattrs: Map[String, String] = Map()


  def this(idnodo: String, graphAttrs: (String, String)*) {
    this
    this.idnodo = idnodo
    //this.date = date
    this.nodeattrs = graphAttrs.toMap
  }

}

object Nodo {
  implicit object Mapper extends DefaultColumnMapper[Nodo]
}
