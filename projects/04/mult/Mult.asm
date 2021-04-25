// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

@i // variable "i" is a counter - which will be created at the next register in the memory starting from 16
M=0 //intialise i to zero
@sum //to collect sum values 
M=0

(LOOP)
    //if(R1 < i) jump to end (condition for iteration)
    @1
    D=M //D Contains the 2nd operand (i.e. R1) in the equation R0 * R1 = R2
    @i
    D=D-M //R1 - i
    @STORE
    D;JLE // jump to End if R1 - i < 0

    //increment i by 1
    @i
    M=M+1

    //do sum=sum+R0
    @sum 
    D=M
    @0
    D=D+M
    @sum
    M=D
    @LOOP
    0;JMP
(STORE)
    //store @sum to R2
    @sum
    D=M
    @2
    M=D
(END)    
    @END
    0;JMP  // Infinite loop