package com.bereket.compiler

class IdentifierMetadata {

    IdentifierCategory category;
    IdentifierOccurrence occurrence;
    int index;

    public IdentifierMetadata(IdentifierCategory category,
                              IdentifierOccurrence occurrence, int index = 0){
        this.category = category;
        this.occurrence = occurrence
        this.index = index
    }
}
