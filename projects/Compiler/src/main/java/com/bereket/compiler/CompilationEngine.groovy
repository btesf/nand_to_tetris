package com.bereket.compiler

class CompilationEngine {

    private FileOutputStream fileOutputStream
    private String xmlSource = "";
    private List<TokenInformation> tokenizedTokens = new ArrayList<>();
    private List<String> programStructure = new ArrayList<>()
    int currentIndex = 0;

    public CompilationEngine(String inputFileName, String outputFileName){
        File xmlFile = new File(inputFileName)
        xmlSource = new String(xmlFile.readBytes());
        fileOutputStream = new FileOutputStream(outputFileName)
    }

    //for com.bereket.compiler.Test purposes
    public CompilationEngine(){

    }

    void compileFile(){
        def rootNode = new XmlSlurper().parseText(xmlSource)
        readXmlToTokenInformation(rootNode, tokenizedTokens)


        compileClass();

        for(TokenInformation tokenInformation: tokenizedTokens){
            println tokenInformation.toString()
        }
    }

    static void readXmlToTokenInformation(def tokens, List<TokenInformation> tokenizedTokens){
        def childNodes= tokens.childNodes()
        while(childNodes.hasNext()){
            def node = childNodes.next()
            String body = node.children.get(0).toString().trim()
            tokenizedTokens.add(new TokenInformation(body,TokenType.getTokenType(node.name.toString().trim())))
        }
    }

    //'class' className '{' classVarDec* subroutineDec* '}'
    public void compileClass(){
        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "class") throw new RuntimeException("Class definition should begin with 'class'")
        programStructure.add("<class>")
        programStructure.add(createXmlNode("keyword", tokenInformation.token))
        //className
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("Identifier expected after class name; found: " + tokenInformation.token)
        programStructure.add(createXmlNode("identifier", tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "{") throw new RuntimeException("'{' expected  after class name")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileClassVarDec();
        compileSubroutineDec()
        //class ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "}") throw new RuntimeException("Class should be closed using '}'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</class>")
    }

    private String createXmlNode(String nodeName, String value){
        return "<${nodeName}>${value}</${nodeName}>".toString()
    }

