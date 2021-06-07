package com.bereket.compiler

import org.apache.commons.lang3.StringUtils

class JackTokenizer {

    private final List<String> KEYWORDS = ["class", "constructor", "function", "method", "field", "static", "var", "int", "char",
                                           "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else", "while", "return"];
    private final List<String> SYMBOLS  = ["{", "}", "(", ")", "[", "]", ".", ",", ";", "+", "-", "*", "/", "&", "<", ">", "=", "~"]
    private List<String> sourceCode = new ArrayList<>();
    private int lineCounter = 0;
    private FileOutputStream fileOutputStream
    private List<TokenInformation> tokenizedTokens = new ArrayList<>();

    //for test purpose
    public JackTokenizer(){
    }

    public JackTokenizer(String sourceFile, String outputFileName){
        String fileContent = new String(new File(sourceFile).readBytes());
        List<String> rawLines = readAndCleanSource(fileContent);
        this.filterSourceFile(rawLines)
        fileOutputStream = new FileOutputStream(outputFileName)
    }

    public JackTokenizer(List<String> sourceCode){
        this.sourceCode = sourceCode;
    }

    public void tokenize(){
        while(hasMoreTokens()){
            List<String> tokens = getTokensFromLine(getCurrentLine())
            for(String token: tokens){
                if(StringUtils.isEmpty(token)) continue
                TokenType tokenType = tokenType(token)
                tokenizedTokens.add(new TokenInformation(token, tokenType));
            }
            advance();
        }
        writeTokensToFile()
        this.close()
    }

    public boolean hasMoreTokens(){
        return this.lineCounter < this.sourceCode.size();
    }

    public void advance(){
        this.lineCounter++;
    }

    TokenType tokenType(String token){
        if(KEYWORDS.contains(token)) return TokenType.KEYWORD;
        if(SYMBOLS.contains(token)) return TokenType.SYMBOL;
        if(token.contains("\"")){
            if(token.startsWith("\"") && token.endsWith("\"")) return TokenType.STRING_CONST;
            else
                throw new RuntimeException("String format '" + token + "' is not valid. String should begin and end with double quote.")
        }
        try {
            Integer.parseInt(token) //if this does not throw an exception this is a number
            return TokenType.INT_CONST
        } catch(Exception e){}
        //identifier should not begin with numberx
        if(token.matches("^[0-9]+.*")) throw new RuntimeException("Identifier should not begin with number charcter: " + token)
        return TokenType.IDENTIFIER
    }

    private void writeTokensToFile(){
        StringBuilder builder = new StringBuilder("<tokens>\n")
        for(TokenInformation tokenInformation: tokenizedTokens){
            String tagName = tokenInformation.tokenType.tagName
            builder.append(String.format("<%s>%s</%s>\n", tagName, tokenInformation.token, tagName))
        }
        builder.append("</tokens>")
        if(fileOutputStream) fileOutputStream.write(builder.toString().getBytes())
    }

    private String getCurrentLine(){
        return this.sourceCode.get(lineCounter);
    }

    List<String> getSourceCode() {
        return sourceCode
    }

    void filterSourceFile(List<String> rawLines) {
        for(String line : rawLines){
            if(!StringUtils.isEmpty(line)) line = line.trim()
            if(!StringUtils.isEmpty(line)){
                if(line.startsWith("//")) continue; //remove comment lines
                line = line.replaceAll("//.*", "").trim(); //remove comments at the end of a statement
                if(!StringUtils.isEmpty(line)) this.sourceCode.add(line);
            }
        }
    }

   static  List<String> readAndCleanSource(String fileContent){
        fileContent = fileContent.replaceAll("/\\*([\\S\\s\\n]+?)\\*/", ""); //remove comment blocks
        fileContent = fileContent.replaceAll("\\s*\n+", "\n"); //remove extra new lines
        List<String> lines = fileContent.split("\n")
        return lines
    }

    private List<String> getTokensFromLine(String line){
        List<String> tokens = []
        StringTokenizer tokenizer = new StringTokenizer(line, "{}()[].,;+-*/&<>=~", true)
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim()
            if(token.startsWith("\"") && token.endsWith("\"")){
                tokens.add(token)
            } else {
                String[] parts = token.split("\\s+")
                for(String part: parts){
                    part = part.trim()
                    if(!StringUtils.isEmpty(part)) tokens.add(part);
                }
            }
        }
        return tokens;
    }

    List<TokenInformation> getTokenizedTokens() {
        return tokenizedTokens
    }

    public void close(){
        if(fileOutputStream) fileOutputStream.close()
    }
}
