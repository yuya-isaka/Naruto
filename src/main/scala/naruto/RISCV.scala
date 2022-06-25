package naruto

import chisel3._
import chisel3.util._
import Consts._

// ===================================================================================================

class RISCV[Conf <: RVConfig](conf: Conf) extends Module {

  // -----------------------------------------------------

  val io = IO(new Bundle {

    // 命令メモリ
    val iData = Flipped(new IDataBundle)
    // データメモリ
    val dData = Flipped(new DDataBundle)
    // デバッグモニター
    val dbg_monitor = new CpuDebugMonitor(conf)

  })

  // 初期設定 ------------------------------------------

  // PC初期値 ... link.ldで設定されている
  val pc_init = "h8000_0000".U

  // 次PC初期値
  val npc_init = "h8000_0004".U

  // NOP (addi x0, x0, 0) パイプラインの初期値
  val nop_inst = 19.U

  // 制御命令 初期値
  val ctrl_init = Wire((new DecodeResultBundle))
    .decode(nop_inst, InstructionTable())

  // 32 or 64
  val WORD_LEN = conf.xlen
  // 5 or 6
  val ADDR_LEN = log2Ceil(conf.xlen)

  val word_default = UInt(WORD_LEN.W)
  val addr_default = UInt(ADDR_LEN.W)
  val word_init = 0.U(WORD_LEN.W)
  val addr_init = 0.U(ADDR_LEN.W)
  val imm_init = 0.S(WORD_LEN.W)

  // -----------------------------------------------------

  // レジスタ設定 ------------------------------------------

  // 3（IFから受け取る命令に関するもの）
  // ・IDのプログラムカウンタ
  // ・IDの次プログラムカウンタ
  // ・IDのプログラムカウンタに伴う命令
  val id_pc = RegInit(word_default, pc_init)
  val id_npc = RegInit(word_default, npc_init)
  val id_inst = RegInit(word_default, nop_inst)

  // 7（IDから受け取るデコード結果と、IFからの引き継ぎ）
  // ・EXのプログラムカウンタ
  // ・EXの次プログラムカウンタ
  // ・EXのプログラムカウンタに伴う命令
  // ・EXの制御信号（命令に伴う処理を制御するもの）
  // ・命令内のrs1, rs2
  // ・命令内のrd
  // ・命令内のrs1, rs2の参照データ
  val ex_pc = RegInit(word_default, pc_init)
  val ex_npc = RegInit(word_default, npc_init)
  val ex_inst = RegInit(word_default, nop_inst)
  val ex_ctrl = RegInit(ctrl_init)
  val ex_reg_raddr = RegInit(VecInit(addr_init, addr_init))
  val ex_reg_waddr = RegInit(addr_init)
  val ex_rs = RegInit(VecInit(word_init, word_init))

  // 8（EXから受け取る実行結果と、IDとIFからの引き継ぎ）
  // ・MEMのプログラムカウンタ
  // ・MEMの次プログラムカウンタ
  // ・MEMの制御信号（命令に伴う処理を制御）
  // ・命令内のrd
  // ・命令内のrs1, rs2の参照データ
  // ・命令内の即値(EXでやってるの変)
  // ・EXの計算結果
  // ・EXの比較結果
  val mem_pc = RegInit(word_default, pc_init)
  val mem_npc = RegInit(word_default, npc_init)
  val mem_ctrl = RegInit(ctrl_init)
  val mem_reg_waddr = RegInit(addr_init)
  val mem_rs = RegInit(VecInit(word_init, word_init))
  val mem_imm = RegInit(imm_init)
  val mem_alu_out = RegInit(word_init)
  val mem_alu_cmp_out = RegInit(false.B)

  // 5（MEMから受け取るレジスタ書き込み結果と、IF,ID,EXからの引き継ぎ）
  // ・WBの次プログラムカウンタ
  // ・WBの制御信号（命令に従う処理を制御）
  // ・命令内のrd
  // ・EXの計算結果
  // ・MEMのロード結果
  val wb_npc = RegInit(word_default, npc_init)
  val wb_ctrl = RegInit(ctrl_init)
  // これ名前変えたいね
  val wb_reg_waddr = RegInit(addr_init)
  val wb_alu_out = RegInit(word_init)
  val wb_dData_readData = RegInit(word_init)

