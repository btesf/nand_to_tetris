// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

//============= clear=screen=================
(START_CLEAR_DISPLAY)
    @i
    M=0
(LOOP_CLEAR_SCREEN)
    @i
    D=M
    //there are 8192 16-bit words in Hack Screen 
    @8192
    D=A-D
    @END_CLEAR_DISPLAY
    D;JLE

    @i
    D=M
    @SCREEN
    //set address to @SCREEN + @i (where i < 8192)
    A=D+A
    M=0 //write 0 to memory
    @i
    M=M+1
    @LOOP_CLEAR_SCREEN
    0;JEQ
(END_CLEAR_DISPLAY)
    //check keyboard value and jump to screen fill if it has some value
    @KBD
    D=M
    @START_FILL_DISPLAY
    D;JGT

    @END_CLEAR_DISPLAY
    0;JEQ

//============= fill=screen=================
(START_FILL_DISPLAY)
    @i
    M=0
(LOOP_FILL_DISPLAY)
    @i
    D=M
    //there are 8192 16-bit words in Hack Screen 
    @8192
    D=A-D
    @END_FILL_DISPLAY
    D;JLE

    @i
    D=M
    @SCREEN
    //set address to @SCREEN + @i (where i < 8192)
    A=D+A
    M=-1 //write -1 to memory
    @i
    M=M+1
    @LOOP_FILL_DISPLAY
    0;JEQ

(END_FILL_DISPLAY)
    //check keyboard value and jump to screen clear if it is empty
    @KBD
    D=M
    @START_CLEAR_DISPLAY
    D;JEQ
    
    @END_FILL_DISPLAY
    0;JEQ