package com.bereket.compiler

class CompilationEngine {

    private FileOutputStream fileOutputStream
    private final List<String> UNARY_OPS = ["-", "~"]
    private final List<String> BINARY_OPS = ["+", "-", "*", "/", "&", "|", "<", ">", "=", "&lt;", "&gt;", "&amp;"]
    private final List<String> KEYWORD_CONSTANTS = ["true", "false", "null", "this"]
    private List<TokenInformation> tokenizedTokens = new ArrayList<>();
    private List<String> programStructure = new ArrayList<>()
    private SymbolTable symbolTable = new SymbolTable()
    private JackTokenizer jackTokenizer;
    private VMWriter vmWriter
    private String jackFileName
    private int labelCounter = 0;
    int currentIndex = 0;
    private Set<String> voidMethods = new HashSet<>()
    private Map<String, Integer> localVarsLookup = new HashMap<>()

    public CompilationEngine(String inputFileName, String outputFileName){
        jackTokenizer = new JackTokenizer(inputFileName)
        vmWriter = new VMWriter(outputFileName)
        jackFileName = outputFileName.substring(outputFileName.lastIndexOf("/") + 1) //only leave the last token after /
                .replaceAll("[.].*", "")
    }

    //for com.bereket.compiler.Test purposes
    public CompilationEngine(){
        vmWriter = new VMWriter(null)
    }

