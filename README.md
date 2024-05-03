# Naruto

Naruto is a simple 32-bit RISC-V core implemented in Chisel.

## Current Status

Currently running on the Basys3 FPGA Evaluation Board.

## Architecture

- **Pipelining**: 5-stage (fetch, decode, execute, memory access, write back)
- **Instruction Set**: RV32I
- **Privileges**: Excludes privileged instructions and interrupt handling

## References

- [Homemade RISC-V](https://github.com/horie-t/homemade-riscv-en)
- [RISC-V Chisel Book](https://github.com/chadyuu/riscv-chisel-book)
