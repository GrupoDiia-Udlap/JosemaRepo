package controller

import org.graphstream.graph.implementations.SingleGraph

/**
  * Created by José Manuel Jiménez Ávila on 15/08/2017.
  */
abstract class GraphUtil {

  def createSingleGraph(name: String): SingleGraph = {
    val sg: SingleGraph = new SingleGraph(name)
    sg.setStrict(false)
    sg.setAutoCreate(true)
    sg
  }


}
