#!/usr/bin/env bash
#export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/07/StackArithmetic/StackTest/'
#export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/07/MemoryAccess/StaticTest/'
export JAVA_PROGRAM_ARGS='/Users/btesfamichael/Documents/nand_to_tetris/projects/07/MemoryAccess/BasicTest/'
mvn exec:java -Dexec.mainClass="VMTranslator" -Dexec.args="$JAVA_PROGRAM_ARGS"
ls -l $JAVA_PROGRAM_ARGS