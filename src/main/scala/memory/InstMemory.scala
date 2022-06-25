package naruto

import chisel3._
import chisel3.util._

// 命令データメモリ (4KB (32bit 1024命令))
class IData(insts: List[Long]) extends Module {
  val io = IO(new IDataBundle)

  // 最大命令数
  val wordMax = 1024

  // ROMの元イメージ 1024命令に調整 足りない分は0でパディング
  val image = insts ::: List.fill(wordMax - insts.length)(0L)

  // ROM本体
  val rom = VecInit(image.map((n) => n.asUInt(32.W)))

  // 下位2ビットは無視して、4バイト単位でアクセス
  io.inst := rom(io.addr(log2Ceil(wordMax) - 1, 2))
}
