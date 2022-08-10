%: %.c
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c -o startup.o startup.S
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c -o $@.o $<
	riscv64-unknown-elf-ld $@.o startup.o -b elf32-littleriscv -T link.ld -static -o $@.elf
	riscv64-unknown-elf-objdump -D $@.elf > $@.dump
	riscv64-unknown-elf-objcopy -O binary $@.elf $@.bin
	od -t x4 --endian=little -An $@.bin

clean:
	$(RM) *.o *.elf *.bin