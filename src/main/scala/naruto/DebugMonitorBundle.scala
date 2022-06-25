package naruto

import chisel3._
import chisel3.util._

// デバッグバンドル ==========================================================================

class CpuDebugMonitor[Conf <: RVConfig](conf: Conf) extends Bundle {
  // PC
  val pc =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))

  // Register 0-31
  val reg_0 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_1 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_2 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_3 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_4 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_5 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_6 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_7 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_8 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_9 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_10 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_11 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_12 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_13 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_14 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_15 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_16 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_17 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_18 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_19 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_20 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_21 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_22 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_23 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_24 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_25 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_26 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_27 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_28 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_29 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_30 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
  val reg_31 =
    if (conf.debug == true) Output(UInt(conf.xlen.W)) else Output(UInt(0.W))
}
