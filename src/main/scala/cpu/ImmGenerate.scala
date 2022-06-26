package naruto

import chisel3._
import chisel3.util._
import Consts._

/** 即値生成クラス */
object ImmGen {

  /** 命令から即値を生成します。
    * @param sel
    *   命令形式。ScalarOpConstantsの、IMM_X形式の定数
    * @param inst
    *   機械語命令
    *
    * @return
    *   32ビットの符号あり整数
    */
  def apply(sel: UInt, inst: UInt) = {
    val sign = Mux(sel === IMM_Z, 0.S, inst(31).asSInt)
    val b30_20 = Mux(sel === IMM_U, inst(30, 20).asSInt, sign)
    val b19_12 = Mux(sel =/= IMM_U && sel =/= IMM_J, sign, inst(19, 12).asSInt)
    val b11 = Mux(
      sel === IMM_U || sel === IMM_Z,
      0.S,
      Mux(
        sel === IMM_J,
        inst(20).asSInt,
        Mux(sel === IMM_B, inst(7).asSInt, sign)
      )
    )
    val b10_5 = Mux(sel === IMM_U || sel === IMM_Z, 0.U, inst(30, 25))
    val b4_1 = Mux(
      sel === IMM_U,
      0.U,
      Mux(
        sel === IMM_S || sel === IMM_B,
        inst(11, 8),
        Mux(sel === IMM_Z, inst(19, 16), inst(24, 21))
      )
    )
    val b0 = Mux(
      sel === IMM_S,
      inst(7),
      Mux(sel === IMM_I, inst(20), Mux(sel === IMM_Z, inst(15), 0.U))
    )

    Cat(sign, b30_20, b19_12, b11, b10_5, b4_1, b0).asSInt
  }
}
