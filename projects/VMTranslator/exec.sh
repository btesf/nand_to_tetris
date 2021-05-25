#!/usr/bin/env bash
export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/07/StackArithmetic/SimpleAdd/'
#export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/07/StackArithmetic/StackTest/'
#export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/07/MemoryAccess/StaticTest/'
#export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/07/MemoryAccess/BasicTest/'
#export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/08/ProgramFlow/BasicLoop/'
#export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/08/ProgramFlow/FibonacciSeries/'
mvn exec:java -Dexec.mainClass="VMTranslator" -Dexec.args="$JAVA_PROGRAM_ARGS"
ls -l $JAVA_PROGRAM_ARGS