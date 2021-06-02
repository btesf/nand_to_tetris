(LOOP)
    //if(R1 < i) jump to end (condition for iteration)
    @1
    D=M //D Contains the 2nd operand (i.e. R1) in the equation R0 * R1 = R2
    D=D-M //R1 - i
    @STORE
    D;JLE // jump to End if R1 - i < 0
    //increment i by 1
    @i
    M=M+1
    //do sum=sum+R0
    D=M
    @0
    D=D+M
    M=D
    @LOOP
    0;JMP
(STORE)
    //store @sum to R2
    D=M
    @2
    M=D
(END)
    @END
    0;JMP  // Infinite loop