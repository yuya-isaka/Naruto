/*
テストの流れ
・Chiselテストコードファイルを作成
・テスト用のhexファイルを作成
・hexファイルをMemoryクラスでロード
・Coreクラスでデバッグ信号を出力
・sbtを利用してScalaコードをコンパイルした後，テストを実行
 */

package naruto

import chisel3._
import org.scalatest.flatspec._
import chiseltest._
import java.io._

class CpuTopDebug extends AnyFlatSpec with ChiselScalatestTester {
  "Naruto RISC-V" should "Debug monitor" in {

    val writer = new PrintWriter(new File("trace.log"))

    println(
      "\n----------------------------  start ------------------------------\n"
    )

    // バックエンドをVerilator
    test(new TopDevelop(new RV32ConfigDebug)).withAnnotations(Seq(VerilatorBackendAnnotation)) {
      // test(new TopDevelop(new RV32ConfigDebug)) { // デフォルト
      c =>
        println("")
        // step1000回以上を許容
        c.clock.setTimeout(0)

        // ----------------------------------------------------------------
        // for (i <- 0 until 10) {
        //   // peekInt
        //   // peekBoolean
        //   // とかある。
        //   val pc: BigInt = c.io.dbg_monitor.pc.peekInt()
        //   writer.printf("PC : %08x\n".format(pc))
        //   c.clock.step(1)
        // }
        // ----------------------------------------------------------------
        for (i <- 0 until 40) {
          // PC
          val pc = c.io.dbg_monitor.pc.peekInt()

          // Register 0-31
          val reg0 = c.io.dbg_monitor.reg_0.peekInt()
          val reg1 = c.io.dbg_monitor.reg_1.peekInt()
          val reg2 = c.io.dbg_monitor.reg_2.peekInt()
          val reg3 = c.io.dbg_monitor.reg_3.peekInt()
          val reg4 = c.io.dbg_monitor.reg_4.peekInt()
          val reg5 = c.io.dbg_monitor.reg_5.peekInt()
          val reg6 = c.io.dbg_monitor.reg_6.peekInt()
          val reg7 = c.io.dbg_monitor.reg_7.peekInt()
          val reg8 = c.io.dbg_monitor.reg_8.peekInt()
          val reg9 = c.io.dbg_monitor.reg_9.peekInt()
          val reg10 = c.io.dbg_monitor.reg_10.peekInt()
          val reg11 = c.io.dbg_monitor.reg_11.peekInt()
          val reg12 = c.io.dbg_monitor.reg_12.peekInt()
          val reg13 = c.io.dbg_monitor.reg_13.peekInt()
          val reg14 = c.io.dbg_monitor.reg_14.peekInt()
          val reg15 = c.io.dbg_monitor.reg_15.peekInt()
          val reg16 = c.io.dbg_monitor.reg_16.peekInt()
          val reg17 = c.io.dbg_monitor.reg_17.peekInt()
          val reg18 = c.io.dbg_monitor.reg_18.peekInt()
          val reg19 = c.io.dbg_monitor.reg_19.peekInt()
          val reg20 = c.io.dbg_monitor.reg_20.peekInt()
          val reg21 = c.io.dbg_monitor.reg_21.peekInt()
          val reg22 = c.io.dbg_monitor.reg_22.peekInt()
          val reg23 = c.io.dbg_monitor.reg_23.peekInt()
          val reg24 = c.io.dbg_monitor.reg_24.peekInt()
          val reg25 = c.io.dbg_monitor.reg_25.peekInt()
          val reg26 = c.io.dbg_monitor.reg_26.peekInt()
          val reg27 = c.io.dbg_monitor.reg_27.peekInt()
          val reg28 = c.io.dbg_monitor.reg_28.peekInt()
          val reg29 = c.io.dbg_monitor.reg_29.peekInt()
          val reg30 = c.io.dbg_monitor.reg_30.peekInt()
          val reg31 = c.io.dbg_monitor.reg_31.peekInt()

          writer.printf("pc    : %x\n".format(pc))
          writer.printf("reg0  : %d\n".format(reg0))
          writer.printf("reg1  : %d\n".format(reg1))
          writer.printf("reg2  : %d\n".format(reg2))
          writer.printf("reg3  : %d\n".format(reg3))
          writer.printf("reg4  : %d\n".format(reg4))
          writer.printf("reg5  : %d\n".format(reg5))
          writer.printf("reg6  : %d\n".format(reg6))
          writer.printf("reg7  : %d\n".format(reg7))
          writer.printf("reg8  : %d\n".format(reg8))
          writer.printf("reg9  : %d\n".format(reg9))
          writer.printf("reg10 : %d\n".format(reg10))
          writer.printf("reg11 : %d\n".format(reg11))
          writer.printf("reg12 : %d\n".format(reg12))
          writer.printf("reg13 : %d\n".format(reg13))
          writer.printf("reg14 : %d\n".format(reg14))
          writer.printf("reg15 : %d\n".format(reg15))
          writer.printf("reg16 : %d\n".format(reg16))
          writer.printf("reg17 : %d\n".format(reg17))
          writer.printf("reg18 : %d\n".format(reg18))
          writer.printf("reg19 : %d\n".format(reg19))
          writer.printf("reg20 : %d\n".format(reg20))
          writer.printf("reg21 : %d\n".format(reg21))
          writer.printf("reg22 : %d\n".format(reg22))
          writer.printf("reg23 : %d\n".format(reg23))
          writer.printf("reg24 : %d\n".format(reg24))
          writer.printf("reg25 : %d\n".format(reg25))
          writer.printf("reg26 : %d\n".format(reg26))
          writer.printf("reg27 : %d\n".format(reg27))
          writer.printf("reg28 : %d\n".format(reg28))
          writer.printf("reg29 : %d\n".format(reg29))
          writer.printf("reg30 : %d\n".format(reg30))
          writer.printf("reg31 : %d\n".format(reg31))

          c.clock.step(1)
        }
    }
    writer.close()

    println(
      "\n------------------------------  end ------------------------------\n"
    )
  }
}
