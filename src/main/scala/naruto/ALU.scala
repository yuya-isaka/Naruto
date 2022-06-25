package naruto

import chisel3._
import chisel3.util._
import ALU._

// ----------------------------------------------------------------------------

object ALU {

  // 機能の数
  val SZ_ALU_FN = 4.W
  // テンプレート
  val FN_X = BitPat("b????")
  // 加算
  val FN_ADD = 0.U
  // 左シフト
  val FN_SL = 1.U
  // 等号(==)判定            10 cmd(3)が立ってない
  val FN_SEQ = 2.U
  // 不等(!=)判定            11 cmd(3)が立ってない
  val FN_SNE = 3.U
  // 排他的論理和(XOR)
  val FN_XOR = 4.U
  // 右論理シフト
  val FN_SR = 5.U
  // 論理和(OR)
  val FN_OR = 6.U
  // 論理積(AND)
  val FN_AND = 7.U
  // 8以上 ------------------------------------------- cmd(3)が立つ
  // 減算
  val FN_SUB = 10.U
  // 右算術シフト
  val FN_SRA = 11.U
  // 12以上は比較演算 ---------------------------------
  // 未満(<)判定              1100
  val FN_SLT = 12.U
  // 以上(>=)判定             1101 cmd(0)が立っている
  val FN_SGE = 13.U
  // 未満(<)判定。符号なし     1110 cmd(1)が立っている
  val FN_SLTU = 14.U
  // 以上(>=)判定。符号なし    1111 cmd(1)が立っている cmd(0)が立っている
  val FN_SGEU = 15.U

  // 減算か否か
  // cmdの３ビット目が立っているかどうか
  // ここでは、io.fun(cmd)が8以上かどうかを確認
  def isSub(cmd: UInt) = cmd(3)

  // 比較か否か
  def isCmp(cmd: UInt) = cmd >= FN_SLT

  // 符号なし整数の比較か否か
  // cmd(1)が立ってたら符号なし整数の比較
  def cmpUnsigned(cmd: UInt) = cmd(1)

  // 比較処理結果にNOT演算必要か否か
  // cmdの０ビットが立っているかいないか。立っていたらNOT演算が必要
  // FN_SGE or FN_SGEUなら１
  def cmpInverted(cmd: UInt) = cmd(0)

  /** 比較処理の内で、等しいまたは等しくないの演算? */
  // 等しいか否か（比較処理中）
  // 等号、不等の判定は、3ビット目が立ってない
  def cmpEq(cmd: UInt) = !cmd(3)

}

// ----------------------------------------------------------------------------

// io.fnで何を演算させるか制御
// io.fnはデコーダから出力される制御信号生成器から出力, IDecodeクラス
class ALU extends Module {

  val io = IO(new Bundle {
    // 演算の機能種類の指定
    val fn = Input(UInt(SZ_ALU_FN))
    // オペランド１
    val in1 = Input(UInt(32.W))
    // オペランド２
    val in2 = Input(UInt(32.W))
    // 演算結果
    val alu_out = Output(UInt(32.W))
    // 比較結果
    val cmp_out = Output(Bool())
  })

  // 加算、減算
  val in2_inv = Mux(isSub(io.fn), ~io.in2, io.in2)
  // xorは加算
  val in1_xor_in2 = io.in1 ^ in2_inv
  val adder_out = io.in1 + in2_inv + isSub(io.fn)

  // 各種比較 (未満、等値、不等)
  val slt =
    Mux(
      io.in1(31) === io.in2(31),
      adder_out(31),
      Mux(cmpUnsigned(io.fn), io.in2(31), io.in1(31))
    )
  io.cmp_out := cmpInverted(io.fn) ^ Mux(cmpEq(io.fn), in1_xor_in2 === 0.U, slt)

  // シフト演算
  val (shamt, shin_r) = (io.in2(4, 0), io.in1)
  val shin = Mux(io.fn === FN_SR || io.fn === FN_SRA, shin_r, Reverse(shin_r))
  val shout_r = (Cat(isSub(io.fn) & shin(31), shin).asSInt >> shamt)(31, 0)
  val shout_l = Reverse(shout_r)
  val shout = Mux(io.fn === FN_SR || io.fn === FN_SRA, shout_r, 0.U) |
    Mux(io.fn === FN_SL, shout_l, 0.U)

  // ビット演算 (補足: A XOR B | A AND Bは、 A OR Bと同じ)
  val logic = Mux(io.fn === FN_XOR || io.fn === FN_OR, in1_xor_in2, 0.U) |
    Mux(io.fn === FN_OR || io.fn === FN_AND, io.in1 & io.in2, 0.U)
  val shift_logic = (isCmp(io.fn) && slt) | logic | shout
  val alu_out =
    Mux(io.fn === FN_ADD || io.fn === FN_SUB, adder_out, shift_logic)

  // 出力
  io.alu_out := alu_out
}
