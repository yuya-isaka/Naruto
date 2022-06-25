package naruto

import chisel3._
import chisel3.util._
import scala.language.implicitConversions // uintToBitPat

// UInt型 -> BitPat型　暗黙の型変換を実現
object CastImplicits {
  implicit def uintToBitPat(x: UInt) = BitPat(x)
}
