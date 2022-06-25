package naruto

// ベース　-----------------------------------------------------

abstract class RVConfig() {
  val xlen = 32
  val debug = false
}

// 本番 -----------------------------------------------------

case class RV32Config() extends RVConfig {
  override val xlen = 32
  override val debug = false
}

// 開発 -----------------------------------------------------

case class RV32ConfigDebug() extends RVConfig {
  override val xlen = 32
  override val debug = true
}

case class RV64ConfigDebug() extends RVConfig {
  override val xlen = 64
  override val debug = true
}
