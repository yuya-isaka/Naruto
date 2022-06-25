package naruto

import chisel3._
import chisel3.util._
import chisel3.withClock
import chisel3.stage.ChiselStage
import Program._

// 開発 =============================================================================================

class TopDevelop[Conf <: RVConfig](conf: Conf) extends Module {
  val io = IO(new TopIo(conf))

  // CPU <> Memory --------------------------------------------------------------------------

  val cpu = Module(new RISCV(conf))

  val iData = Module(new IData(iData_Program))
  iData.io <> cpu.io.iData

  // データメモリは0x80001000 ~ h80002000の間
  val dData = Module(new DData(dData_Program))
  when(
    "h80001000".U <= cpu.io.dData.addr && cpu.io.dData.addr < "h80002000".U
  ) {
    dData.io.addr := cpu.io.dData.addr
    dData.io.size := cpu.io.dData.size
    dData.io.writeData := cpu.io.dData.writeData
    dData.io.writeEnable := cpu.io.dData.writeEnable
  }.otherwise {
    dData.io.addr := 0.U
    dData.io.size := 0.U
    dData.io.writeData := 0.U
    dData.io.writeEnable := false.B
  }

  // Top <> Uart, CPU <> Uart -----------------------------------------------------------------

  val uart = Module(new Uart)

  // 全体の入出力
  uart.io.rxData := io.rxData
  io.txData := uart.io.txData

  // CPUからh10000000への出力は、uartへの出力とする（メモリマップドIO）
  when("h10000000".U === cpu.io.dData.addr) {
    uart.io.sendData.bits := cpu.io.dData.writeData
    uart.io.sendData.valid := cpu.io.dData.writeEnable
  }.otherwise {
    uart.io.sendData.bits := 0.U
    uart.io.sendData.valid := false.B
  }
  uart.io.receiveData.ready := false.B

  // uartへの出力の時は、cpuの方は０にする
  when("h10000000".U === cpu.io.dData.addr) {
    cpu.io.dData.readData := 0.U
  }.elsewhen(
    "h80001000".U <= cpu.io.dData.addr && cpu.io.dData.addr < "h80002000".U
  ) {
    cpu.io.dData.readData := dData.io.readData
  }.otherwise {
    cpu.io.dData.readData := 0.U
  }

  // Debug Monitor -----------------------------------------------------------------------------

  io.dbg_monitor.pc := cpu.io.dbg_monitor.pc
  io.dbg_monitor.reg_0 := cpu.io.dbg_monitor.reg_0
  io.dbg_monitor.reg_1 := cpu.io.dbg_monitor.reg_1
  io.dbg_monitor.reg_2 := cpu.io.dbg_monitor.reg_2
  io.dbg_monitor.reg_3 := cpu.io.dbg_monitor.reg_3
  io.dbg_monitor.reg_4 := cpu.io.dbg_monitor.reg_4
  io.dbg_monitor.reg_5 := cpu.io.dbg_monitor.reg_5
  io.dbg_monitor.reg_6 := cpu.io.dbg_monitor.reg_6
  io.dbg_monitor.reg_7 := cpu.io.dbg_monitor.reg_7
  io.dbg_monitor.reg_8 := cpu.io.dbg_monitor.reg_8
  io.dbg_monitor.reg_9 := cpu.io.dbg_monitor.reg_9
  io.dbg_monitor.reg_10 := cpu.io.dbg_monitor.reg_10
  io.dbg_monitor.reg_11 := cpu.io.dbg_monitor.reg_11
  io.dbg_monitor.reg_12 := cpu.io.dbg_monitor.reg_12
  io.dbg_monitor.reg_13 := cpu.io.dbg_monitor.reg_13
  io.dbg_monitor.reg_14 := cpu.io.dbg_monitor.reg_14
  io.dbg_monitor.reg_15 := cpu.io.dbg_monitor.reg_15
  io.dbg_monitor.reg_16 := cpu.io.dbg_monitor.reg_16
  io.dbg_monitor.reg_17 := cpu.io.dbg_monitor.reg_17
  io.dbg_monitor.reg_18 := cpu.io.dbg_monitor.reg_18
  io.dbg_monitor.reg_19 := cpu.io.dbg_monitor.reg_19
  io.dbg_monitor.reg_20 := cpu.io.dbg_monitor.reg_20
  io.dbg_monitor.reg_21 := cpu.io.dbg_monitor.reg_21
  io.dbg_monitor.reg_22 := cpu.io.dbg_monitor.reg_22
  io.dbg_monitor.reg_23 := cpu.io.dbg_monitor.reg_23
  io.dbg_monitor.reg_24 := cpu.io.dbg_monitor.reg_24
  io.dbg_monitor.reg_25 := cpu.io.dbg_monitor.reg_25
  io.dbg_monitor.reg_26 := cpu.io.dbg_monitor.reg_26
  io.dbg_monitor.reg_27 := cpu.io.dbg_monitor.reg_27
  io.dbg_monitor.reg_28 := cpu.io.dbg_monitor.reg_28
  io.dbg_monitor.reg_29 := cpu.io.dbg_monitor.reg_29
  io.dbg_monitor.reg_30 := cpu.io.dbg_monitor.reg_30
  io.dbg_monitor.reg_31 := cpu.io.dbg_monitor.reg_31

