package naruto

import chisel3._
import chisel3.util._

// モジュールではない (呼び出しの数だけ回路が形成)
class RegFile {
  val rf = Mem(32, UInt(32.W))

  // 読み出し (addr: 0-31)
  def read(addr: UInt) = {
    Mux(addr === 0.U, 0.U(32.W), rf(addr))
  }

  // 書き込み (addr: 0-31, data: 32bit)
  def write(addr: UInt, data: UInt) = {
    when(addr =/= 0.U) {
      rf(addr) := data
    }
  }
}
