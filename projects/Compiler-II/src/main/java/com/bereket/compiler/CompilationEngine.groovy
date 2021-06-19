package com.bereket.compiler

import java.lang.reflect.Method

class CompilationEngine {

    private final List<String> UNARY_OPS = ["-", "~"]
    private final List<String> BINARY_OPS = ["+", "-", "*", "/", "&", "|", "<", ">", "=", "&lt;", "&gt;", "&amp;"]
    private final List<String> KEYWORD_CONSTANTS = ["true", "false", "null", "this"]
    private List<TokenInformation> tokenizedTokens = new ArrayList<>();
    private List<String> programStructure = new ArrayList<>()
    private SymbolTable symbolTable = new SymbolTable()
    private JackTokenizer jackTokenizer;
    private VMWriter vmWriter
    private String jackFileName
    private String jackClassName
    private int labelCounter = 0;
    int currentIndex = 0;

    private static Map<String, MethodMetaData> methodMetadataLookup = [:]

    private class MethodMetaData {
        String methodName
        String className
        boolean isVoid = false
        boolean isStatic = false
        int noOfLocalVars = -1

        MethodMetaData(String methodName, String className, boolean isVoid, boolean isStatic) {
            this.methodName = methodName; this.className = className;
            this.isVoid = isVoid; this.isStatic = isStatic
        }

        //"Qualified call" -> "className.method". Returns className part of the previous example
        static String getObjectNameFromMethodCallSignature(String callSignature){
            if(callSignature.indexOf('.') != -1) return callSignature.substring(0, callSignature.indexOf('.'));
            return null
        }
    }

    public CompilationEngine(String inputFileName, String outputFileName) {
        jackTokenizer = new JackTokenizer(inputFileName)
        vmWriter = new VMWriter(outputFileName)
        jackFileName = outputFileName.substring(outputFileName.lastIndexOf("/") + 1) //only leave the last token after /
                .replaceAll("[.].*", "")
        jackClassName = jackFileName
    }

    //for com.bereket.compiler.Test purposes
    public CompilationEngine() {
        vmWriter = new VMWriter(null)
    }

    void compileFile() {
        jackTokenizer.tokenize()
        this.tokenizedTokens.addAll(jackTokenizer.tokenizedTokens)
        compileClass();
        vmWriter.resetVmLines()
        resetState()
        compileClass()
        vmWriter.writeFile()
    }

    //'class' className '{' classVarDec* subroutineDec* '}'
    public void compileClass() {
        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "class") throw new RuntimeException("Class definition should begin with 'class'")
        //className
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("Identifier expected after class name; found: " + tokenInformation.token)