  // Hello World プログラムの終了条件、HexTestで利用 --------------------------------------------------

  val count = RegInit(0.U(6.W))
  count := count + 1.U
  io.exit := 0.U
  printf(p"${count}|    PC     : 0x${Hexadecimal(iData.io.inst)}\n")
  when(iData.io.inst === "h0000006f".U) {
    io.exit := 1.U
  }
}

// 本番 ===============================================================================================================

object Naruto extends App {
  (new ChiselStage).emitVerilog(
    new Top(new RV32Config),
    Array(
      "-o",
      "naruto.v",
      "--target-dir",
      "rtl"
    )
  )
}

// 遅いクロック生成 (Vivadoのクロックウィザードで70MHzに設定)
// ブラックボックスモジュールはChiselにVerilogを埋め込む方法
// HasBlckBoxInline ... モジュール内にインランでVerilogを記述
// HasBlckBoxResource ... モジュール内にインランでVerilogを記述
// ブラックボックスを使うモジュールでは、クロックとリセットを追加しないと自動的に挿入されない
class SlowClock extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    // 元々のクロック
    val clk_original = Input(Clock())
    // 新しいクロック
    val clk_new = Output(Clock())
  })
}

class Top[Conf <: RVConfig](conf: Conf) extends Module {
  val io = IO(new TopIo(conf))

  val slowClock = Module(new SlowClock)
  slowClock.io.clk_original := clock

