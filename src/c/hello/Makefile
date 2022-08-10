run:
	riscv64-unknown-elf-gcc -march=rv32g -mabi=ilp32 -nostartfiles -T link.ld hello.s -o hello
	riscv64-unknown-elf-objdump -D hello > hello.dump
	riscv64-unknown-elf-objcopy -O binary hello hello.bin
	od -t x4 --endian=little -An hello.bin

run2:
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c -o startup.o startup.S
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c -o hello_c.o hello_c.c
	riscv64-unknown-elf-ld hello_c.o startup.o -b elf32-littleriscv -T link.ld -static -o hello_c.elf
	riscv64-unknown-elf-objdump -D hello_c.elf > hello_c.dump
	riscv64-unknown-elf-objcopy -O binary hello_c.elf hello_c.bin
	od -t x4 --endian=little -An hello.bin

clean:
	$(RM) *.o *.elf *.bin
