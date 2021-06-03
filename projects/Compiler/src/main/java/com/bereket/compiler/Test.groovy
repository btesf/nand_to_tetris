package com.bereket.compiler

class Test {

    public static void main(String[] args) {
        String abc = "/Users/bereket/Documents/nand2tetris/projects/07/abcd.txt"
        println abc.substring(0, abc.lastIndexOf("/"))

        String destinationDirectory = abc.substring(0, abc.lastIndexOf("/") + 1) //include the last slash
        String destinationFileName = abc.toString().replaceAll(destinationDirectory, "") //only leave the file name with extension
                .replaceAll("[.].*", "")

        println destinationFileName

        final List<String> ARITHMETIC_COMMANDS = ["add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"];
        String[] tokens = "ADD   neg ki".split("\\s+")
        println(ARITHMETIC_COMMANDS.contains(tokens[0].toLowerCase()))
    }
}
