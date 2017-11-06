import java.lang

import org.graphstream.algorithm.{BetweennessCentrality, PageRank}
import org.graphstream.algorithm.generator.{BarabasiAlbertGenerator, Generator}
import org.graphstream.algorithm.measure.DegreeCentrality
import org.graphstream.graph.{Graph, Node}
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file._

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
  * Created by José Manuel Jiménez Ávila on 26/06/2017.
  */
object GraphStreamSmall {

  def main(args: Array[String]): Unit = {
    val graph: Graph = new SingleGraph("Random")
    val generator: Generator = new BarabasiAlbertGenerator()

    generator.addSink(graph)
    generator.begin()

    for(i <- 1 until 5000) {
      generator.nextEvents()
    }

    generator.end()

    val bcb: BetweennessCentrality = new BetweennessCentrality
    val bd: DegreeCentrality = new DegreeCentrality()
    bcb.init(graph)
    bcb.compute()

    val pageRank: PageRank = new PageRank
    pageRank.init(graph)
    pageRank.compute()

    val iterator = graph.getEachNode[Node].iterator()

    val orderedNodes = orderNodes(toArray[Node](graph.getEachNode[Node].asScala.iterator), "PageRank")
    println("Ordered nodes: " + orderedNodes.length)

    /*
    while(iterator.hasNext) {
      val node: Node = iterator.next()
      println(node.getId + "->" + node.getAttribute("Cb") + ", " + node.getAttribute("PageRank"))
    }
    */

    for(node <- orderedNodes) {
      println(node.getId + "->" + node.getAttribute("Cb") + ", " + node.getAttribute("PageRank"))
    }

    val fileSink: FileSink = new FileSinkGML

    fileSink.writeAll(graph, "graph.gml")

    graph.display()
  }

  def orderNodes(nodesArray: Array[Node], attribute: String): ArrayBuffer[Node] = {
    val array = ArrayBuffer[Node]()

    def order(node: Node, nodes: Array[Node]): ArrayBuffer[Node] = {
      array += node
      if(nodes.length > 0) {
        val gNode = greaterNode(nodes.head, nodes.tail)
        order(gNode, nodes.filter(_.getId != gNode.getId))
      } else {
        array
      }

    }

    def greaterNode(node: Node, nodes: Array[Node]): Node = {
      if(nodes.length > 0) {
        val head: Node = nodes.head
        if (node.getAttribute[Double](attribute) > head.getAttribute[Double](attribute)) {
          greaterNode(node, nodes.tail)
        } else {
          greaterNode(head, nodes.tail)
        }
      } else {
        node
      }
    }

    if(nodesArray.length > 0) {
      val gNode = greaterNode(nodesArray.head, nodesArray.tail)
      order(gNode, nodesArray.filter(_.getId != gNode.getId))
    } else {
      array
    }

  }

  def toArray[T](iterator: Iterator[T])(implicit c: ClassTag[T]): Array[T] = {
    var array = Array[T]()

    while(iterator.hasNext) {
      array = array :+ iterator.next()
    }

    array
  }

  implicit class CompareNodes(n: Node) {

    def greater(than: Node): Node = {

      if(n.getAttribute[Double]("PageRank") > than.getAttribute[Double]("PageRank")) {
        n
      } else {
        than
      }
    }
  }
}