run:
	riscv64-unknown-elf-gcc -march=rv32g -mabi=ilp32 -nostartfiles -Tlink.ld hello.s -o hello
	riscv64-unknown-elf-objdump -d hello > hello.dump
	riscv64-unknown-elf-objcopy -O binary hello hello.bin
	od -t x4 --endian=little -An hello.bin

clean:
	$(RM) *.o *.elf *.bin