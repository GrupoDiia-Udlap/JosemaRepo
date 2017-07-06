import org.graphstream.algorithm.BetweennessCentrality
import org.graphstream.algorithm.generator.{Generator, PreferentialAttachmentGenerator}
import org.graphstream.graph.Graph
import org.graphstream.graph.implementations.SingleGraph
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by José Manuel Jiménez Ávila on 26/06/2017.
  */

@RunWith(classOf[JUnitRunner])
class GraphStreamTest extends FunSuite {

  test("Generate random graph") {
    val graph: Graph = new SingleGraph("Random")
    val generator: Generator = new PreferentialAttachmentGenerator()

    generator.addSink(graph)
    generator.begin()

    for(i <- 1 to 30) {
      generator.nextEvents()
    }

    generator.end()

    val bcb = new BetweennessCentrality
    bcb.init(graph)
    bcb.compute()


    graph.display()

  }


}
