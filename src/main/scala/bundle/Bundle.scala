package naruto

import chisel3._
import chisel3.util._

// ==========================================================================================

// トップバンドル
class TopIo[Conf <: RVConfig](conf: Conf) extends Bundle {
  // ホストから送られてくるデータ
  val rxData = Input(Bool())

  // ホストに送るデータ
  val txData = Output(Bool())
  // デバッグモニタ
  val dbg_monitor = new CpuDebugMonitor(conf)
  // 開発のみ使用 (HexTest)
  val exit = if (conf.debug == true) Output(UInt(1.W)) else Output(UInt(0.W))
}

// ==========================================================================================

// データメモリバンドル
class DDataBundle extends Bundle {
  // 読み取りor書き込み先アドレス
  val addr = Input(UInt(32.W))
  // データサイズ (0: 1バイト, 1: 2バイト, 2: 4バイト funct3の下位2ビット)
  val size = Input(UInt(2.W))
  // 書き込みデータ
  val writeData = Input(UInt(32.W))
  // 書き込みチェック
  val writeEnable = Input(Bool())

  // 読み出しデータ
  val readData = Output(UInt(32.W))
}

// 命令メモリバンドル
class IDataBundle extends Bundle {
  // 機械語のアドレス
  val addr = Input(UInt(32.W))

  // 機械語
  val inst = Output(UInt(32.W))
}