    //classVardDec = ('static' | 'field') type varName (',' varName)* ';'
    private void compileClassVarDec(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "static" && tokenInformation.token != "field")) return;
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add("<classVarDec>")
        programStructure.add(createXmlNode("keyword", tokenInformation.token)) //field | static
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if(tokenInformation.tokenType == TokenType.KEYWORD) {
            if(!["char", "int", "boolean"].contains(tokenInformation.token)) throw new RuntimeException("Unsupported variable type: '${tokenInformation.token}'. It should be one of 'int', 'char' or 'boolean'")
            programStructure.add(createXmlNode("keyword", tokenInformation.token))
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){
            programStructure.add(createXmlNode("identifier", tokenInformation.token))
        } else throw new RuntimeException("Invalid type '${tokenInformation.token}'")
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")
        programStructure.add(createXmlNode("identifier", tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        //if the next token is comma (,) read the next variable declaration
        while(tokenInformation.token == ","){
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.tokenType != TokenType.IDENTIFIER)
                throw new RuntimeException("variable list after comma should be an identifier")
            programStructure.add(createXmlNode("identifier", tokenInformation.token))
            tokenInformation = getCurrentTokenAndAdvance()
        }
        if(tokenInformation.token != ";")
            throw new RuntimeException("Class variable declaration should terminate with ';'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</classVarDec>")
        compileClassVarDec()
    }

    //('constructor' | 'function' | 'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    public void compileSubroutineDec(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "constructor" && tokenInformation.token != "function" && tokenInformation.token != "method")) return;
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add("<subroutineDec>")
        programStructure.add(createXmlNode("keyword", tokenInformation.token)) //constructor | function | method
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if(tokenInformation.tokenType == TokenType.KEYWORD) {
            if(tokenInformation.token != "void") throw new RuntimeException("Invalid return type '${tokenInformation.token}' for method. It can only be 'void' or type")
            programStructure.add(createXmlNode("keyword", tokenInformation.token))
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){
            programStructure.add(createXmlNode("identifier", tokenInformation.token))
        } else throw new RuntimeException("Invalid return type '${tokenInformation.token}'")
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("method/function name is not valid : ${tokenInformation.token}")
        programStructure.add(createXmlNode("identifier", tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        //next is '('
        if(tokenInformation.token != "(") throw new RuntimeException("Method parameter list should start with '('. Found ${tokenInformation.token}")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileParameterList();
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ")") throw new RuntimeException("Method parameter list should end with ')'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileSubroutine()
        programStructure.add("</subroutineDec>")
        compileClassVarDec()
    }

    public void compileSubroutine(){

    }

    //((type varName) (',' type varName)*)?
    public void compileParameterList(){
        programStructure.add("<parameterList>")
        if(readParameter()){
            TokenInformation tokenInformation = getCurrentTokenAndAdvance()
            //if the next token is comma (,) read the next variable declaration
            while(tokenInformation != null && tokenInformation.token == ","){
                programStructure.add(createXmlNode("symbol", tokenInformation.token))
                if(readParameter()){
                    tokenInformation = getCurrentTokenAndAdvance()
                }
            }
        }
        programStructure.add("</parameterList>")
    }

    public boolean readParameter(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null) false;
        String type = null;
        if(tokenInformation.tokenType == TokenType.KEYWORD) {
            if(!["char", "int", "boolean"].contains(tokenInformation.token)) return false; //throw new RuntimeException("Unsupported variable type: '${tokenInformation.token}'. It should be one of 'int', 'char' or 'boolean'")
            type = "keyword"
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){
            type = "identifier"
        } else return false
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode(type, tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")
        programStructure.add(createXmlNode("identifier", tokenInformation.token))
        return true;
    }

    public void compileVarDec(){

    }

    public void compileStatements(){

    }

    public void compileDo(){

    }

    public void compileLet(){

    }

    public void compileWhile(){

    }

    public void compileReturn(){

    }

    public void compileIf(){

    }

    public void compileExpression(){

    }

    public void compileTerm(){

    }

    public void compileExpressionList(){

    }

    public void close(){
        fileOutputStream.close()
    }

    public TokenInformation getCurrentTokenAndAdvance(){
        if(currentIndex >= tokenizedTokens.size()) return null; // throw new RuntimeException("Reached out of bound - cannot go to next token.")
        return tokenizedTokens.get(currentIndex++)
    }

    public TokenInformation getCurrentToken(){
        if(currentIndex >= tokenizedTokens.size()) return null;
        return tokenizedTokens.get(currentIndex)
    }

    public TokenInformation lookupNextTokenInformation(){
        if((currentIndex + 1) < tokenizedTokens.size()) {
            TokenInformation tokenInformation = tokenizedTokens.get(currentIndex + 1)
            return tokenInformation
        }
        return null
    }

    public TokenInformation lookupPreviousTokenInformation(){
        return tokenizedTokens.get(currentIndex - 1)
    }

    private KeyWord determineKeywordFromToken(String token){
        switch(token){
            case "class":
                return KeyWord.CLASS;
            case "method":
                return KeyWord.METHOD;
            case "function":
                return KeyWord.FUNCTION;
            case "constructor":
                return KeyWord.CONSTRUCTOR;
            case "int":
                return KeyWord.INT;
            case "boolean":
                return KeyWord.BOOLEAN;
            case "char":
                return KeyWord.CHAR;
            case "void":
                return KeyWord.VOID;
            case "var":
                return KeyWord.VAR;
            case "static":
                return KeyWord.STATIC;
            case "field":
                return KeyWord.FIELD;
            case "let":
                return KeyWord.LET;
            case "do":
                return KeyWord.DO;
            case "if":
                return KeyWord.IF;
            case "else":
                return KeyWord.ELSE;
            case "while":
                return KeyWord.WHILE;
            case "return":
                return KeyWord.RETURN;
            case "true":
                return KeyWord.TRUE;
            case "false":
                return KeyWord.FALSE;
            case "null":
                return KeyWord.NULL;
            case "this":
                return KeyWord.THIS;
            default:
                throw new RuntimeException("Unknown keyword")

        }
    }

    List<TokenInformation> getTokenizedTokens() {
        return tokenizedTokens
    }

    void setTokenizedTokens(List<TokenInformation> tokenizedTokens) {
        this.tokenizedTokens = tokenizedTokens
    }

    List<String> getProgramStructure() {
        return programStructure
    }
}