        //class name
        String className = tokenInformation.token

        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "{") throw new RuntimeException("'{' expected  after class name")
        compileClassVarDec();
        compileSubroutineDec(className)
        //class ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "}") throw new RuntimeException("Class should be closed using '}'")
    }

    //classVardDec = ('static' | 'field') type varName (',' varName)* ';'
    private void compileClassVarDec() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null || (tokenInformation.token != "static" && tokenInformation.token != "field")) return;
        //keep the kind
        IdentifierKind kind = IdentifierKind.valueOf(tokenInformation.token.toUpperCase())
        advance()
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if (tokenInformation.tokenType == TokenType.KEYWORD) {
            if (!["char", "int", "boolean"].contains(tokenInformation.token)) throw new RuntimeException("Unsupported variable type: '${tokenInformation.token}'. It should be one of 'int', 'char' or 'boolean'")
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER) {
        } else throw new RuntimeException("Invalid type '${tokenInformation.token}'")

        //keep the variable type
        String type = tokenInformation.token //record the type

        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")

        //keep the variable name
        String identifierName = tokenInformation.token
        //put the variable in symbol table
        symbolTable.define(identifierName, type, kind)

        tokenInformation = getCurrentTokenAndAdvance()
        //if the next token is comma (,) read the next variable declaration
        while (tokenInformation.token == ",") {
            tokenInformation = getCurrentTokenAndAdvance()
            if (tokenInformation.tokenType != TokenType.IDENTIFIER)
                throw new RuntimeException("variable list after comma should be an identifier")

            //keep the variable name
            identifierName = tokenInformation.token
            //put the variable in symbol table
            symbolTable.define(identifierName, type, kind)

            tokenInformation = getCurrentTokenAndAdvance()
        }
        if (tokenInformation.token != ";")
            throw new RuntimeException("Class variable declaration should terminate with ';'")
        compileClassVarDec()
    }

    //('constructor' | 'function' | 'method') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    public void compileSubroutineDec(String className) {

        String subroutineName;
        boolean isVoid = false
        boolean isStatic = false

        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null || (tokenInformation.token != "constructor" && tokenInformation.token != "function" && tokenInformation.token != "method")) return;
        if (tokenInformation.token == "function") isStatic = true

        //reset subroutine scoped symbol table
        this.symbolTable.restSubRoutineSymbolTable()

        advance()
        //constructor | function | method
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if (tokenInformation.tokenType == TokenType.KEYWORD) {
            if (!(tokenInformation.token in ["void", "int", "char"]))
                throw new RuntimeException("Invalid return type '${tokenInformation.token}' for method. It can only be 'void' or type")
            if (tokenInformation.token == "void")
                isVoid = true
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER) {
        } else throw new RuntimeException("Invalid return type '${tokenInformation.token}'")
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("method/function name is not valid : ${tokenInformation.token}")

        //Sub-routine name
        subroutineName = "${className}.${tokenInformation.token}"
        if (!methodMetadataLookup.containsKey(subroutineName)) methodMetadataLookup.put(subroutineName, new MethodMetaData(tokenInformation.token, className, isVoid, isStatic))

        tokenInformation = getCurrentTokenAndAdvance()
        //next is '('
        if (tokenInformation.token != "(") throw new RuntimeException("Method parameter list should start with '('. Found ${tokenInformation.token}")

        compileParameterList();

        tokenInformation = getCurrentToken()
        if (tokenInformation.token != ")")
            throw new RuntimeException("Method parameter list should end with ')'")

        advance()
        compileSubroutineBody(subroutineName)

        if (isVoid) {
            //push constant 0 to the stack
            vmWriter.writePush(MemorySegment.CONST, 0)
        }

        vmWriter.writeReturn()
        compileSubroutineDec(className)
    }

    //'{' varDec* statements '}'
    public void compileSubroutineBody(String subroutineName) {

        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation.token != "{") throw new RuntimeException("Subroutine body should begin with '}'")
        advance()

        //write the function call here
        MethodMetaData metaData = methodMetadataLookup.get(subroutineName)
        vmWriter.writeFunction(subroutineName, (metaData && metaData.noOfLocalVars > -1 ? metaData.noOfLocalVars : 0))

        int noOfLocalVars = compileVarDec()
        //will be filled in first pass
        if (metaData.noOfLocalVars == -1) {
            metaData.noOfLocalVars = noOfLocalVars
            methodMetadataLookup.put(subroutineName, metaData)
        }

        // allocate memory for new objects creation here
        if(metaData && metaData.methodName == "new"){
            vmWriter.writePush(MemorySegment.CONST, symbolTable.varCount(IdentifierKind.FIELD))
            vmWriter.writeCall("Memory.alloc", 1)
            vmWriter.writePop(MemorySegment.POINTER, 0) //Anchors this to the base address
        } else if(metaData && !metaData.isStatic){
            vmWriter.writePush(MemorySegment.ARG, 0)
            vmWriter.writePop(MemorySegment.POINTER, 0) //Anchors this to the base address
        }

        compileStatements()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "}") throw new RuntimeException("Subroutine body should end with '}'")
    }

    //((type varName) (',' type varName)*)?
    public int compileParameterList() {
        int paramsCount = 0;
        if (readParameter()) {
            paramsCount++
            if (getCurrentToken() && getCurrentToken().token == ")") return paramsCount

            TokenInformation tokenInformation = getCurrentTokenAndAdvance()
            //if the next token is comma (,) read the next variable declaration
            while (tokenInformation != null && tokenInformation.token == ",") {
                if (readParameter()) {
                    paramsCount++
                    if (getCurrentToken() && getCurrentToken().token == ")")
                        break;
                    tokenInformation = getCurrentTokenAndAdvance()
                }
            }
        }
        return paramsCount
    }

    public boolean readParameter() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null) return false;
        if (tokenInformation.tokenType == TokenType.KEYWORD) {
            if (!["char", "int", "boolean"].contains(tokenInformation.token)) return false;
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER) {
        } else return false
        //extract the datatype
        String dataType = tokenInformation.token
        getCurrentTokenAndAdvance()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")
        //extract the variable name
        String variableName = tokenInformation.token
        symbolTable.define(variableName, dataType, IdentifierKind.ARG)
        return true;
    }

    //var' type varName (',' varName)* ';'
    public int compileVarDec() {
        int noOfLocalVars = 0
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null || tokenInformation.token != "var") return 0;
        advance() //var
        tokenInformation = getCurrentTokenAndAdvance()
        //if the token type is keyword, it should be any of the ff. "int", "char" or "boolean". Otherwise it can be a class name
        if (tokenInformation.tokenType == TokenType.KEYWORD) {
            if (!["char", "int", "boolean"].contains(tokenInformation.token)) throw new RuntimeException("Unsupported variable type: '${tokenInformation.token}'. It should be one of 'int', 'char' or 'boolean'")
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER) {
        } else throw new RuntimeException("Invalid type '${tokenInformation.token}'")

        //keep data type
        String dataType = tokenInformation.token

        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")

        //keep variable name
        String variableName = tokenInformation.token
        symbolTable.define(variableName, dataType, IdentifierKind.VAR)
        SymbolTable.SymbolEntry entry = symbolTable.getSymbolEntry(variableName)
        vmWriter.writePush(MemorySegment.LOCAL, entry.index)

        noOfLocalVars++

        tokenInformation = getCurrentTokenAndAdvance()
        //if the next token is comma (,) read the next variable declaration
        while (tokenInformation.token == ",") {
            tokenInformation = getCurrentTokenAndAdvance()
            if (tokenInformation.tokenType != TokenType.IDENTIFIER)
                throw new RuntimeException("variable list after comma should be an identifier")

            variableName = tokenInformation.token
            symbolTable.define(variableName, dataType, IdentifierKind.VAR)
            entry = symbolTable.getSymbolEntry(variableName)
            vmWriter.writePush(MemorySegment.LOCAL, entry.index)
            noOfLocalVars++
            tokenInformation = getCurrentTokenAndAdvance()
        }
        if (tokenInformation.token != ";")
            throw new RuntimeException("Variables declaration should terminate with ';'")
        noOfLocalVars += compileVarDec()

        return noOfLocalVars
    }

    public void compileStatements() {
        compileStatement();
    }

    public void compileStatement() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null || tokenInformation.tokenType != TokenType.KEYWORD) return;
        if (!(tokenInformation.token in ["let", "if", "while", "do", "return"])) return;
        switch (tokenInformation.token) {
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
    public void compileDo() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null || (tokenInformation.token != "do" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        advance() //do
        compileSubroutineCall("")
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != ";")
            throw new RuntimeException("do expression should terminate with ';'")

    }

    //'return' (expression)? ';'
    public void compileReturn() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null || (tokenInformation.token != "do" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        advance() //return
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != ";")
            throw new RuntimeException("return expression should terminate with ';'")
    }

    //'let' varName ('[' expression ']')? '=' expression ';'
    public void compileLet() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null || (tokenInformation.token != "let" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        advance() //var
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.tokenType != TokenType.IDENTIFIER) throw new RuntimeException("variable name is not valid : ${tokenInformation.token}")

        String variableName = tokenInformation.token
        SymbolTable.SymbolEntry symbolEntry = symbolTable.getSymbolEntry(variableName)

        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token == "[") {
            compileExpression()
            tokenInformation = getCurrentTokenAndAdvance()
            if (tokenInformation.token != "]") throw new RuntimeException("No closing ']' found. Got : ${tokenInformation.token}")
            tokenInformation = getCurrentTokenAndAdvance()
        }
        if (tokenInformation.token != "=") throw new RuntimeException("Equal sign expected in expression. Found : ${tokenInformation.token}")

        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != ";")
            throw new RuntimeException("Variables declaration should terminate with ';'")

        //POP/assign value to variable
        if(symbolEntry.kind == IdentifierKind.FIELD){
            vmWriter.writePop(MemorySegment.THIS, symbolEntry.index)
        } else
            vmWriter.writePop(symbolEntry.kind.memorySegment, symbolEntry.index)
    }

    //'while' '(' expression ')' '{' statements '}'
    public void compileWhile() {

        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation == null || (tokenInformation.token != "while" && tokenInformation.tokenType != TokenType.KEYWORD)) return;

        int counter = labelCounter++
        vmWriter.writeLabel("${jackFileName}_${counter}_while_begin")

        //brace
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "(") throw new RuntimeException("While condition expression should begin with '('. Got : ${tokenInformation.token}")
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != ")") throw new RuntimeException("While condition expression should end with ')'. Got : ${tokenInformation.token}")

        vmWriter.writeArithmetic(Command.NOT)
        vmWriter.writeIf("${jackFileName}_${counter}_while_end")

        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start while condition block")

        compileStatements()
        //if block ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "}") throw new RuntimeException("while condition block should be closed using '}'")

        vmWriter.writeGoto("${jackFileName}_${counter}_while_begin")
        vmWriter.writeLabel("${jackFileName}_${counter}_while_end")
    }

    //'if' '(' expression ')' '{' statements '}' 'else' '{' statements '}'
    public void compileIf() {
        TokenInformation tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation == null || (tokenInformation.token != "if" && tokenInformation.tokenType != TokenType.KEYWORD)) return;
        int counter = labelCounter++;

        //brace
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "(") throw new RuntimeException("If condition should begin with '('. Got : ${tokenInformation.token}")
        compileExpression()
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != ")")
            throw new RuntimeException("If condition should end with ')'. Got : ${tokenInformation.token}")
        tokenInformation = getCurrentTokenAndAdvance()

        if (tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start if condition block")

        vmWriter.writeArithmetic(Command.NOT)
        vmWriter.writeIf("${jackFileName}_${counter}_else")

        compileStatements()
        //if block ending node
        tokenInformation = getCurrentTokenAndAdvance()
        if (tokenInformation.token != "}") throw new RuntimeException("if condition block should be closed using '}'")
        tokenInformation = getCurrentToken()

        vmWriter.writeGoto("${jackFileName}_${counter}_end")
        vmWriter.writeLabel("${jackFileName}_${counter}_else")

        if (tokenInformation != null && tokenInformation.token == "else") {
            advance()
            tokenInformation = getCurrentTokenAndAdvance()
            if (tokenInformation.token != "{") throw new RuntimeException("'{' expected  to start else block")

            compileStatements()
            tokenInformation = getCurrentTokenAndAdvance()
            if (tokenInformation.token != "}") throw new RuntimeException("else block should be closed using '}'")
        }
        vmWriter.writeLabel("${jackFileName}_${counter}_end")
    }

    //returns true if expression has at least one term; false otherwise
    public boolean compileExpression() {
        TokenInformation tokenInformation = getCurrentToken()
        //if an expression starts with a symbol, it should be by one of '(', '~', '-' to mark the start of an expression
        if (tokenInformation == null ||
                (tokenInformation.tokenType == TokenType.SYMBOL && !["(", "~", "-"].contains(tokenInformation.token))) return false;

        compileTerm()
        tokenInformation = getCurrentToken()
        //if the current token is binary operator
        while (tokenInformation && BINARY_OPS.contains(tokenInformation.token)) {
            String binaryOperator = tokenInformation.getDecodedString()
            advance()
            compileTerm()
            tokenInformation = getCurrentToken()
            Command cmd = Command.getCommandForSymbol(binaryOperator)
            if (cmd) vmWriter.writeArithmetic(cmd)
            else {
                // "*", "/", do OS system calls
                if (binaryOperator == "*") {
                    vmWriter.writeCall("Math.multiply", 2)
                } else if (binaryOperator == "/") {
                    vmWriter.writeCall("Math.divide", 2)
                } else
                    throw new RuntimeException("Operator not supporter yet")
            }
        }
        return true
    }

    public void compileTerm() {

        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation == null) return;
        tokenInformation = getCurrentTokenAndAdvance()

        if (tokenInformation.tokenType == TokenType.INT_CONST) { //integerConstant
            vmWriter.writePush(MemorySegment.CONST, tokenInformation.token.toInteger())
        } else if (tokenInformation.tokenType == TokenType.STRING_CONST) { // stringConstant
            char[] chars = tokenInformation.token.chars
            for (char c : chars) {
                //compileSubroutineCall()  => String.appendChar(c).
            }
        } else if (tokenInformation.tokenType == TokenType.KEYWORD) { //keywordConstant
            if (!KEYWORD_CONSTANTS.contains(tokenInformation.token)) throw new RuntimeException("Unsupported keyword '${tokenInformation.token}' in Term.")
            if (tokenInformation.token == "true") {
                vmWriter.writePush(MemorySegment.CONST, 0)
                vmWriter.writeArithmetic(Command.NOT)
            } else if (tokenInformation.token in ["false", "null"]) {
                vmWriter.writePush(MemorySegment.CONST, 0)
            } else if (tokenInformation.token == "this") {
                vmWriter.writePush(MemorySegment.POINTER, 0)
            }
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER) {
            //varName | varName '[' expression ']' | subroutineCall
            compileTermWithLeadingIdentifier(tokenInformation)
        } else if (tokenInformation.tokenType == TokenType.SYMBOL) {
            //it can be: '(' expression ')'  or unaryOp term or <className>.subroutineName(expressionList)
            compileTermWithLeadingSymbol(tokenInformation)
        } else throw new RuntimeException("Unknown term compilation path.")

    }

    private void compileTermWithLeadingIdentifier(TokenInformation tokenInformation) {

        SymbolTable.SymbolEntry symbolEntry = symbolTable.getSymbolEntry(tokenInformation.token)
        if (symbolEntry) {
            if(symbolEntry.kind == IdentifierKind.FIELD){
                vmWriter.writePush(MemorySegment.THIS, symbolEntry.index)
            } else  vmWriter.writePush(symbolEntry.kind.getMemorySegment(), symbolEntry.index)
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
        if (!tokenInformation) return
        if (tokenInformation.token == "(") {
            compileExpression()
            tokenInformation = getCurrentToken()
            if (tokenInformation == null || tokenInformation.token != ")") throw new RuntimeException("Expression group should be terminated by  ')'")
                advance()
        } else if (UNARY_OPS.contains(tokenInformation.token)) { //unary op term
            compileTerm()
            Command cmd = tokenInformation.token == "~" ? Command.NOT : Command.NEG
            vmWriter.writeArithmetic(cmd)

        } else throw new RuntimeException("Term cannot start with '${tokenInformation.token}'")
    }

    void compileSubroutineCall(String part) {

        TokenInformation tokenInformation = getCurrentTokenAndAdvance()

        if (tokenInformation.token == "(") { // subRoutine( expressionList )
            int noOfArgs = 0
            final String REGEX = "[A-Z].+\\..*"
            boolean isMethod = false
            SymbolTable.SymbolEntry memberObject = null
            //static method: - Always begin with a Class name
            //field: preceded by an object name or local method call
            if(!part.matches(REGEX))
                isMethod = true

            if(isMethod){
                String objectName = MethodMetaData.getObjectNameFromMethodCallSignature(part)
                if(objectName){ //object name might not be present for local methods
                    memberObject = symbolTable.getSymbolEntry(objectName)
                    if(memberObject){
                        //push the object address on top of the stack as an arg[0] for the called method
                        //Since the object variable belongs to this class, first find the the address of the current object (owning object which composes "objectName"), and add the "index"
                        //we find in symbol table to get the offset
                        vmWriter.writePush(memberObject.kind.memorySegment, memberObject.index) //
                        noOfArgs++
                    } else {
                        //vmWriter.writePush(MemorySegment.POINTER, 0) //
                        //noOfArgs++
                        throw new RuntimeException("Irregular !!!")
                    }
                } else { //this is a self call -> just methodName()
                    //push the current object address
                    vmWriter.writePush(MemorySegment.POINTER, 0) //
                    noOfArgs++
                }
            }

            String subroutineName = resolveSubroutineNameFromCall(part)
            MethodMetaData methodMetaData = methodMetadataLookup.get(subroutineName)

            noOfArgs += compileExpressionList()
            tokenInformation = getCurrentTokenAndAdvance()
            if (tokenInformation == null || tokenInformation.token != ")")
                throw new RuntimeException("Subroutine call should be terminated by  ')'")

            vmWriter.writeCall(subroutineName, noOfArgs)
            //Return code
            //if the called subroutine is a constructor, the call VM code will end up putting the created new object address on the stack.
            // We want to save the returned address in the object variable.
            if(isMethod && methodMetaData){
                if(methodMetaData.methodName == "new"){
                    vmWriter.writePop(MemorySegment.LOCAL, memberObject.index)
                } else if (methodMetaData.isVoid) { //void methods put 0 on the top of the stack which should be popped out
                    vmWriter.writePop(MemorySegment.TEMP, 0)
                }
            }
        } else if (tokenInformation.tokenType == TokenType.IDENTIFIER || tokenInformation.token == ".") {
            //If Token is Identifier, it only comes here the first time as in className.<....> or in a recursive call where the previous token was "." ; [className.] subRoutine [( expressioinList ) ]  <= only the subRoutine part
            //If "." it comes between object/class and subroutine name
            part += tokenInformation.token
            compileSubroutineCall(part)

        } else throw new RuntimeException("Invalid subroutine call path")
    }

    private String resolveSubroutineNameFromCall(String subroutineCall){
        //Static subroutine signature
        final String REGEX = "[A-Z].+\\..*"
        if(subroutineCall.matches(REGEX)) return subroutineCall;

        String objectName = MethodMetaData.getObjectNameFromMethodCallSignature(subroutineCall)
        SymbolTable.SymbolEntry memberObject = symbolTable.getSymbolEntry(objectName)
        if(memberObject) { //if this ia a member object, find the object type to get the class name
            return subroutineCall.replaceAll(objectName, memberObject.type)
        }
        //it it comes to this point: this is  member call. Append the current class name
        return "${jackClassName}." + subroutineCall
    }

    void compileArrayExpression() {
        TokenInformation tokenInformation = getCurrentToken()
        if (tokenInformation.token == "[") { // '[' expression ']'
            advance()
            compileExpression()
            tokenInformation = getCurrentToken()
            if (tokenInformation == null || tokenInformation.token != "]") throw new RuntimeException("No closing ']' found. Got : ${tokenInformation.token}")
            advance()
        }
    }

    public int compileExpressionList() {
        int noOfExpressions = 0
        if(compileExpression()) noOfExpressions++
        TokenInformation tokenInformation = getCurrentToken()
        //if the next token is comma (,) read the next variable declaration
        while (tokenInformation.token == ",") {
            advance()
            compileExpression()
            noOfExpressions++
            tokenInformation = getCurrentToken()
        }
        return noOfExpressions
    }

    public TokenInformation getCurrentTokenAndAdvance() {
        if (currentIndex >= tokenizedTokens.size()) return null;
        return tokenizedTokens.get(currentIndex++)
    }

    public void advance(){
        this.getCurrentTokenAndAdvance()
    }

    public TokenInformation getCurrentToken() {
        if (currentIndex >= tokenizedTokens.size()) return null;
        return tokenizedTokens.get(currentIndex)
    }

    void setTokenizedTokens(List<TokenInformation> tokenizedTokens) {
        this.tokenizedTokens = tokenizedTokens
    }

    public SymbolTable getSymbolTable() {
        return symbolTable
    }

    private void resetState() {
        programStructure = new ArrayList<>()
        symbolTable = new SymbolTable()
        labelCounter = 0;
        currentIndex = 0;
    }
}
