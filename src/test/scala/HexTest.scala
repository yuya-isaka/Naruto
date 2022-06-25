package naruto

import chisel3._
import org.scalatest.flatspec._
import chiseltest._
import java.io._

// ２つのトレイトをテストクラスに継承
class HexTest extends AnyFlatSpec with ChiselScalatestTester {
  "Naruto RISC-V" should "work through hex" in {

    println(
      "\n-------------------------- start ------------------------------\n"
    )

    // バックエンドをVerilator
    // test(new TopDevelop(new RV32ConfigDebug)).withAnnotations(Seq(VerilatorBackendAnnotation)) {
    test(new TopDevelop(new RV32ConfigDebug)) { c =>
      println("")
      while (!c.io.exit.peekBoolean()) {
        c.clock.step(1)
      }
    }

    println(
      "\n---------------------------- end ------------------------------\n"
    )

  }
}
