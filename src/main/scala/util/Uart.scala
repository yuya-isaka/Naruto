package naruto

import chisel3._
import chisel3.util._

// util --------------------------------------------------------------

object Uart {
  // クロック周波数
  val clock_freq = 70000000

  // 転送速度
  val trans_bps = 115200
}

import Uart._

// 本体 --------------------------------------------------------------

class Uart extends Module {
  val io = IO(new Bundle {

    // ホストPCからの受信データ
    val rxData = Input(Bool())

    // ホストPCへの送信データ
    val txData = Output(Bool())

    // Ready-Validインタフェース
    // input: ready
    // output: valid
    // output: bits
    val receiveData = Decoupled(UInt(8.W))

    // 送信データ
    // input: valid
    // input: bits
    // output: ready
    val sendData = Flipped(Decoupled(UInt(8.W)))
  })

  // 受信モジュール ---------------------------------
  val uartRx = Module(new UartRx)
  // ホストPCと接続
  uartRx.io.rxData := io.rxData
  io.receiveData <> uartRx.io.receiveData

  // 送信モジュール ---------------------------------
  // 送信データをキューに入れる
  val txQueue = Module(new Queue(UInt(8.W), 64))
  txQueue.io.enq <> io.sendData

  // ホスト PCと接続
  val uartTx = Module(new UartTx)
  io.txData := uartTx.io.txData
  uartTx.io.sendData <> txQueue.io.deq
}

// 送信 ----------------------------------------------------------------------------------

class UartTx extends Module {
  val io = IO(new Bundle {

    // 整形前の中のデータ
    // input: valid
    // input: bits
    // output: ready
    val sendData = Flipped(Decoupled(UInt(8.W)))

    // ホストPCへの送信データ
    val txData = Output(Bool())
  })

  // -----------------------------------------------------------------

  // ステートマシン
  val sIdle :: sStartBit :: sSend :: sStopBit :: Nil = Enum(4)
  val state = RegInit(sIdle)

  // 状態遷移カウンタ
  val (txCount, txEnable) = Counter(true.B, clock_freq / trans_bps)

  // 8bitずつ送信
  val sendCount = Reg(UInt(3.W))

  // シフトレジスタ
  val shiftReg = Module(new ParallelInSerialOut(8))
  shiftReg.io.shiftIn := Reverse(io.sendData.bits)
  shiftReg.io.load := state === sIdle && io.sendData.valid
  shiftReg.io.enable := state === sSend && txEnable

  // -----------------------------------------------------------------

  // 状態遷移
  when(state === sIdle && io.sendData.valid) {
    state := sStartBit
    txCount := 0.U
  }.elsewhen(txEnable) {
    switch(state) {
      // スタートビット
      is(sStartBit) {
        state := sSend
        sendCount := 0.U
      }
      // 送信
      is(sSend) {
        when(sendCount === 7.U) {
          state := sStopBit
        }.otherwise {
          sendCount := sendCount + 1.U
        }
      }
      // ストップビット
      is(sStopBit) {
        state := sIdle
      }
    }
  }

  // 送信準備完了 (ready -> valid)
  io.sendData.ready := state === sIdle

  // ホストPCへの送信データ
  io.txData := MuxCase(
    true.B,
    Array(
      (state === sStartBit) -> false.B,
      (state === sSend) -> shiftReg.io.shiftOut,
      (state === sStopBit) -> true.B
    ).toIndexedSeq
  )
}

// 受信 ----------------------------------------------------------------------------------

class UartRx extends Module {
  val io = IO(new Bundle {

    // ホストPCからの受信データ
    val rxData = Input(Bool())

    // 整形後の中に取り込んだ後のデータ
    // input: ready
    // output: valid
    // output: bits
    val receiveData = Decoupled(UInt(8.W))
  })

  // --------------------------------------------------------------

  // ステートマシン
  val sIdle :: sStartBit :: sReceive :: sStopBit :: Nil = Enum(4)
  val state = RegInit(sIdle)

  // 状態遷移カウンタ
  val (rxCount, rxEnable) = Counter(true.B, clock_freq / trans_bps)
  // 1/2ビット周期カウンタ
  val (rxHalfCount, rxHalfEnable) =
    Counter(true.B, clock_freq / trans_bps / 2)

  // 8ビットずつ受信
  val receiveCount = Reg(UInt(3.W))

  // シフトレジスタ
  val shiftReg = Module(new SerialInParallelOut(8))
  shiftReg.io.shiftIn := io.rxData
  shiftReg.io.load := state === sReceive && rxEnable

  val rDataValid = RegInit(false.B)
  // NedgeとBufferはobject
  val start = Nedge(Buffer(io.rxData))

  // --------------------------------------------------------------

  // 状態遷移
  when(state === sIdle && start) {
    state := sStartBit
    rxHalfCount := 0.U
    // スタートビット
  }.elsewhen(state === sStartBit && rxHalfEnable) {
    state := sReceive
    rxCount := 0.U
    receiveCount := 0.U
  }.elsewhen(rxEnable) {
    switch(state) {
      // 受信
      is(sReceive) {
        when(receiveCount === 7.U) {
          state := sStopBit
        }.otherwise {
          receiveCount := receiveCount + 1.U
        }
      }
      // ストップビット
      is(sStopBit) {
        when(rxEnable) {
          state := sIdle
        }
      }
    }
  }

  // 受信準備完了フラグ
  when(state === sStopBit && rxEnable) {
    rDataValid := true.B
  }.elsewhen(io.receiveData.ready) {
    rDataValid := false.B
  }

  // 受信
  io.receiveData.bits := Reverse(shiftReg.io.shiftOut)
  io.receiveData.valid := rDataValid
}
