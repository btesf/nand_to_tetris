package com.bereket.compiler

class CompilationEngine {

    private FileOutputStream fileOutputStream
    private String xmlSource = "";
    private final List<String> UNARY_OPS = ["-", "~"]
    private final List<String> BINARY_OPS = ["+", "-", "*", "/", "&", "|", "<", ">", "=", "&lt;", "&gt;", "&amp;"]
    private final List<String> KEYWORD_CONSTANTS = ["true", "false", "null", "this"]
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
        compileSubroutineBody()
        programStructure.add("</subroutineDec>")
        compileClassVarDec()
    }

    //'{' varDec* statements '}'
    public void compileSubroutineBody(){

        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation.token != "{") throw new RuntimeException("Subroutine body should begin with '}'")
        programStructure.add("<subroutineBody>")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileVarDec()
        compileStatements()
        tokenInformation = getCurrentToken()
        if(tokenInformation.token != "}") throw new RuntimeException("Subroutine body should end with '}'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</subroutineBody>")
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
            if(!["char", "int", "boolean"].contains(tokenInformation.token)) return false;
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

    //var' type varName (',' varName)* ';'
    public void compileVarDec(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || tokenInformation.token != "var" ) return;
        programStructure.add("<varDec>")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("keyword", tokenInformation.token)) //var
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
            throw new RuntimeException("Variables declaration should terminate with ';'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</varDec>")
        compileVarDec()
    }

    public void compileStatements(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || tokenInformation.tokenType != TokenType.KEYWORD) return;
        switch(tokenInformation.token){
            case "let":
                compileLet();
                break;
            case "if":
                compileIf()
                break;
            case "while":
                compileWhile()
                break;
            case "do":
                compileDo()
                break;
            case "return":
                compileReturn()
                break;
            default: return;
        }
    }

    //'do' compileDo ';'
    public void compileDo(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "do" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        programStructure.add("<doStatement>")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("keyword", tokenInformation.token)) //do
        compileSubroutine()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ";")
            throw new RuntimeException("do expression should terminate with ';'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</doStatement>")
    }

    //'return' (expression)? ';'
    public void compileReturn(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "do" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        programStructure.add("<returnStatement>")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("keyword", tokenInformation.token)) //return
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ";")
            throw new RuntimeException("return expression should terminate with ';'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</returnStatement>")
    }

    //'let' varName ('[' expression ']')? '=' expression ';'
    public void compileLet(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "let" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        programStructure.add("<letStatement>")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("keyword", tokenInformation.token)) //var
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")
        programStructure.add(createXmlNode("identifier", tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token == "["){
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
            compileExpression()
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.token != "]") throw new RuntimeException("No closing ']' found. Got : ${tokenInformation.token}")
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
            tokenInformation = getCurrentTokenAndAdvance()
        }
        if(tokenInformation.token != "=") throw new RuntimeException("Equal sign expected in expression. Found : ${tokenInformation.token}")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ";")
            throw new RuntimeException("Variables declaration should terminate with ';'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</letStatement>")
    }

    //'while' '(' expression ')' '{' statements '}'
    public void compileWhile() {

        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation == null || (tokenInformation.token != "while" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        programStructure.add("<whileStatement>")
        programStructure.add(createXmlNode("keyword", tokenInformation.token))
        //brace
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "(") throw new RuntimeException("While condition expression should begin with '('. Got : ${tokenInformation.token}")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != ")") throw new RuntimeException("While condition expression should end with ')'. Got : ${tokenInformation.token}")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start while condition block")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileStatements()
        //if block ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "}") throw new RuntimeException("while condition block should be closed using '}'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        programStructure.add("</whileStatement>")
    }

    //'if' '(' expression ')' '{' statements '}'
    public void compileIf(){
        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation == null || (tokenInformation.token != "if" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        programStructure.add("<ifStatement>")
        programStructure.add(createXmlNode("keyword", tokenInformation.token))
        //brace
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "(") throw new RuntimeException("If condition should begin with '('. Got : ${tokenInformation.token}")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ")") throw new RuntimeException("If condition should end with ')'. Got : ${tokenInformation.token}")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start if condition block")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        compileStatements()
        //if block ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "}") throw new RuntimeException("if condition block should be closed using '}'")
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
        tokenInformation = getCurrentToken()
        if(tokenInformation != null && tokenInformation.token == "else"){
            tokenInformation = getCurrentTokenAndAdvance()
            programStructure.add(createXmlNode("keyword", tokenInformation.token))
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start else block")
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
            compileStatements()
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.token != "}") throw new RuntimeException("else block should be closed using '}'")
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
        }
        programStructure.add("</ifStatement>")
    }

    public void compileExpression(){

        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || tokenInformation.tokenType == TokenType.SYMBOL) return;
        programStructure.add("<expression>")
        compileTerm()
        tokenInformation = getCurrentToken()
        //if the current token is binary operator
        while(tokenInformation != null && BINARY_OPS.contains(tokenInformation.token)){
            tokenInformation = getCurrentTokenAndAdvance()
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
            compileTerm()
            tokenInformation = getCurrentToken()
        }
        programStructure.add("</expression>")
    }

    public void compileTerm(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null) return;
        programStructure.add("<term>")
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType in [TokenType.INT_CONST, TokenType.STRING_CONST] ){ //integerConstant | stringConstant
            programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token))
        } else if (tokenInformation.tokenType == TokenType.KEYWORD){ //keywordConstant
            if(!KEYWORD_CONSTANTS.contains(tokenInformation.token)) throw new RuntimeException("Unsupported keyword '${tokenInformation.token}' in Term.")
            programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token))
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){ //varName | varName '[' expression ']' | subroutineCall | '(' expression ')'
            programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token)) //varName
            tokenInformation = getCurrentToken()
            if(tokenInformation != null){
                if(tokenInformation.token == "["){ // '[' expression ']'
                    tokenInformation = getCurrentTokenAndAdvance()
                    programStructure.add(createXmlNode("symbol", tokenInformation.token))
                    compileExpression()
                    tokenInformation = getCurrentToken()
                    if(tokenInformation == null || tokenInformation.token != "]") throw new RuntimeException("No closing ']' found. Got : ${tokenInformation.token}")
                    tokenInformation = getCurrentTokenAndAdvance()
                    programStructure.add(createXmlNode("symbol", tokenInformation.token))
                } else if(tokenInformation.token == "("){ //subroutineName '(' expressionList ')'
                    compileSubroutineCallExpressionList()
                }
            }
        }
       else if (tokenInformation.tokenType == TokenType.SYMBOL) {
            //it can be: '(' expression ')'  or unaryOp term or <className>.subroutineName(expressionList)
            if (tokenInformation.token == "(") {
                programStructure.add(createXmlNode("symbol", tokenInformation.token))
                compileExpression()
                tokenInformation = getCurrentToken()
                if (tokenInformation == null || tokenInformation.token != ")") throw new RuntimeException("Expression group should be terminated by  ')'")
                tokenInformation = getCurrentTokenAndAdvance()
                programStructure.add(createXmlNode("symbol", tokenInformation.token))
            } else if (tokenInformation.token == '.') { // '.' subroutineName '(' expressionList ')'
                programStructure.add(createXmlNode("symbol", tokenInformation.token))
                compileSubroutineCallExpressionList()
            } else if (UNARY_OPS.contains(tokenInformation.token)) { //unary op term
                if(!UNARY_OPS.contains(tokenInformation.token)) throw new RuntimeException("Unary operator is not found: Got ${tokenInformation.token}")
                programStructure.add(createXmlNode("symbol", tokenInformation.token))
                compileTerm()
            }
        }// else throw new RuntimeException("Unknown term compilation path.")

        programStructure.add("</term>")
    }

    public void compileSubroutineCallExpressionList(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation.token == "("){
            tokenInformation = getCurrentTokenAndAdvance()
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
            compileExpressionList()
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.token != ")") throw new RuntimeException("Expression group should be terminated by  ')'")
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
        }
    }

    public void compileExpressionList(){
        programStructure.add("<expressionList>")
        compileExpression();
        TokenInformation tokenInformation = getCurrentToken()
        //if the next token is comma (,) read the next variable declaration
        while(tokenInformation.token == ","){
            tokenInformation = getCurrentTokenAndAdvance()
            programStructure.add(createXmlNode("symbol", tokenInformation.token))
            compileExpression()
        }
        programStructure.add("</expressionList>")
    }

    private void compileKeywordConstant(){
        TokenInformation tokenInformation = getCurrentToken()
        if(!KEYWORD_CONSTANTS.contains(tokenInformation.token)) throw new RuntimeException("Keyword constant is not found: Got ${tokenInformation.token}")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("keyword", tokenInformation.token))
    }

    private void compileUnaryOperator(){
        TokenInformation tokenInformation = getCurrentToken()
        if(!UNARY_OPS.contains(tokenInformation.token)) throw new RuntimeException("Unary operator is not found: Got ${tokenInformation.token}")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
    }

    public void compileBinaryOperator(){
        TokenInformation tokenInformation = getCurrentToken()
        if(!BINARY_OPS.contains(tokenInformation.token)) throw new RuntimeException("Binary operator is not found: Got ${tokenInformation.token}")
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode("symbol", tokenInformation.token))
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
