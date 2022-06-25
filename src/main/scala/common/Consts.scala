package naruto

import chisel3._
import chisel3.util._

object Consts {

  // 不定値
  val X = BitPat("b?")
  // No
  val N = BitPat("b0")
  // Yes
  val Y = BitPat("b1")

  // OP1テンプレート
  val OP1 = BitPat("b??")
  // ゼロ・レジスタ
  val OP1_ZERO = 0.U(2.W)
  // rs1に指定されたレジスタ値
  val OP1_RS1 = 1.U(2.W)
  // プログラム・カウンタの値
  val OP1_PC = 2.U(2.W)

  // テンプレート
  val OP2 = BitPat("b??")
  // 1命令のバイト数
  val OP2_SIZE = 1.U(2.W)
  // rs2に指定されたレジスタ値
  val OP2_RS2 = 2.U(2.W)
  // 命令ビット内の即値
  val OP2_IMM = 3.U(2.W)

  // 機械語形式テンプレート
  val IMM_X = BitPat("b???")
  // S形式の即値
  val IMM_S = 0.U(3.W)
  // B形式の即値
  val IMM_B = 1.U(3.W)
  // U形式の即値
  val IMM_U = 2.U(3.W)
  // J形式の即値
  val IMM_J = 3.U(3.W)
  // I形式の即値
  val IMM_I = 4.U(3.W)
  // Z形式(システム管理命令で使用)の即値
  val IMM_Z = 5.U(3.W)

  // メモリアクセスサイズテンプレート
  val MT_X = BitPat("b???")
  // メモリ・アクセスサイズ (funct3)
  val MT_SZ = 3.W
  // バイト単位アクセス (funct3)
  val MT_B = "b000".U

  // メモリアクセスの種類テンプレート
  def M_X = BitPat("b?????")
  // メモリアクセスの種類
  val M_SZ = 5.W
  // int load
  def M_XRD = "b00000".U;
  // int store
  def M_XWR = "b00001".U;
}
