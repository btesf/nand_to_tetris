package com.bereket.compiler

public class TokenInformation {

    private final Map<String, String> specialCharacterCodedLookup = ['<': '&lt;', '>': '&gt;', '"': '&quot;', '&': '&amp;']
    String token
    TokenType tokenType

    TokenInformation(String token, TokenType tokenType){
        if(tokenType == TokenType.STRING_CONST){
            if(token.length() < 2) throw new RuntimeException("String token format is not valid : " + token);
            //token = token.substring(1, token.length() -1) //remove the leading and trailing double quotes
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

    @Override
    String toString() {
        return "${token} : ${tokenType}"
    }
}