  withClock(slowClock.io.clk_new) {

    // CPU <> Memory ---------------------------------------------------------------------------

    val cpu = Module(new RISCV(conf))

    val iData = Module(new IData(iData_Program))
    iData.io <> cpu.io.iData

    // データメモリは0x80001000 ~ h80002000の間
    val dData = Module(new DData(dData_Program))
    when(
      "h80001000".U <= cpu.io.dData.addr && cpu.io.dData.addr < "h80002000".U
    ) {
      dData.io.addr := cpu.io.dData.addr
      dData.io.size := cpu.io.dData.size
      dData.io.writeData := cpu.io.dData.writeData
      dData.io.writeEnable := cpu.io.dData.writeEnable
    }.otherwise {
      dData.io.addr := 0.U
      dData.io.size := 0.U
      dData.io.writeData := 0.U
      dData.io.writeEnable := false.B
    }

    // Top <> Uart, CPU <> Uart ---------------------------------------------------------------------------

    val uart = Module(new Uart)

    // 全体の入出力
    uart.io.rxData := io.rxData
    io.txData := uart.io.txData

    // CPUからh10000000への出力は、uartへの出力とする（メモリマップドIO）
    when("h10000000".U === cpu.io.dData.addr) {
      uart.io.sendData.bits := cpu.io.dData.writeData
      uart.io.sendData.valid := cpu.io.dData.writeEnable
    }.otherwise {
      uart.io.sendData.bits := 0.U
      uart.io.sendData.valid := false.B
    }
    uart.io.receiveData.ready := false.B

    // uartへの出力の時は、cpuの方は０にする
    when("h10000000".U === cpu.io.dData.addr) {
      cpu.io.dData.readData := 0.U
    }.elsewhen(
      "h80001000".U <= cpu.io.dData.addr && cpu.io.dData.addr < "h80002000".U
    ) {
      cpu.io.dData.readData := dData.io.readData
    }.otherwise {
      cpu.io.dData.readData := 0.U
    }

    // Debug Monitor ----------------------------------------------------------------------------

    // PC
    io.dbg_monitor.pc := cpu.io.dbg_monitor.pc

    // Register 0-31
    io.dbg_monitor.reg_0 := cpu.io.dbg_monitor.reg_0
    io.dbg_monitor.reg_1 := cpu.io.dbg_monitor.reg_1
    io.dbg_monitor.reg_2 := cpu.io.dbg_monitor.reg_2
    io.dbg_monitor.reg_3 := cpu.io.dbg_monitor.reg_3
    io.dbg_monitor.reg_4 := cpu.io.dbg_monitor.reg_4
    io.dbg_monitor.reg_5 := cpu.io.dbg_monitor.reg_5
    io.dbg_monitor.reg_6 := cpu.io.dbg_monitor.reg_6
    io.dbg_monitor.reg_7 := cpu.io.dbg_monitor.reg_7
    io.dbg_monitor.reg_8 := cpu.io.dbg_monitor.reg_8
    io.dbg_monitor.reg_9 := cpu.io.dbg_monitor.reg_9
    io.dbg_monitor.reg_10 := cpu.io.dbg_monitor.reg_10
    io.dbg_monitor.reg_11 := cpu.io.dbg_monitor.reg_11
    io.dbg_monitor.reg_12 := cpu.io.dbg_monitor.reg_12
    io.dbg_monitor.reg_13 := cpu.io.dbg_monitor.reg_13
    io.dbg_monitor.reg_14 := cpu.io.dbg_monitor.reg_14
    io.dbg_monitor.reg_15 := cpu.io.dbg_monitor.reg_15
    io.dbg_monitor.reg_16 := cpu.io.dbg_monitor.reg_16
    io.dbg_monitor.reg_17 := cpu.io.dbg_monitor.reg_17
    io.dbg_monitor.reg_18 := cpu.io.dbg_monitor.reg_18
    io.dbg_monitor.reg_19 := cpu.io.dbg_monitor.reg_19
    io.dbg_monitor.reg_20 := cpu.io.dbg_monitor.reg_20
    io.dbg_monitor.reg_21 := cpu.io.dbg_monitor.reg_21
    io.dbg_monitor.reg_22 := cpu.io.dbg_monitor.reg_22
    io.dbg_monitor.reg_23 := cpu.io.dbg_monitor.reg_23
    io.dbg_monitor.reg_24 := cpu.io.dbg_monitor.reg_24
    io.dbg_monitor.reg_25 := cpu.io.dbg_monitor.reg_25
    io.dbg_monitor.reg_26 := cpu.io.dbg_monitor.reg_26
    io.dbg_monitor.reg_27 := cpu.io.dbg_monitor.reg_27
    io.dbg_monitor.reg_28 := cpu.io.dbg_monitor.reg_28
    io.dbg_monitor.reg_29 := cpu.io.dbg_monitor.reg_29
    io.dbg_monitor.reg_30 := cpu.io.dbg_monitor.reg_30
    io.dbg_monitor.reg_31 := cpu.io.dbg_monitor.reg_31

    // io.exit (本番ではHexTestが使えないのでfalse.B入力) -------------------------------------------
    io.exit := 0.U
  }
}
