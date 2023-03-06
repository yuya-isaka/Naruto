# naruto

The simple 32-bit RISC-V core written in Chisel.

## Current Status
Running on Basys3 FPGA Evaluation Board

### Architecture
* 5-stage pipelined operation (fetch, decode, execute, memory access, write back)
* Instruction set: RV32I
* Omit privileged instructions and interrupt area


### References

* https://github.com/horie-t/homemade-riscv-en

* https://github.com/chadyuu/riscv-chisel-book
