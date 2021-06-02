enum TokenType {
    KEYWORD("keyword"),
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant")

    private TokenType(String tagName){ this.tagName = tagName }
    private String tagName;

    public String getTagName(){
        return tagName;
    }
}