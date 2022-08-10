# 開発のみ -------------------------------------------

# デバッグ用
debug:
	sbt "testOnly naruto.CpuTopDebug"

# プログラムが終了するか否か (適切な終了PCを設定)
test:
	sbt "testOnly naruto.HexTest"

# 開発 & 本番 ----------------------------------------
run:
	sbt "runMain naruto.Naruto"

# 個人用テスト
testtest:
	sbt "testOnly test.TestTop"