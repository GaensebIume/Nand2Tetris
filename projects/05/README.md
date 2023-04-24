# PROJECT 5
## Gates
### Memory
Maps memory addresses of RAM, screen, and KBD.
Requires 16-bit data stream, load bit, 15 bit address stream.
Outputs 16-bit data stream.
### CPU
Performs calculations using ALU and implements logic for loading data from registers.
Requires 16-bit input stream, 16-bit instruction stream and reset bit.
Manipulates memory address M
Outputs content of M [16-bit], address in memory of M [15-bit], bit stating whether to write to M or not and 15-bit stream (memory address of next instruction set)
### Computer
Combines CPU, Memory and RAM.
Requires one bit to reset the computer.
No output.