  // ------------------------------------------------------

  // データハザード、分岐ハザード -------------------------------

  // 組み合わせ回路
  // データハザードある？ない？
  val load_stall = Wire(Bool())

  // 組み合わせ回路
  // 分岐ある？ない？
  val jump_flush = Wire(Bool())

  // ---------------------------------------------------------

  // PC ------------------------------------------------------------------------------------

  // プログラム・カウンタ(アドレス0x8000_0000から開始する)
  // 命令開始位置
  val pc = RegInit(word_default, pc_init)
  // 組み合わせ回路
  // 次命令開始位置 (PCを更新したら、自動的に更新)
  val npc = WireInit(word_default, npc_init)
  npc := pc + 4.U

  // ------------------------------------------------------------------------------------

  // Instruction Fetch ------------------------------------------------------------------------------------

  io.iData.addr := pc

  // ------------------------------------------------------------------------------------

  // Instruction Decode ------------------------------------------------------------------------------------

  // データハザードなし and 分岐なし
  //                              -> IDステージに命令移動
  // データハザードあり
  //                              -> フラッシュ (一回待てば良い)
  // 分岐あり
  //                              -> フラッシュ (ジャンプするから要らなくなる)
  //  PC
  //  次PC
  //  機械語
  when(!load_stall && !jump_flush) {
    id_pc := pc
    id_npc := npc
    id_inst := io.iData.inst
  }.elsewhen(jump_flush) {
    id_pc := pc_init
    id_npc := npc_init
    id_inst := nop_inst
  }

  // 命令(機械語)がわかったら、命令に関する情報をまとめる
  //  inst 命令コード
  //  rd   宛先レジスタ・アドレス(レジスタ番号)
  //  rs1  元レジスタ1・アドレス
  //  rs2  元レジスタ2・アドレス
  val id_inst_arange: ArangeInstructionBundle = ArangeInstruction(id_inst)

  // 命令(機械語)がわかったら、その命令に対応した制御信号を生成する。
  // あとはその制御信号に従って記述
  val id_ctrl: DecodeResultBundle =
    Wire(new DecodeResultBundle).decode(id_inst_arange.inst, InstructionTable())

  // レジスタアドレス
  val id_raddr1 = id_inst_arange.rs1
  val id_raddr2 = id_inst_arange.rs2
  val id_waddr = id_inst_arange.rd
  // 読み出しアドレスをまとめて扱う
  val id_raddr: Seq[UInt] = Vector(id_raddr1, id_raddr2)

  // -----------------------------------------------------------

  val rf = new RegFile

  // レジスタから読み込み
  val id_rs: Seq[UInt] = id_raddr.map(rf.read _)

  // 組み合わせ回路
  // 実行ステージでロード命令 and ロードしたいアドレスと読み込みアドレスが一緒
  // 実行ステージのロード命令が終わってから、読み込みたい（データハザード）
  //                                                                                  -> ストール
  load_stall := ((id_raddr(0) === ex_reg_waddr || id_raddr(1) === ex_reg_waddr)
    && (ex_ctrl.mem === Y) && (ex_ctrl.mem_cmd === M_XRD))

  // ------------------------------------------------------------------------------------

  // EXE ------------------------------------------------------------------------------------

  // データハザードなし and 分岐なし
  //                              -> EXEステージに命令移動
  // データハザードあり
  //                              -> フラッシュ (１回待てば解消)
  // 分岐あり
  //                              -> フラッシュ (ジャンプするから要らない)
  //  PC
  //  次PC
  //  機械語
  //  制御信号
  //  読み込みアドレス（rs1, rs2)
  //  書き込みアドレス (rd)
  //  読み出した値 (rs1, rs2)
  when(!load_stall && !jump_flush) {
    ex_pc := id_pc
    ex_npc := id_npc
    ex_inst := id_inst_arange.inst
    ex_ctrl := id_ctrl
    ex_reg_raddr := id_raddr
    ex_reg_waddr := id_waddr
    ex_rs := id_rs
  }.otherwise {
    ex_pc := pc_init
    ex_npc := npc_init
    ex_ctrl := ctrl_init
    ex_inst := nop_inst
    ex_reg_raddr := VecInit(addr_init, addr_init)
    ex_reg_waddr := addr_init
    ex_rs := VecInit(word_init, word_init)
  }

