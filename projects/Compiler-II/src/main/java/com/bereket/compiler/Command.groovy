package com.bereket.compiler

public enum Command {

    ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT

    public static Command getCommandForSymbol(String symbol){
        switch (symbol){
            //"+", "-", "*", "/", "&", "|", "<", ">", "=", "&lt;", "&gt;", "&amp;"
           case "+": return ADD
           case "~": return NOT
           case "-": return SUB
           case ">":
           case "&gt;":
                return GT
           case "<":
           case "&lt;":
                return LT
           case "=": return EQ
           case "&":
           case "&amp;":
                return AND
           case "|": return OR
            default: return null
        }
    }
}