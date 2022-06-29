%: %.c
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c -o start.o start.S
	riscv64-unknown-elf-gcc -march=rv32i -mabi=ilp32 -c -o $@.o $<
	riscv64-unknown-elf-ld $@.o start.o -b elf32-littleriscv -T link.ld -static -o $@.elf
	riscv64-unknown-elf-objdump -d $@.elf > $@.dump
	riscv64-unknown-elf-objcopy -O binary $@.elf $@.bin
	od -An -tx4 -w4 -v $@.bin > $@.hex
	rm -f start.o
	rm -f $@.o
	rm -f $@.elf
	rm -f $@
	rm -f $@.bin

clean:
	$(RM) *.o *.elf *.bin