  // バイパス（フォワーディング）するレジスタ値 -----------------------------------------------------

  val ex_rs_bypassed: Seq[UInt] =
    // rs1とrs2を調べる
    for (i <- 0 until id_raddr.size)
      yield MuxCase(
        ex_rs(i),
        Seq(
          // 『実行ステージで読み込むレジスタアドレス』と『メモリステージでわかる書き込む先のレジスタアドレス(rd)』が一緒、だったら
          // -> メモリステージの結果
          ((ex_reg_raddr(i) =/= 0.U)
            && (ex_reg_raddr(i) === mem_reg_waddr)
            && (mem_ctrl.wxd === Y))
            -> mem_alu_out,
          // 『実行ステージで読み込むレジスタアドレス』　と　『ロード命令で読み込んだ値を書き込む先のレジスタアドレス』が一緒、だったら
          // -> データメモリの読み込みデータ
          ((ex_reg_raddr(i) =/= 0.U)
            && (ex_reg_raddr(i) === wb_reg_waddr)
            && (wb_ctrl.wxd === Y)
            && (wb_ctrl.mem === Y))
            -> io.dData.readData
        )
      )

  // 即値生成 --------------------------------------------------------------------------------

  val ex_imm = ImmGen(ex_ctrl.sel_imm, ex_inst)

  // コントローラによって、ALUに入力する値を決定 --------------------------------------------------

  val ex_op1 =
    MuxLookup(
      // コントローラ
      ex_ctrl.sel_alu1,
      word_init,
      Seq(
        // rs1
        OP1_RS1 -> ex_rs_bypassed(0),
        // PC
        OP1_PC -> ex_pc
      )
    )

  val ex_op2 = MuxLookup(
    // コントローラ
    ex_ctrl.sel_alu2,
    word_init,
    Seq(
      // rs2
      OP2_RS2 -> ex_rs_bypassed(1),
      // 即値
      OP2_IMM -> ex_imm.asUInt,
      // JAL命令の時, 4
      OP2_SIZE -> 4.U(WORD_LEN.W)
    )
  )

  // 演算 -----------------------------------------------------------------------------------------

  val alu = Module(new ALU)
  alu.io.fn := ex_ctrl.alu_fn
  alu.io.in1 := ex_op1
  alu.io.in2 := ex_op2

  // Memory Access ステージ =============================================================================

  // 分岐なし
  //                              -> MEMステージに命令移動
  // 分岐あり
  //                              -> フラッシュ (ジャンプするからいらなくなる)
  //  PC
  //  次PC
  //  制御信号
  //  バイパスされた値
  //  書き込みアドレス (rd)
  //  即値
  //  計算結果
  //  比較結果
  when(!jump_flush) {
    mem_pc := ex_pc
    mem_npc := ex_npc
    mem_ctrl := ex_ctrl
    mem_rs := ex_rs_bypassed
    mem_reg_waddr := ex_reg_waddr
    mem_imm := ex_imm
    mem_alu_out := alu.io.alu_out
    mem_alu_cmp_out := alu.io.cmp_out
  }.otherwise {
    mem_pc := pc_init
    mem_npc := npc_init
    mem_ctrl := ctrl_init
    mem_rs := VecInit(word_init, word_init)
    mem_reg_waddr := addr_init
    mem_imm := imm_init
    mem_alu_out := word_init
    mem_alu_cmp_out := false.B
  }

  // ----------------------------------------------------------------------------------------------

  // Data Memoryに計算結果をアドレスとして代入（ロード命令、ストア命令なら、ここにアドレスが入っているから)
  io.dData.addr := mem_alu_out
  // メモリ書き込み有りかどうか判断
  io.dData.writeEnable := mem_ctrl.mem_cmd === M_XWR
  // mem_typeからメモリアクセスサイズ確認
  io.dData.size := mem_ctrl.mem_type
  // データメモリの読み込みデータ
  io.dData.writeData := mem_rs(1)

