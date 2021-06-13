package com.bereket.compiler

public class TokenInformation {

    private final Map<String, String> specialCharacterCodedLookup = ['<': '&lt;', '>': '&gt;', '"': '&quot;', '&': '&amp;']
    private final Map<String, String> specialCharacterReverseLookup = ['&lt;' : '<', '&gt;' : '>',  '&quot;' : '"', '&amp;' : '&']
    String token
    TokenType tokenType

    TokenInformation(String token, TokenType tokenType){
        if(tokenType == TokenType.STRING_CONST){
            token = token.replaceAll("^\"|\"\$", "");//remove the leading double quote (")
        } else if(tokenType == TokenType.SYMBOL){
            token = getEncodedString(token)
        }
        this.token = token
        this.tokenType = tokenType
    }

    private String getEncodedString(String token){
        if(specialCharacterCodedLookup.containsKey(token)){
            return specialCharacterCodedLookup.get(token)
        }
        return token
    }

    public String getDecodedString(){
        if(tokenType == TokenType.SYMBOL){
           String value = specialCharacterReverseLookup.get(token)
           if(value) return value
        }
        return token
    }

    @Override
    String toString() {
        return "${token} : ${tokenType}"
    }

    @Override
    boolean equals(Object obj) {
        if(!(obj instanceof TokenInformation)) return false;
        TokenInformation tokenInformation = (TokenInformation) obj;
        if(tokenInformation.token == this.token && tokenInformation.tokenType == this.tokenType) return true;
        return false;
    }
}
