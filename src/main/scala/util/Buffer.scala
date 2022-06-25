package naruto

import chisel3._
import chisel3.util._

// -------------------------------------------

// 同期
class Buffer extends Module {
  val io = IO(new Bundle {
    val in = Input(Bool())
    val out = Output(Bool())
  })

  val reg1 = RegNext(io.in)
  val reg2 = RegNext(reg1)

  io.out := reg2
}

// -------------------------------------------

object Buffer {
  def apply(in: Bool): Bool = {
    val buffer = Module(new Buffer)
    buffer.io.in := in
    return buffer.io.out
  }
}
