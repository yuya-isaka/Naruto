package naruto

object Program {

  // Hello world --------------------------------------------------------

  // hello.dumpを見る限り、ノイマン型アーキテクチャにしても問題なさそう

  // 命令
  // val iData_Program = List(0x00001417L, 0x00040413L, 0x00040503L, 0x00140413L,
  //   0x00050c63L, 0x008000efL, 0xff1ff06fL, 0x100002b7L, 0x00a28023L,
  //   0x00008067L, 0x0000006fL)

  // データ
  // val dData_Program = List(0x6c6c6548L, 0x77202c6fL, 0x646c726fL, 0x00000a21L)

  // fib ----------------------------------------------------------------

  // 命令
  val iData_Program = List(
     0xfe010113L, 0x00112e23L, 0x00812c23L, 0x00912a23L,
     0x02010413L, 0xfea42623L, 0xfec42703L, 0x00100793L,
     0x00e7c663L, 0x00100793L, 0x0300006fL, 0xfec42783L,
     0xfff78793L, 0x00078513L, 0xfc9ff0efL, 0x00050493L,
     0xfec42783L, 0xffe78793L, 0x00078513L, 0xfb5ff0efL,
     0x00050793L, 0x00f487b3L, 0x00078513L, 0x01c12083L,
     0x01812403L, 0x01412483L, 0x02010113L, 0x00008067L,
     0xff010113L, 0x00112623L, 0x00812423L, 0x01010413L,
     0x00900513L, 0xf7dff0efL, 0x0000006fL, 0xfe5ff0efL,
     0x0000006fL
  )

  // データ
  val dData_Program = List()
}
