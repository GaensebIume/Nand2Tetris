
//function Sys.init 0
@Sys.init
//push constant 4
@4
D=A
@SP
A=M
M=D
@SP
M=M+1
//call Main.fibonacci 1   
@Main.fibonacci
@LCL
D=M
@SP
A=M
M=D
@SP
M=M+1
@ARG
D=M
@SP
A=M
M=D
@SP
M=M+1
@THIS
D=M
@SP
A=M
M=D
@SP
M=M+1
@THAT
D=M
@SP
A=M
M=D
@SP
M=M+1
@6
A=M
@SP
D=A
A=M-D
@SP
A=M
@LCL
D=A(Main.fibonacci)
0;JMP
//label WHILE
@label
//goto WHILE              (goto)
0;JMP