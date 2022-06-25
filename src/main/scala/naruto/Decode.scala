package naruto

import chisel3._
import chisel3.util._
import ALU._
import Consts._
import Instructions._
import CastImplicits._

// ArangeInstruction (AI) =========================================================================================

// 機械語 -> 整理した機械語 を生成するオブジェクト
object ArangeInstruction {

  def apply(inst: UInt): ArangeInstructionBundle = {
    val res = Wire(new ArangeInstructionBundle)
    res.inst := inst
    res.rd := inst(11, 7)
    res.rs1 := inst(19, 15)
    res.rs2 := inst(24, 20)
    return res
  }

}

// InstructionTable =================================================================================================

// 機械語 <> 制御信号　のテーブルを生成するオブジェクト
object InstructionTable {

  // br, jal, jalr, s_slu2, s_alu1, imm, alu, mem, mem_cmd, mem_type, wxd
  def apply(): Seq[(BitPat, Seq[BitPat])] = {
    return Seq(
      // KeyInst -> CtrlList
      BEQ -> Seq(Y, N, N, OP1_RS1, OP2_RS2, IMM_B, FN_SEQ, N, M_X, MT_X, N),
      JALR -> Seq(N, N, Y, OP1_RS1, OP2_IMM, IMM_I, FN_ADD, N, M_X, MT_X, Y),
      JAL -> Seq(N, Y, N, OP1_PC, OP2_SIZE, IMM_J, FN_ADD, N, M_X, MT_X, Y),
      LUI -> Seq(N, N, N, OP1_ZERO, OP2_IMM, IMM_U, FN_ADD, N, M_X, MT_X, Y),
      AUIPC -> Seq(N, N, N, OP1_PC, OP2_IMM, IMM_U, FN_ADD, N, M_X, MT_X, Y),
      ADDI -> Seq(N, N, N, OP1_RS1, OP2_IMM, IMM_I, FN_ADD, N, M_X, MT_X, Y),
      LB -> Seq(N, N, N, OP1_RS1, OP2_IMM, IMM_I, FN_ADD, Y, M_XRD, MT_B, Y),
      SB -> Seq(N, N, N, OP1_RS1, OP2_IMM, IMM_S, FN_ADD, Y, M_XWR, MT_B, N)
    )
  }

}
