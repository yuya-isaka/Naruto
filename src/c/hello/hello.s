	.section .text
	.equ UART_ADDR, 0x10000000
	.global _start

_start:
	la s0, message

putloop:
	lb a0, 0(s0)
	addi s0, s0, 1
	beqz a0, fin
	jal sendchar
	j putloop

sendchar:
	li t0, UART_ADDR
	sb a0, 0(t0)
	ret

fin:
	j fin

	.section .rodata

message:
	.ascii "Hello, world!\n\0"
