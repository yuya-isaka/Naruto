package naruto

import chisel3._
import chisel3.util._

// -----------------------------------------------------

class Nedge extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val pulse = Output(Bool())
  })

  val reg = RegNext(io.in, false.B)

  io.pulse := reg && !io.in
}

// -----------------------------------------------------

object Nedge {
  def apply(in: Bool): Bool = {

    val n_edge = Module(new Nedge)
    n_edge.io.in := in

    return n_edge.io.pulse
  }
}