    void compileFile(){
        jackTokenizer.tokenize()
        this.tokenizedTokens.addAll(jackTokenizer.tokenizedTokens)
        compileClass();
        vmWriter.resetVmLines()
        resetState()
        compileClass()
        vmWriter.writeFile()
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
        addToStructure("<class>")
        addToStructure(tokenInformation)
        //className
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("Identifier expected after class name; found: " + tokenInformation.token)
        addToStructure(tokenInformation)

        //class name
        String className = tokenInformation.token

        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "{") throw new RuntimeException("'{' expected  after class name")
        addToStructure(tokenInformation)
        compileClassVarDec();
        compileSubroutineDec(className)
        //class ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "}") throw new RuntimeException("Class should be closed using '}'")
        addToStructure(tokenInformation)
        addToStructure("</class>")
    }

    private String createXmlNode(String nodeName, String value){
        return "<${nodeName}>${value}</${nodeName}>".toString()
    }

    //classVardDec = ('static' | 'field') type varName (',' varName)* ';'
    private void compileClassVarDec(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "static" && tokenInformation.token != "field")) return;
        //keep the kind
        IdentifierKind kind = IdentifierKind.valueOf(tokenInformation.token.toUpperCase())
        tokenInformation = getCurrentTokenAndAdvance()
        addToStructure("<classVarDec>")
        addToStructure(tokenInformation) //field | static
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if(tokenInformation.tokenType == TokenType.KEYWORD) {
            if(!["char", "int", "boolean"].contains(tokenInformation.token)) throw new RuntimeException("Unsupported variable type: '${tokenInformation.token}'. It should be one of 'int', 'char' or 'boolean'")
            addToStructure(tokenInformation)
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){
            addToStructure(tokenInformation)
        } else throw new RuntimeException("Invalid type '${tokenInformation.token}'")

        //keep the variable type
        String type = tokenInformation.token //record the type

        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")

        //keep the variable name
        String identifierName = tokenInformation.token
        //put the variable in symbol table
        symbolTable.define(identifierName, type, kind)

        addToStructure(tokenInformation)
        tokenInformation = getCurrentTokenAndAdvance()
        //if the next token is comma (,) read the next variable declaration
        while(tokenInformation.token == ","){
            addToStructure(tokenInformation)
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.tokenType != TokenType.IDENTIFIER)
                throw new RuntimeException("variable list after comma should be an identifier")

            //keep the variable name
            identifierName = tokenInformation.token
            //put the variable in symbol table
            symbolTable.define(identifierName, type, kind)

            addToStructure(tokenInformation)
            tokenInformation = getCurrentTokenAndAdvance()
        }
        if(tokenInformation.token != ";")
            throw new RuntimeException("Class variable declaration should terminate with ';'")
        addToStructure(tokenInformation)
        addToStructure("</classVarDec>")
        compileClassVarDec()
    }

    //('constructor' | 'function' | 'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    public void compileSubroutineDec(String className){

        String subroutineName;
        boolean isVoidRoutine = false

        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "constructor" && tokenInformation.token != "function" && tokenInformation.token != "method")) return;

        //reset subroutine scoped symbol table
        this.symbolTable.restSubRoutineSymbolTable()

        tokenInformation = getCurrentTokenAndAdvance()
        addToStructure("<subroutineDec>")
        addToStructure(tokenInformation) //constructor | function | method
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if(tokenInformation.tokenType == TokenType.KEYWORD) {
            if(!(tokenInformation.token in ["void", "int", "char"]))
                throw new RuntimeException("Invalid return type '${tokenInformation.token}' for method. It can only be 'void' or type")
            if(tokenInformation.token == "void")
                isVoidRoutine = true
            addToStructure(tokenInformation)
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){
            addToStructure(tokenInformation)
        } else throw new RuntimeException("Invalid return type '${tokenInformation.token}'")
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("method/function name is not valid : ${tokenInformation.token}")
        addToStructure(tokenInformation)

        //Sub-routine name
        subroutineName = "${className}.${tokenInformation.token}"

        tokenInformation = getCurrentTokenAndAdvance()
        //next is '('
        if(tokenInformation.token != "(") throw new RuntimeException("Method parameter list should start with '('. Found ${tokenInformation.token}")
        addToStructure(tokenInformation)

        compileParameterList();

        tokenInformation = getCurrentToken()
        if(tokenInformation.token != ")")
            throw new RuntimeException("Method parameter list should end with ')'")

        tokenInformation = getCurrentTokenAndAdvance()

        addToStructure(tokenInformation)
        compileSubroutineBody(subroutineName)

        if(isVoidRoutine){
            //push constant 0 to the stack
            vmWriter.writePush(MemorySegment.CONST, 0)
            voidMethods.add(subroutineName)
        }
        vmWriter.writeReturn()

        addToStructure("</subroutineDec>")
        compileSubroutineDec(className)
    }

    //'{' varDec* statements '}'
    public void compileSubroutineBody(String subroutineName){

        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation.token != "{") throw new RuntimeException("Subroutine body should begin with '}'")
        addToStructure("<subroutineBody>")
        tokenInformation = getCurrentTokenAndAdvance()
        addToStructure(tokenInformation)

        //write the function call here
        vmWriter.writeFunction(subroutineName, localVarsLookup.get(subroutineName) ?: 0)
        int noOfLocalVars = compileVarDec()
        //will be filled in first pass
        if(!localVarsLookup.containsKey(subroutineName)) localVarsLookup.put(subroutineName, noOfLocalVars)

        compileStatements()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "}") throw new RuntimeException("Subroutine body should end with '}'")
        addToStructure(tokenInformation)
        addToStructure("</subroutineBody>")
    }

    //((type varName) (',' type varName)*)?
    public int compileParameterList(){
        int paramsCount = 0;
        addToStructure("<parameterList>")
        if(readParameter()){
            paramsCount++
           if(getCurrentToken() && getCurrentToken().token == ")") return paramsCount

            TokenInformation tokenInformation = getCurrentTokenAndAdvance()
            //if the next token is comma (,) read the next variable declaration
            while(tokenInformation != null && tokenInformation.token == ","){
                addToStructure(tokenInformation)
                if(readParameter()){
                    paramsCount++
                    if(getCurrentToken() && getCurrentToken().token == ")")
                        break;
                    tokenInformation = getCurrentTokenAndAdvance()
                }
            }
        }
        addToStructure("</parameterList>")
        return paramsCount
    }

    public boolean readParameter(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null) return false;
        String type = null;
        if(tokenInformation.tokenType == TokenType.KEYWORD) {
            if(!["char", "int", "boolean"].contains(tokenInformation.token)) return false;
            type = "keyword"
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){
            type = "identifier"
        } else return false
        //extract the datatype
        String dataType =  tokenInformation.token
        tokenInformation = getCurrentTokenAndAdvance()
        programStructure.add(createXmlNode(type, tokenInformation.token))
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")
        //extract the variable name
        String variableName = tokenInformation.token
        symbolTable.define(variableName, dataType, IdentifierKind.ARG)
        addToStructure(tokenInformation)
        return true;
    }

    //var' type varName (',' varName)* ';'
    public int compileVarDec(){
        int noOfLocalVars = 0
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || tokenInformation.token != "var" ) return 0;
        addToStructure("<varDec>")
        tokenInformation = getCurrentTokenAndAdvance()
        addToStructure(tokenInformation) //var
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if(tokenInformation.tokenType == TokenType.KEYWORD) {
            if(!["char", "int", "boolean"].contains(tokenInformation.token)) throw new RuntimeException("Unsupported variable type: '${tokenInformation.token}'. It should be one of 'int', 'char' or 'boolean'")
            addToStructure(tokenInformation)
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){
            addToStructure(tokenInformation)
        } else throw new RuntimeException("Invalid type '${tokenInformation.token}'")

        //keep data type
        String dataType = tokenInformation.token

        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")
        addToStructure(tokenInformation)

        //keep variable name
        String variableName = tokenInformation.token
        symbolTable.define(variableName, dataType, IdentifierKind.VAR)
        SymbolTable.SymbolEntry entry = symbolTable.getSymbolEntry(variableName)
        vmWriter.writePush(MemorySegment.LOCAL, entry.index)
        noOfLocalVars++

        tokenInformation = getCurrentTokenAndAdvance()
        //if the next token is comma (,) read the next variable declaration
        while(tokenInformation.token == ","){
            addToStructure(tokenInformation)
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.tokenType != TokenType.IDENTIFIER)
                throw new RuntimeException("variable list after comma should be an identifier")

            variableName = tokenInformation.token
            symbolTable.define(variableName, dataType, IdentifierKind.VAR)
            entry = symbolTable.getSymbolEntry(variableName)
            vmWriter.writePush(MemorySegment.LOCAL, entry.index)
            noOfLocalVars++

            addToStructure(tokenInformation)
            tokenInformation = getCurrentTokenAndAdvance()
        }
        if(tokenInformation.token != ";")
            throw new RuntimeException("Variables declaration should terminate with ';'")
        addToStructure(tokenInformation)
        addToStructure("</varDec>")
        noOfLocalVars += compileVarDec()

        return noOfLocalVars
    }

    public void compileStatements() {
        addToStructure("<statements>")
        compileStatement();
        addToStructure("</statements>")
    }

    public void compileStatement(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || tokenInformation.tokenType != TokenType.KEYWORD) return;
        if(!(tokenInformation.token in ["let", "if", "while", "do", "return"])) return;
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
        compileStatement();
    }
    //'do' compileDo ';'
    public void compileDo(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "do" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        addToStructure("<doStatement>")
        tokenInformation = getCurrentTokenAndAdvance()
        addToStructure(tokenInformation) //do
        compileSubroutineCall("")
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ";")
            throw new RuntimeException("do expression should terminate with ';'")
        addToStructure(tokenInformation)

        addToStructure("</doStatement>")
    }

    //'return' (expression)? ';'
    public void compileReturn(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "do" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        addToStructure("<returnStatement>")
        tokenInformation = getCurrentTokenAndAdvance()
        addToStructure(tokenInformation) //return
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ";")
            throw new RuntimeException("return expression should terminate with ';'")
        addToStructure(tokenInformation)
        addToStructure("</returnStatement>")
    }

    //'let' varName ('[' expression ']')? '=' expression ';'
    public void compileLet(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null || (tokenInformation.token != "let" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        addToStructure("<letStatement>")
        tokenInformation = getCurrentTokenAndAdvance()
        addToStructure(tokenInformation) //var
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")
        addToStructure(tokenInformation)
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token == "["){
            addToStructure(tokenInformation)
            compileExpression()
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.token != "]") throw new RuntimeException("No closing ']' found. Got : ${tokenInformation.token}")
            addToStructure(tokenInformation)
            tokenInformation = getCurrentTokenAndAdvance()
        }
        if(tokenInformation.token != "=") throw new RuntimeException("Equal sign expected in expression. Found : ${tokenInformation.token}")
        addToStructure(tokenInformation)
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ";")
            throw new RuntimeException("Variables declaration should terminate with ';'")
        addToStructure(tokenInformation)
        addToStructure("</letStatement>")
    }

    //'while' '(' expression ')' '{' statements '}'
    public void compileWhile() {

        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation == null || (tokenInformation.token != "while" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        addToStructure("<whileStatement>")

        int counter = labelCounter++
        vmWriter.writeLabel("${jackFileName}_${counter}_while_begin")


        addToStructure(tokenInformation)
        //brace
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "(") throw new RuntimeException("While condition expression should begin with '('. Got : ${tokenInformation.token}")
        addToStructure(tokenInformation)
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != ")") throw new RuntimeException("While condition expression should end with ')'. Got : ${tokenInformation.token}")

        vmWriter.writeArithmetic(Command.NEG)
        vmWriter.writeIf("${jackFileName}_${counter}_while_end")

        addToStructure(tokenInformation)
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start while condition block")

        addToStructure(tokenInformation)
        compileStatements()
        //if block ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "}") throw new RuntimeException("while condition block should be closed using '}'")
        addToStructure(tokenInformation)
        addToStructure("</whileStatement>")

        vmWriter.writeGoto("${jackFileName}_${counter}_while_begin")
        vmWriter.writeLabel("${jackFileName}_${counter}_while_end")
    }

    //'if' '(' expression ')' '{' statements '}' 'else' '{' statements '}'
    public void compileIf(){
        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation == null || (tokenInformation.token != "if" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        addToStructure("<ifStatement>")
        addToStructure(tokenInformation)

        int counter = labelCounter++;

        //brace
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "(") throw new RuntimeException("If condition should begin with '('. Got : ${tokenInformation.token}")
        addToStructure(tokenInformation)
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != ")")
            throw new RuntimeException("If condition should end with ')'. Got : ${tokenInformation.token}")
        addToStructure(tokenInformation)
        tokenInformation = getCurrentTokenAndAdvance()

        if(tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start if condition block")

        vmWriter.writeArithmetic(Command.NEG)
        vmWriter.writeIf("${jackFileName}_${counter}_else")


        addToStructure(tokenInformation)
        compileStatements()
        //if block ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.token != "}") throw new RuntimeException("if condition block should be closed using '}'")
        addToStructure(tokenInformation)
        tokenInformation = getCurrentToken()

        vmWriter.writeLabel("${jackFileName}_${counter}_else")

        if(tokenInformation != null && tokenInformation.token == "else"){
            tokenInformation = getCurrentTokenAndAdvance()
            addToStructure(tokenInformation)
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start else block")
            addToStructure(tokenInformation)

            compileStatements()
            tokenInformation = getCurrentTokenAndAdvance()
            if(tokenInformation.token != "}") throw new RuntimeException("else block should be closed using '}'")
            addToStructure(tokenInformation)
        }
        vmWriter.writeLabel("${jackFileName}_${counter}_end")
        addToStructure("</ifStatement>")
    }

    public void compileExpression(){

        TokenInformation tokenInformation = getCurrentToken()
        //if an expression starts with a symbol, it should be by one of '(', '~', '-' to mark the start of an expression
        if(tokenInformation == null ||
                (tokenInformation.tokenType == TokenType.SYMBOL && !["(", "~", "-"].contains(tokenInformation.token))) return;
        addToStructure("<expression>")
        compileTerm()
        tokenInformation = getCurrentToken()
        //if the current token is binary operator
        while(tokenInformation && BINARY_OPS.contains(tokenInformation.token)){
            String binaryOperator = tokenInformation.getDecodedString()

            tokenInformation = getCurrentTokenAndAdvance()
            addToStructure(tokenInformation)
            compileTerm()
            tokenInformation = getCurrentToken()
            Command cmd = Command.getCommandForSymbol(binaryOperator)
            if(cmd) vmWriter.writeArithmetic(cmd)
            else {
                // "*", "/", do OS system calls
                if(binaryOperator == "*"){
                    vmWriter.writeCall("Math.multiply", 2)
                } else if (binaryOperator == "/"){
                    vmWriter.writeCall("Math.divide", 2)
                } else
                    throw new RuntimeException("Operator not supporter yet")
            }
        }
        addToStructure("</expression>")
    }

    public void compileTerm(){
        TokenInformation tokenInformation = getCurrentToken()
        if(tokenInformation == null) return;
        addToStructure("<term>")
        tokenInformation = getCurrentTokenAndAdvance()
        if(tokenInformation.tokenType == TokenType.INT_CONST){ //integerConstant
            programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token))

            vmWriter.writePush(MemorySegment.CONST, tokenInformation.token.toInteger())

        } else if(tokenInformation.tokenType ==  TokenType.STRING_CONST){ // stringConstant
            programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token))
            char[] chars = tokenInformation.token.chars
            for(char c : chars){
                //compileSubroutineCall()  => String.appendChar(c).
            }
           // vmWriter.writePush(MemorySegment.CONST, Integer.valueOf(tokenInformation.token))

        } else if (tokenInformation.tokenType == TokenType.KEYWORD){ //keywordConstant
            if(!KEYWORD_CONSTANTS.contains(tokenInformation.token)) throw new RuntimeException("Unsupported keyword '${tokenInformation.token}' in Term.")
            programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token))

            if(tokenInformation.token == "true"){
                vmWriter.writePush(MemorySegment.CONST, 1)
                vmWriter.writeArithmetic(Command.NEG)
            } else if (tokenInformation.token in ["false", "null"]){
                vmWriter.writePush(MemorySegment.CONST, 0)
            } else if(tokenInformation.token == "this"){
            //TODO:
            }
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER){ //varName | varName '[' expression ']' | subroutineCall | '(' expression ')'
            compileTermWithLeadingIdentifier(tokenInformation)
        } else if (tokenInformation.tokenType == TokenType.SYMBOL) {
            //it can be: '(' expression ')'  or unaryOp term or <className>.subroutineName(expressionList)
            compileTermWithLeadingSymbol(tokenInformation)
        } else throw new RuntimeException("Unknown term compilation path.")

        addToStructure("</term>")
    }

    private void compileTermWithLeadingIdentifier(TokenInformation tokenInformation) {
        programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token)) //varName

        SymbolTable.SymbolEntry symbolEntry = symbolTable.getSymbolEntry(tokenInformation.token)
        if(symbolEntry){
            vmWriter.writePush(symbolEntry.kind.getMemorySegment(), symbolEntry.index)
        }

        TokenInformation currentToken = getCurrentToken()
        if (currentToken && currentToken.token == "[") {
            compileArrayExpression()
        } else if (currentToken && (currentToken.token == "(" || currentToken.token == ".")) {
            //if the "currentToken" is '(' or '.' the previous token (tokenInformation) must be a method name as in : ClassName. or methodName(
            String subroutineName = tokenInformation.token
            compileSubroutineCall(subroutineName)
        }
    }

    private void compileTermWithLeadingSymbol(TokenInformation tokenInformation) {
        if(!tokenInformation) return
        if (tokenInformation.token == "(") {
            addToStructure(tokenInformation)
            compileExpression()
            tokenInformation = getCurrentToken()
            if (tokenInformation == null || tokenInformation.token != ")")
                throw new RuntimeException("Expression group should be terminated by  ')'")
            tokenInformation = getCurrentTokenAndAdvance()
            addToStructure(tokenInformation)
        } else if (UNARY_OPS.contains(tokenInformation.token)) { //unary op term
            addToStructure(tokenInformation)
            compileTerm()

            Command cmd = tokenInformation.token == "~" ? Command.NEG : Command.NOT
            vmWriter.writeArithmetic(cmd)

        } else throw new RuntimeException("Term cannot start with '${tokenInformation.token }'")
    }

    //DONE
    void compileSubroutineCall(String part){
        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token == "(") { // subRoutine( expressionList )
            addToStructure(tokenInformation)
            int noOfArgs = compileExpressionList()
            tokenInformation = getCurrentTokenAndAdvance()
            if (tokenInformation == null || tokenInformation.token != ")")
                throw new RuntimeException("Subroutine call should be terminated by  ')'")

            vmWriter.writeCall(part, noOfArgs)
            if(voidMethods.contains(part)){
                vmWriter.writePop(MemorySegment.CONST, 0)
            }
            addToStructure(tokenInformation)
        } else if (tokenInformation.token == "."){ // [className].[subRoutine ( expressioinList ) ] <= only the dot part
            addToStructure(tokenInformation) // write .

            part += tokenInformation.token
            compileSubroutineCall(part)

        } else if(tokenInformation.tokenType == TokenType.IDENTIFIER){ //it only comes here in a recursive call where the previous token was "." ; [className.] subRoutine [( expressioinList ) ]  <= only the subRoutine part
            addToStructure(tokenInformation)

            part += tokenInformation.token
            compileSubroutineCall(part)

        } else throw new RuntimeException("Invalid subroutine call path")
    }

    void compileArrayExpression() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation.token == "[") { // '[' expression ']'
            tokenInformation = getCurrentTokenAndAdvance()
            addToStructure(tokenInformation)
            compileExpression()
            tokenInformation = getCurrentToken()
            if (tokenInformation == null || tokenInformation.token != "]") throw new RuntimeException("No closing ']' found. Got : ${tokenInformation.token}")
            tokenInformation = getCurrentTokenAndAdvance()
            addToStructure(tokenInformation)
        }
    }

    public int compileExpressionList(){
        int noOfExpressions = 0
        addToStructure("<expressionList>")
        compileExpression();
        noOfExpressions++
        TokenInformation tokenInformation = getCurrentToken()
        //if the next token is comma (,) read the next variable declaration
        while(tokenInformation.token == ","){
            tokenInformation = getCurrentTokenAndAdvance()
            addToStructure(tokenInformation)
            compileExpression()
            noOfExpressions++
            tokenInformation = getCurrentToken()
        }
        addToStructure("</expressionList>")
        return noOfExpressions
    }

    public TokenInformation getCurrentTokenAndAdvance(){
        if(currentIndex >= tokenizedTokens.size()) return null; // throw new RuntimeException("Reached out of bound - cannot go to next token.")
        return tokenizedTokens.get(currentIndex++)
    }

    public TokenInformation getCurrentToken(){
        if(currentIndex >= tokenizedTokens.size()) return null;
        return tokenizedTokens.get(currentIndex)
    }

    List<TokenInformation> getTokenizedTokens() {
        return tokenizedTokens
    }

    void setTokenizedTokens(List<TokenInformation> tokenizedTokens) {
        this.tokenizedTokens = tokenizedTokens
    }

    private void addToStructure(TokenInformation tokenInformation){
        programStructure.add(createXmlNode(tokenInformation.tokenType.tagName, tokenInformation.token))
    }

    private void addToStructure(String string){
        programStructure.add(string)
    }

    List<String> getProgramStructure() {
        return programStructure
    }

    public SymbolTable getSymbolTable() {
        return symbolTable
    }

    private void resetState(){
        programStructure = new ArrayList<>()
        symbolTable = new SymbolTable()
        labelCounter = 0;
        currentIndex = 0;
    }
}
