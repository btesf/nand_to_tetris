package com.bereket.compiler

enum TokenType {

    KEYWORD("keyword"),
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant")

    private TokenType(String tagName){ this.tagName = tagName }
    private String tagName;

    private final static KEYWORD_TAG = "keyword"
    private final static SYMBOL_TAG = "symbol"
    private final static IDENTIFIER_TAG = "identifier"
    private final static INT_CONST_TAG = "integerConstant"
    private final static STRING_CONST_TAG = "stringConstant"

    public String getTagName(){
        return tagName;
    }

    public static TokenType getTokenType(String tagName){
        switch(tagName){
            case KEYWORD_TAG: return KEYWORD
            case SYMBOL_TAG: return SYMBOL
            case IDENTIFIER_TAG: return IDENTIFIER
            case INT_CONST_TAG: return INT_CONST
            case STRING_CONST_TAG: return STRING_CONST
            default: throw new RuntimeException("Cannot find matching com.bereket.compiler.TokenType for " + tagName)
        }
    }
}