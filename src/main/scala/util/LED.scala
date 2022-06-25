package naruto

import chisel3._
import chisel3.util._

// util ----------------------------------------------------------------

// バンドル
class LEDBundle extends Bundle {

  // 光らせる部位（gfe_dcba)
  val cathodes = UInt(7.W)

  // 小数点
  val decimal = UInt(1.W)

  // 表示桁
  val anodes = UInt(8.W)
}

// 本体 ----------------------------------------------------------------

// 7segLED
class LED extends Module {
  val io = IO(new Bundle {

    // 16パターン、４桁
    val digits = Input(Vec(4, UInt(4.W)))

    // 点滅
    val blink = Input(Bool())

    // led
    val led = Output(new LEDBundle)
  })

  // 数値 ----------------------------------------------------------------

  // 桁チェンジ (10万回に1回)
  val (_, digit_change) = Counter(true.B, 100000)

  // 表示桁
  val (digit_index, _) = Counter(digit_change, 4)

  // 表示数値
  val digitNum = io.digits(digit_index)

  // カソード ----------------------------------------------------------------

  // gfe_dcba
  io.led.cathodes := MuxCase(
    "b111_1111".U,
    Array(
      (digitNum === "h0".U) -> "b100_0000".U,
      (digitNum === "h1".U) -> "b111_1001".U,
      (digitNum === "h2".U) -> "b010_0100".U,
      (digitNum === "h3".U) -> "b011_0000".U,
      (digitNum === "h4".U) -> "b001_1001".U,
      (digitNum === "h5".U) -> "b001_0010".U,
      (digitNum === "h6".U) -> "b000_0010".U,
      (digitNum === "h7".U) -> "b101_1000".U,
      (digitNum === "h8".U) -> "b000_0000".U,
      (digitNum === "h9".U) -> "b001_0000".U,
      (digitNum === "ha".U) -> "b000_1000".U,
      (digitNum === "hb".U) -> "b000_0011".U,
      (digitNum === "hc".U) -> "b100_0110".U,
      (digitNum === "hd".U) -> "b010_0001".U,
      (digitNum === "he".U) -> "b000_0110".U,
      (digitNum === "hf".U) -> "b000_1110".U
    ).toIndexedSeq
  )

  // アノード ----------------------------------------------------------------

  // 初期、0桁目表示
  val anodes = RegInit("b1111_1110".U(4.W))

  // 桁チェンジ
  when(digit_change) {
    // ローテートシフト
    anodes := Cat(anodes(6, 0), anodes(7))
  }

  // 点滅制御 ----------------------------------------------------------------

  // 点滅 (1億回に1回), io.blinkは外からの入力, 光らせるタイミングは外の回路次第
  val (_, blink_toggle) = Counter(io.blink, 100000000)

  // 点滅時 点灯制御
  val blink_light = RegInit(true.B)
  when(blink_toggle) {
    blink_light := !blink_light
  }

  // 点灯するとき、anodeを設定、それ以外は1(点灯しない)
  io.led.anodes := Mux(!io.blink || blink_light, anodes, "hff".U)

  // 小数点なし
  io.led.decimal := 1.U
}
