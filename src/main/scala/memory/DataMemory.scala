package naruto

import chisel3._
import chisel3.util._

// データメモリ（4KB (32bit 1024個), リトルエンディアン）
// inits: 初期データ
class DData(inits: List[Long]) extends Module {
  val io = IO(new DDataBundle)

  // 最大データ数
  val wordMax = 1024

  // アクセスサイズ
  // 1バイトアクセス
  val bAccess = 0.U
  // 2バイトアクセス
  val hAccess = 1.U
  // 4バイトアクセス
  val wAccess = 2.U

  // RAMの初期値 1024命令に調整
  val image = inits ::: List.fill(wordMax - inits.length)(0L)

  // RAM (レジスタの集合)
  val ram = RegInit(VecInit(image.map((n) => n.asUInt(32.W))))

  // アドレス指定されたデータ
  val accessWord = ram(io.addr(log2Ceil(wordMax) - 1, 2))

  // 読み込み (下位2ビットは無視して、4バイト単位でアクセス) ----------------------------------------
  // switchでいける？
  // https://lynxeyed.hatenablog.com/entry/2020/07/08/235112
  when(io.size === bAccess) { // LB
    io.readData := Cat(
      "h000".U,
      // 下位2ビット分で読み出すビット・フィールドを決定
      MuxLookup(
        io.addr(1, 0),
        0.U(8.W),
        Array(
          0.U -> accessWord(7, 0),
          1.U -> accessWord(15, 8),
          2.U -> accessWord(23, 16),
          3.U -> accessWord(31, 24)
        ).toVector
      )
    )
  }.elsewhen(io.size === hAccess) { // LH
    io.readData := Cat(
      "h00".U,
      // ビット1で読み出すビット・フィールドを決定
      MuxLookup(
        io.addr(1),
        0.U(16.W),
        Array(0.U -> accessWord(15, 0), 1.U -> accessWord(31, 16)).toIndexedSeq
      )
    )
  }.otherwise { // LW
    io.readData := accessWord
  }

  // 書き込み ---------------------------------------------------------------------------
  when(io.writeEnable) {
    when(io.size === bAccess) { // SB
      val bData = io.writeData(7, 0)
      accessWord := MuxLookup(
        io.addr(1, 0),
        0.U(32.W),
        Array(
          0.U -> Cat(accessWord(31, 8), bData),
          1.U -> Cat(accessWord(31, 16), bData, accessWord(7, 0)),
          2.U -> Cat(accessWord(31, 24), bData, accessWord(15, 0)),
          3.U -> Cat(bData, accessWord(23, 0))
        ).toVector
      )
    }.elsewhen(io.size === hAccess) { // SH
      val hData = io.writeData(15, 0)
      accessWord := MuxLookup(
        io.addr(1),
        0.U(32.W),
        Array(
          0.U -> Cat(accessWord(31, 16), hData),
          1.U -> Cat(hData, accessWord(15, 0))
        ).toVector
      )
    }.otherwise { // SW
      accessWord := io.writeData
    }
  }
}
