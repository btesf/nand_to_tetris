import com.bereket.compiler.JackTokenizer
import com.bereket.compiler.TokenInformation
import com.bereket.compiler.TokenType
import org.junit.Test
import org.junit.jupiter.api.Assertions

import static org.junit.Assert.assertEquals

public class TokenizerTest {

    @Test
    public void test_command_destination() {

        String source = '''
        /** 34:   */
        
        /*
        
         * JD-Core Version:    0.7.0.1
        
         */
        
         abc
        
         def //addkdkf
        
         /* 34:   */
        
        /*
        
         * JD-Core Version:    0.7.0.1
        
         */
        
         /* adjd  */
        '''



        JackTokenizer jackTokenizer = new JackTokenizer()
        jackTokenizer.filterSourceFile(JackTokenizer.readAndCleanSource(source))
        List<String> filteredLines = jackTokenizer.getSourceCode();
        assertEquals(filteredLines.get(0),"abc")
        assertEquals(filteredLines.get(1),"def")
    }


    @Test
    public void test_class() {

        String source = '''
        class Square {
                    field Square square;
                    field int direction;
                    static boolean abc, xyz;
                    static char def;

                    constructor void method1(){
                        let a = 5;
                        do ClassName.MethodName(2 + "abc def");
                    }
                }
        '''

        List<TokenInformation> expectedTokens = new ArrayList<>();
        expectedTokens.add(new TokenInformation('class',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('Square',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation('{',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('field',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('Square',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation('square',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation(';',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('field',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('int',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('direction',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation(';',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('static',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('boolean',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('abc',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation(',',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('xyz',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation(';',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('static',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('char',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('def',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation(';',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('constructor',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('void',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('method1',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation('(',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation(')',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('{',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('let',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('a',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation('=',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('5',TokenType.INT_CONST));
        expectedTokens.add(new TokenInformation(';',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('do',TokenType.KEYWORD));
        expectedTokens.add(new TokenInformation('ClassName',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation('.',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('MethodName',TokenType.IDENTIFIER));
        expectedTokens.add(new TokenInformation('(',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('2',TokenType.INT_CONST));
        expectedTokens.add(new TokenInformation('+',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('abc def',TokenType.STRING_CONST));
        expectedTokens.add(new TokenInformation(')',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation(';',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('}',TokenType.SYMBOL));
        expectedTokens.add(new TokenInformation('}',TokenType.SYMBOL));


        JackTokenizer jackTokenizer = new JackTokenizer()
        jackTokenizer.filterSourceFile(JackTokenizer.readAndCleanSource(source))
        jackTokenizer.tokenize();
        List<TokenInformation> actualTokens = jackTokenizer.getTokenizedTokens()
        Assertions.assertArrayEquals(actualTokens.toArray(), expectedTokens.toArray());
    }
}
