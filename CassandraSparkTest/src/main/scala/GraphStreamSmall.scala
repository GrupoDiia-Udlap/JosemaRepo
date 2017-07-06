import java.lang

import org.graphstream.algorithm.{BetweennessCentrality, PageRank}
import org.graphstream.algorithm.generator.{BarabasiAlbertGenerator, Generator}
import org.graphstream.graph.{Graph, Node}
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.file._

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
    bcb.init(graph)
    bcb.compute()

    val pageRank: PageRank = new PageRank
    pageRank.init(graph)
    pageRank.compute()

    val iterator = graph.getEachNode[Node].iterator()

    while(iterator.hasNext) {
      val node: Node = iterator.next()
      println(node.getId + "->" + node.getAttribute("Cb") + ", " + node.getAttribute("PageRank"))
    }

    val fileSink: FileSink = new FileSinkGML

    fileSink.writeAll(graph, "graph.gml")

    graph.display()
  }
}
