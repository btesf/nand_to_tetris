package com.bereket.compiler

enum MemorySegment {
    CONST ("constant"),
    ARG ("argument"), LOCAL ("local"),
    STATIC ("static"), THIS ("this"),
    THAT ("that"), POINTER ("pointer"),
    TEMP ("temp")

    private MemorySegment(String name){this.name = name}

    private String name

    String getName() {
        return name
    }
}