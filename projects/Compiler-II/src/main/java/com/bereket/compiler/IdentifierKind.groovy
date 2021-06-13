package com.bereket.compiler

enum IdentifierKind {
    STATIC, FIELD, ARG, VAR, NONE

    public MemorySegment getMemorySegment(){
        switch(this){
            case STATIC:
                return MemorySegment.STATIC
            case FIELD:
                return MemorySegment.THIS
            case ARG:
                return MemorySegment.ARG
            case VAR:
                return MemorySegment.LOCAL
            default: throw new RuntimeException("Memory segment cannot be determined")
        }
    }
}