package model

import com.datastax.spark.connector.mapper.DefaultColumnMapper

/**
  * Created by José Manuel Jiménez Ávila on 08/08/2017.
  */

class Interaccion extends Serializable {
  //Column mappings
  var idinteraccion: String = _
  var idorigen: String = _
  var tipoorigen: String = _
  var iddestino: String = _
  var tipodestino: String = _
  var tipointeraccion: String = _
  var valorinteraccion: String = _
  var idprecedente: String = _
  var idcurso: String = _
  var fecha: String = _
  var plataforma: String = _

  def this(idinteraccion: String, idorigen: String, tipoorigen: String, iddestino: String,
           tipodestino: String, tipointeraccion: String, valorinteraccion: String,
           idprecedente: String, idcurso: String, fecha: String, plataforma: String){
    this
    this.idinteraccion = idinteraccion
    this.idorigen = idorigen
    this.tipoorigen = tipoorigen
    this.iddestino = iddestino
    this.tipodestino = tipodestino
    this.tipointeraccion = tipointeraccion
    this.valorinteraccion = valorinteraccion
    this.idprecedente = idprecedente
    this.idcurso = idcurso
    this.fecha = fecha
    this.plataforma = plataforma
  }

  /*
  //Getters
  def idinteraccion: String = _idinteraccion
  def idorigen: String = _idorigen
  def tipoorigen: String = _tipoorigen
  def iddestino: String = _iddestino
  def tipodestino: String = _tipodestino
  def tipointeraccion: String = _tipointeraccion
  def valorinteraccion: String = _valorinteraccion
  def idprecedente: String = _idprecedente
  def idcurso: String = _idcurso
  def fecha: String = _fecha
  def plataforma: String = _plataforma

  //Setters
  def idinteraccion_= (value: String):Unit = _idinteraccion = value
  def idorigen_= (value: String):Unit = _idorigen = value
  def tipoorigen_= (value: String):Unit = _tipoorigen = value
  def iddestino_= (value: String):Unit = _iddestino = value
  def tipodestino_= (value: String):Unit = _tipodestino = value
  def tipointeraccion_= (value: String):Unit = _tipointeraccion = value
  def valorinteraccion_= (value: String):Unit = _valorinteraccion = value
  def idprecedente_= (value: String):Unit = _idprecedente = value
  def idcurso_= (value: String):Unit = _idcurso = value
  def fecha_= (value: String):Unit = _fecha = value
  def plataforma_= (value: String):Unit = _plataforma = value

  */

}

object Interaccion {
  /*implicit object Mapper extends DefaultColumnMapper[Interaccion](
    Map("idinteraccion_=" -> "idinteraccion", "idorigen_=" -> "idorigen",
    "tipoorigen_=" -> "tipoorigen", "iddestino_=" -> "iddestino",
    "tipodestino_=" -> "tipodestino", "tipointeraccion_=" -> "tipointeraccion",
    "valorinteraccion_=" -> "valorinteraccion", "idpredecente_=" -> "idpredecente",
    "idcurso_=" -> "idcurso", "fecha_=" -> "fecha", "plataforma_=" -> "plataforma"))*/
  implicit object Mapper extends DefaultColumnMapper[Interaccion]
}
