package naruto

import chisel3._
import chisel3.util._
import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps // zipWithIndex
import Consts._
import ALU._

// 機械語をアレンジ
// 機械語とレジスタアドレスを保持
// 保持バンドルなのでInput/Outputなし
class ArangeInstructionBundle extends Bundle {
  // 機械語
  val inst = UInt(32.W)
  // 宛先レジスタ・アドレス (レジスタ番号)
  val rd = UInt(5.W)
  // 元レジスタ1・アドレス (レジスタ番号)
  val rs1 = UInt(5.W)
  // 元レジスタ2・アドレス (レジスタ番号)
  val rs2 = UInt(5.W)
}

// ==========================================================================================

// 内部制御信号バンドル
// 保持バンドルなのでInput/Outputなし
class DecodeResultBundle extends Bundle {
  // ↓ 保持バンドルのフィールド

  // 条件分岐命令(br)
  val branch = Bool()
  // 無条件分岐命令(jal)
  val jal = Bool()
  // 無条件分岐命令(レジスタ指定)(jalr)
  val jalr = Bool()
  // ALUの入力1の元データタイプ(s_alu1)
  val sel_alu1 = Bits(OP1.getWidth.W)
  // ALUの入力2の元データタイプ(s_alu2)
  val sel_alu2 = Bits(OP2.getWidth.W)
  // 即値(imm)
  val sel_imm = Bits(IMM_X.getWidth.W)
  // ALUの呼び出し機能(alu)
  val alu_fn = Bits(FN_X.getWidth.W)
  // メモリアクセス命令(mem_val)
  val mem = Bool()
  // メモリアクセスの種類(mem_cmd)
  val mem_cmd = Bits(M_SZ)
  // メモリアクセスのサイズ(mem_type)
  val mem_type = Bits(MT_SZ)
  // レジスタ書き込みあり
  val wxd = Bool()

// 機械語から制御信号(保持バンドル)のフィールドを設定して返す
  def decode(
      // 機械語
      inst: UInt,
      // 設定したいテーブル (InstructionTableで生成)
      tables: Iterable[(BitPat, Seq[BitPat])]
  ): DecodeResultBundle = {

    // 1. 『整形(デコード)した制御信号』Get
    val decoder: Seq[UInt] =
      DecodeLogic(
        // 機械語
        inst = inst,
        // 初期値（br, jal, jalr, s_alu1, s_alu2, imm, alu, mem_val, mem_cmd, mem_type, wxd)
        initializeBitPats = Seq(X, X, X, OP1, OP2, IMM_X, FN_X, N, M_X, MT_X, X),
        // 設定したいテーブル
        tables = tables
      )

    // 2. 『整形(デコード)した制御信号』を『保持バンドルフィールド』と対応づけ
    val ctrls = Seq(
      branch,
      jal,
      jalr,
      sel_alu1,
      sel_alu2,
      sel_imm,
      alu_fn,
      mem,
      mem_cmd,
      mem_type,
      wxd
    )

    // zipに続いてmap を実行すると、厳密性のために二つの別々の変換になるのに対し、zipped(lazyZip)に続いてmapを実行すると、怠惰のために一回の変換で実行される
    // (ctrls lazyZip decoder).map((ctrl, d) => ctrl := d)
    ctrls lazyZip decoder map { case (ctrl, d) => ctrl := d }

    return this // Bundle自身を返す
  }
}

// 制御信号生成に必要なオブジェクト ==================================================================================

// 『機械語』から『制御信号』を生成するオブジェクト
object DecodeLogic {

  def apply(
      // 機械語
      inst: UInt,
      // 初期値
      initializeBitPats: Seq[BitPat],
      // 設定したいテーブル
      tables: Iterable[(BitPat, Seq[BitPat])]
  ): Seq[UInt] = {

    // ------------------------------------------------------------------------------

    // -----------------table-------------------
    // Array(
    //  BEQ -> List(Y, ...,B_RS2, A_RS1, ...),
    //  JALR -> List(N, ...,B_IMM, A_RS1, ...),
    //  ...
    // )

    //                    ↓
    //                    ↓
    //                    ↓

    // -----------------mapping------------------
    // ArrayBuffer(
    //    ArrayBuffer(BEQ -> Y, JALR -> N, ...), // brの候補
    //    ...
    //    ArrayBuffer(BEQ -> B_RS2, JALR -> B_IMM, ...), // RS2の候補
    //    ArrayBuffer(BEQ -> A_RS1, JALR -> A_RS1, ...), // RS1の候補
    //    ...
    // )

    // initializeの要素数で初期化
    // 配列とサイズを格納するバッファ
    // 配列バッファへの最後の要素の追加はならし定数時間 (最後への追加が得意)
    val newTables =
      ArrayBuffer.fill(initializeBitPats.size)(ArrayBuffer[(BitPat, BitPat)]())

    for ((keyInst, ctrlList) <- tables)
      for ((ctrl, index) <- ctrlList.zipWithIndex)
        newTables(index) += keyInst -> ctrl

    // ------------------------------------------------------------------------------

    // 保持する制御信号を１つずつ確定
    // br, jal, jalr, s_alu2, s_alu1, imm, alu, mem_val, mem_cmd, mem_type, wxd
    val ctrl_signals =
      for ((oneBitPat, oneTable) <- initializeBitPats zip newTables)
        yield generateCtrl(inst, oneBitPat, oneTable)

    //   val ctrl_signals = ListLookup(
    //     inst,
    //     initializeBitPats.toList,
    //     tables.map { case (instBitPat, ctrlBiPats) => instBitPat -> ctrlBiPats.toList }.toArray
    //   )

      return ctrl_signals
  }

  def generateCtrl(
      inst: UInt,
      defaultBitPat: BitPat,
      table: Iterable[(BitPat, BitPat)]
  ): UInt = {
    return MuxCase(
      // デフォルト
      defaultBitPat.value.U,
      // 比較先
      // caseを展開
      table.map { case (instBitPat, ctrlBitPat) =>
        (inst === instBitPat) -> ctrlBitPat.value.U
      }.toSeq
    )
  }

  // 機械語から制御信号を生成 (１個ずつ)
  // BitPat -> UInt変換
  // MuxLookupやMuxCaseのkeyにBitPatは無理、あとdataにSeq[BitPat]が無理
  // Constsの設定を見直すことで解消できるかもしれない
  // def generateCtrl(
  //     inst: UInt,
  //     defaultBitPat: BitPat,
  //     table: Iterable[(BitPat, BitPat)]
  // ): UInt = {
  //   return MuxLookup(
  //     // 比較元
  //     inst,
  //     // デフォルト
  //     defaultBitPat.value.U,
  //     // 比較先
  //     // caseを展開
  //     table.map { case (instBitPat, ctrlBitPat) =>
  //       instBitPat.value.U -> ctrlBitPat.value.U
  //     }.toSeq
  //   )
  // }
  // 機械語から制御信号を生成 (１個ずつ)
  // BitPat -> UInt変換
  // def generateCtrl(
  //     inst: UInt,
  //     defaultBitPat: Seq[BitPat],
  //     table: Iterable[(BitPat, Seq[BitPat])]
  // ): Seq[BitPat] = {
  //   return MuxLookup(
  //     inst,
  //     // デフォルト
  //     defaultBitPat,
  //     // 比較先
  //     // caseを展開
  //     table.map { case (instBitPat, ctrlBitPat) =>
  //       instBitPat.value.U -> ctrlBitPat
  //     }.toSeq
  //   )
  // }
}
