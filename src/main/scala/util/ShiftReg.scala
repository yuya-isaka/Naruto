package naruto

import chisel3._
import chisel3.util._

// SIPO --------------------------------------------------------------------------

class SerialInParallelOut(nbit: Int) extends Module {
  val io = IO(new Bundle {

    // 入力
    val shiftIn = Input(Bool())

    // 入力取り込むか否か
    val load = Input(Bool())

    // 出力 (後の入力が下位ビット)
    val shiftOut = Output(UInt(nbit.W))
  })

  val reg = RegInit(0.U(nbit.W))
  when(io.load) {
    reg := Cat(reg(nbit - 2, 0), io.shiftIn)
  }

  io.shiftOut := reg
}

// PISO --------------------------------------------------------------------------

class ParallelInSerialOut(nbit: Int) extends Module {
  val io = IO(new Bundle {

    // 入力
    val shiftIn = Input(UInt(nbit.W))

    // 入力取り込むか否か
    val load = Input(Bool())

    // シフトするか否か
    val enable = Input(Bool())

    // 出力 (上位ビットから出力)
    val shiftOut = Output(Bool())
  })

  val reg = RegInit(0.U(nbit.W))
  when(io.load) {
    reg := io.shiftIn
  }.elsewhen(io.enable) {
    reg := Cat(reg(nbit - 2, 0), true.B)
  }

  io.shiftOut := reg(nbit - 1)
}
