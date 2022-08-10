	.section .text
	.global _start

_start:
	# la sp, sp_top
	jal main

fin:
	j fin