  // 分岐ハザードの解決
  // (分岐命令 and 比較結果あり) or ジャンプあり
  //                                            -> 分岐あり
  jump_flush := (((mem_ctrl.branch === Y) && mem_alu_cmp_out) || (mem_ctrl.jal === Y) || (mem_ctrl.jalr === Y))

  // WB =============================================================================================

  // 分岐してもやること変わらない
  // データハザードしてもやること変わらない
  // ただ俺は書き込むだけ
  wb_npc := mem_npc
  wb_ctrl := mem_ctrl
  wb_reg_waddr := mem_reg_waddr
  wb_alu_out := mem_alu_out
  wb_dData_readData := io.dData.readData

  // 立ち下がりエッジで書き込み
  val revClock = Wire(new Clock)
  revClock := (~(clock.asBool)).asClock
  withClock(revClock) {
    val rf_wen = wb_ctrl.wxd
    val rf_waddr = wb_reg_waddr
    val rf_wdata = MuxCase(
      wb_alu_out,
      Seq(
        // ジャンプ命令
        // ジャンプ命令 (jalrの時、rdにnpc格納)
        (wb_ctrl.jalr === Y) -> wb_npc,
        // メモリアクセス命令
        // メモリアクセス命令 (ロード命令)
        (wb_ctrl.mem === Y) -> wb_dData_readData
      )
    )
    when(rf_wen) { rf.write(rf_waddr, rf_wdata) }
  }

  // PC更新 -----------------------------------------------------------------------

  // データハザードなし
  //                  -> PC更新
  when(!load_stall) {
    pc := MuxCase(
      // 次の命令
      npc,
      Seq(
        // (分岐命令, 分岐OK) or ジャンプOK
        //                                  -> PC相対アドレッシングモード
        (((mem_ctrl.branch === Y) && mem_alu_cmp_out) || mem_ctrl.jal === Y) -> (mem_pc + mem_imm.asUInt),
        // ジャンプOK
        //                                  -> 計算した結果にジャンプ
        (mem_ctrl.jalr === Y) -> mem_alu_out
      )
    )
  }

  // Debug Monitor --------------------------------------------------------------

  io.dbg_monitor.pc := pc
  io.dbg_monitor.reg_0 := rf.read(0.U)
  io.dbg_monitor.reg_1 := rf.read(1.U)
  io.dbg_monitor.reg_2 := rf.read(2.U)
  io.dbg_monitor.reg_3 := rf.read(3.U)
  io.dbg_monitor.reg_4 := rf.read(4.U)
  io.dbg_monitor.reg_5 := rf.read(5.U)
  io.dbg_monitor.reg_6 := rf.read(6.U)
  io.dbg_monitor.reg_7 := rf.read(7.U)
  io.dbg_monitor.reg_8 := rf.read(8.U)
  io.dbg_monitor.reg_9 := rf.read(9.U)
  io.dbg_monitor.reg_10 := rf.read(10.U)
  io.dbg_monitor.reg_11 := rf.read(11.U)
  io.dbg_monitor.reg_12 := rf.read(12.U)
  io.dbg_monitor.reg_13 := rf.read(13.U)
  io.dbg_monitor.reg_14 := rf.read(14.U)
  io.dbg_monitor.reg_15 := rf.read(15.U)
  io.dbg_monitor.reg_16 := rf.read(16.U)
  io.dbg_monitor.reg_17 := rf.read(17.U)
  io.dbg_monitor.reg_18 := rf.read(18.U)
  io.dbg_monitor.reg_19 := rf.read(19.U)
  io.dbg_monitor.reg_20 := rf.read(20.U)
  io.dbg_monitor.reg_21 := rf.read(21.U)
  io.dbg_monitor.reg_22 := rf.read(22.U)
  io.dbg_monitor.reg_23 := rf.read(23.U)
  io.dbg_monitor.reg_24 := rf.read(24.U)
  io.dbg_monitor.reg_25 := rf.read(25.U)
  io.dbg_monitor.reg_26 := rf.read(26.U)
  io.dbg_monitor.reg_27 := rf.read(27.U)
  io.dbg_monitor.reg_28 := rf.read(28.U)
  io.dbg_monitor.reg_29 := rf.read(29.U)
  io.dbg_monitor.reg_30 := rf.read(30.U)
  io.dbg_monitor.reg_31 := rf.read(31.U)
}
