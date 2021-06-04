import com.bereket.compiler.CompilationEngine
import com.bereket.compiler.TokenInformation
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.commons.lang3.reflect.MethodUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

public class CompilationEngineTest {

    @org.junit.jupiter.api.Test
    public void testCompileClass(){

        String xml = '''
        <tokens>
            <keyword> class </keyword>
            <identifier> Square </identifier>
            <symbol> { </symbol>            
            <symbol> } </symbol>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileClass()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<class>", "<keyword>class</keyword>", "<identifier>Square</identifier>", "<symbol>{</symbol>", "<symbol>}</symbol>", "</class>")
        //assertThat(programStructure, is(expected))
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    @org.junit.jupiter.api.Test
    public void testCompileClassVarDec(){

        String xml = '''
        <tokens>       
            <keyword> field </keyword>
            <identifier> Square </identifier>
            <identifier> square </identifier>
            <symbol> ; </symbol>
            <keyword> field </keyword>
            <keyword> int </keyword>
            <identifier> direction </identifier>
            <symbol> ; </symbol>
            <keyword> static </keyword>
            <keyword> boolean </keyword>
            <identifier> abc </identifier>
            <symbol> , </symbol>
            <identifier> xyz </identifier>
            <symbol> ; </symbol>
            <keyword> static </keyword>
            <keyword> char </keyword>
            <identifier> def </identifier>
            <symbol> ; </symbol>          
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        FieldUtils.writeField(compilationEngine, "currentIndex", 0, true);
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileClassVarDec()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<classVarDec>", "<keyword>field</keyword>", "<identifier>Square</identifier>", "<identifier>square</identifier>",
                "<symbol>;</symbol>", "</classVarDec>", "<classVarDec>", "<keyword>field</keyword>", "<keyword>int</keyword>", "<identifier>direction</identifier>",
                "<symbol>;</symbol>", "</classVarDec>", "<classVarDec>", "<keyword>static</keyword>", "<keyword>boolean</keyword>",
                "<identifier>abc</identifier>", "<symbol>,</symbol>", "<identifier>xyz</identifier>", "<symbol>;</symbol>", "</classVarDec>",
                "<classVarDec>", "<keyword>static</keyword>", "<keyword>char</keyword>",
                "<identifier>def</identifier>", "<symbol>;</symbol>", "</classVarDec>")
        //assertThat(programStructure, is(expected))
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

    }


    @Test
    public void testSubroutineDec(){
        String xml = '''
        <tokens>
            <keyword> constructor </keyword>
            <keyword> void </keyword>
            <identifier> method1 </identifier>
            <symbol> ( </symbol>
            <symbol> ) </symbol>
            <symbol> { </symbol>
            <keyword>let</keyword>
            <identifier>a</identifier>
            <symbol> = </symbol>
            <integerConstant>5</integerConstant>
            <symbol> ; </symbol>
            <symbol> } </symbol>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileSubroutineDec()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<subroutineDec>",
                "<keyword>constructor</keyword>",
                "<keyword>void</keyword>",
                "<identifier>method1</identifier>",
                "<symbol>(</symbol>",
                "<parameterList>",
                "</parameterList>",
                "<symbol>)</symbol>",
                "<subroutineBody>",
                "<symbol>{</symbol>",
                "<letStatement>",
                "<keyword>let</keyword>",
                "<identifier>a</identifier>",
                "<symbol>=</symbol>",
                "<expression>",
                "<term>",
                "<integerConstant>5</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>;</symbol>",
                "</letStatement>",
                "<symbol>}</symbol>",
                "</subroutineBody>",
                "</subroutineDec>")
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    @Test
    public void testCompileParameterList(){
        String xml = '''
        <tokens>
            <keyword> int </keyword>
            <identifier> a </identifier>
            <symbol> , </symbol>
            <keyword> char </keyword>
            <identifier> b </identifier>
            <symbol> , </symbol>
            <keyword> boolean </keyword>
            <identifier> c </identifier>
            <symbol> , </symbol>
            <identifier> SomeClass </identifier>
            <identifier> d </identifier>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileParameterList()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<parameterList>", "<keyword>int</keyword>", "<identifier>a</identifier>", "<symbol>,</symbol>", "<keyword>char</keyword>", "<identifier>b</identifier>",
                "<symbol>,</symbol>", "<keyword>boolean</keyword>", "<identifier>c</identifier>", "<symbol>,</symbol>", "<identifier>SomeClass</identifier>", "<identifier>d</identifier>", "</parameterList>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }


    @org.junit.jupiter.api.Test
    public void testCompileVarDec(){

        String xml = '''
        <tokens>       
            <keyword> var </keyword>
            <identifier> Square </identifier>
            <identifier> square </identifier>
            <symbol> , </symbol>
            <identifier> direction </identifier>
            <symbol> , </symbol>
            <identifier> abc </identifier>
            <symbol> ; </symbol>        
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        FieldUtils.writeField(compilationEngine, "currentIndex", 0, true);
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileVarDec()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<varDec>", "<keyword>var</keyword>", "<identifier>Square</identifier>", "<identifier>square</identifier>",
                "<symbol>,</symbol>", "<identifier>direction</identifier>", "<symbol>,</symbol>",
                "<identifier>abc</identifier>", "<symbol>;</symbol>", "</varDec>")
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        xml = '''
        <tokens>       
            <keyword> var </keyword>
            <identifier> Square </identifier>
            <identifier> square </identifier>
            <symbol> ; </symbol>
            <keyword> var </keyword>
            <identifier> int </identifier>
            <identifier> abc </identifier>
            <symbol> ; </symbol>    
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        FieldUtils.writeField(compilationEngine, "currentIndex", 0, true);
        FieldUtils.writeField(compilationEngine, "programStructure", new ArrayList(), true);
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileVarDec()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList("<varDec>", "<keyword>var</keyword>", "<identifier>Square</identifier>", "<identifier>square</identifier>", "<symbol>;</symbol>", "</varDec>",
               "<varDec>", "<keyword>var</keyword>", "<identifier>int</identifier>", "<identifier>abc</identifier>", "<symbol>;</symbol>", "</varDec>")
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }


    @org.junit.jupiter.api.Test
    public void testCompileSubroutine(){

        String xml = '''
        <tokens> 
            <symbol>{</symbol>                
            <symbol>}</symbol>        
        </tokens>
        '''

        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        FieldUtils.writeField(compilationEngine, "currentIndex", 0, true);
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileSubroutineBody()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<subroutineBody>", "<symbol>{</symbol>", "<symbol>}</symbol>", "</subroutineBody>")
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        xml = '''
        <tokens> 
            <symbol>{</symbol>      
            <keyword> var </keyword>
            <identifier> Square </identifier>
            <identifier> square </identifier>
            <symbol> , </symbol>
            <identifier> direction </identifier>
            <symbol> , </symbol>
            <identifier> abc </identifier>
            <symbol> ; </symbol>
            <symbol>}</symbol>        
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        FieldUtils.writeField(compilationEngine, "currentIndex", 0, true);
        FieldUtils.writeField(compilationEngine, "programStructure", new ArrayList(), true);
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileSubroutineBody()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList("<subroutineBody>", "<symbol>{</symbol>",
                "<varDec>", "<keyword>var</keyword>", "<identifier>Square</identifier>", "<identifier>square</identifier>",
                "<symbol>,</symbol>", "<identifier>direction</identifier>", "<symbol>,</symbol>",
                "<identifier>abc</identifier>", "<symbol>;</symbol>", "</varDec>",
                "<symbol>}</symbol>", "</subroutineBody>")
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

    }

    @Test
    public void testCompileIf(){
        String xml = '''
        <tokens>
            <keyword> if </keyword>          
            <symbol> ( </symbol> 
            <identifier> a </identifier>   
            <symbol> &lt; </symbol>  
            <integerConstant> 2 </integerConstant>     
            <symbol> ) </symbol>
            <symbol> { </symbol>
            <keyword> let </keyword> 
            <identifier> d </identifier>   
            <symbol> = </symbol>  
            <integerConstant> 5 </integerConstant>  
            <symbol> ; </symbol>
            <symbol> } </symbol>
            <keyword> else </keyword>
            <symbol> { </symbol>
            <keyword> let </keyword> 
            <identifier> d </identifier>   
            <symbol> = </symbol>  
            <identifier> d </identifier>   
            <symbol> - </symbol>
            <integerConstant> 5 </integerConstant>   
            <symbol> ; </symbol>          
            <symbol> } </symbol>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileIf()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<ifStatement>",
                "<keyword>if</keyword>",
                "<symbol>(</symbol>",
                "<expression>",
                "<term>",
                "<identifier>a</identifier>",
                "</term>",
                "<symbol>&lt;</symbol>",
                "<term>",
                "<integerConstant>2</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>)</symbol>",
                "<symbol>{</symbol>",
                "<letStatement>",
                "<keyword>let</keyword>",
                "<identifier>d</identifier>",
                "<symbol>=</symbol>",
                "<expression>",
                "<term>",
                "<integerConstant>5</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>;</symbol>",
                "</letStatement>",
                "<symbol>}</symbol>",
                "<keyword>else</keyword>",
                "<symbol>{</symbol>",
                "<letStatement>",
                "<keyword>let</keyword>",
                "<identifier>d</identifier>",
                "<symbol>=</symbol>",
                "<expression>",
                "<term>",
                "<identifier>d</identifier>",
                "</term>",
                "<symbol>-</symbol>",
                "<term>",
                "<integerConstant>5</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>;</symbol>",
                "</letStatement>",
                "<symbol>}</symbol>",
                "</ifStatement>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    @Test
    public void testCompileWhile(){

    String xml = '''
    <tokens>
        <keyword> while </keyword>          
        <symbol> ( </symbol> 
        <identifier> a </identifier>   
        <symbol> &lt; </symbol>  
        <integerConstant> 2 </integerConstant>     
        <symbol> ) </symbol>
        <symbol> { </symbol>
        <keyword> let </keyword> 
        <identifier> d </identifier>   
        <symbol> = </symbol>  
        <integerConstant> 5 </integerConstant>  
        <symbol> ; </symbol>
        <symbol> } </symbol>
    </tokens>
    '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileWhile()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<whileStatement>",
                "<keyword>while</keyword>",
                "<symbol>(</symbol>",
                "<expression>",
                "<term>",
                "<identifier>a</identifier>",
                "</term>",
                "<symbol>&lt;</symbol>",
                "<term>",
                "<integerConstant>2</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>)</symbol>",
                "<symbol>{</symbol>",
                "<letStatement>",
                "<keyword>let</keyword>",
                "<identifier>d</identifier>",
                "<symbol>=</symbol>",
                "<expression>",
                "<term>",
                "<integerConstant>5</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>;</symbol>",
                "</letStatement>",
                "<symbol>}</symbol>",
                "</whileStatement>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }


    @Test
    public void testCompileDo(){
        String xml = '''
        <tokens>
            <keyword> do </keyword>          
            <symbol> ; </symbol>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileDo()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<doStatement>", "<keyword>do</keyword>", "<symbol>;</symbol>", "</doStatement>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }


    @Test
    public void testCompileReturn(){
        String xml = '''
        <tokens>
            <keyword> return </keyword>          
            <symbol> ; </symbol>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileReturn()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<returnStatement>", "<keyword>return</keyword>", "<symbol>;</symbol>", "</returnStatement>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        xml = '''
        <tokens>
            <keyword> return </keyword>
            <integerConstant>1</integerConstant>
            <symbol>+</symbol>
            <identifier>d</identifier>          
            <symbol> ; </symbol>
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        compilationEngine = new CompilationEngine();
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileReturn()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList("<returnStatement>",
                "<keyword>return</keyword>",
                "<expression>",
                "<term>",
                "<integerConstant>1</integerConstant>",
                "</term>",
                "<symbol>+</symbol>",
                "<term>",
                "<identifier>d</identifier>",
                "</term>",
                "</expression>",
                "<symbol>;</symbol>",
                "</returnStatement>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }


    @Test
    public void testKeywordConstant(){
        String xml = '''
        <tokens>                     
            <keyword> true </keyword>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        MethodUtils.invokeMethod(compilationEngine, true, "compileKeywordConstant")
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<keyword>true</keyword>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    @Test
    public void testBinaryOperator(){
        String xml = '''
        <tokens>                     
            <symbol> + </symbol>
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        MethodUtils.invokeMethod(compilationEngine, true, "compileBinaryOperator")
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<symbol>+</symbol>")

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    //test integer, string and keyword constants
    @Test
    public void testCompleTerm1(){
        String xml = '''
        <tokens>                     
            <integerConstant> 1 </integerConstant>         
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<term>",
                "<integerConstant>1</integerConstant>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        xml = '''
        <tokens>                     
            <keyword> null </keyword>  
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        compilationEngine = new CompilationEngine();
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList(
                "<term>",
                "<keyword>null</keyword>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        xml = '''
        <tokens>                     
            <stringConstant> bereket </stringConstant>  
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        compilationEngine = new CompilationEngine();
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList(
                "<term>",
                "<stringConstant>bereket</stringConstant>",
                "</term>",
        )

        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    //test single variable name
    @Test
    public void testCompleTerm2(){
        String xml = '''
        <tokens>                     
            <identifier> abc </identifier>         
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<term>",
                "<identifier>abc</identifier>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    //test simple array expression abc[1]
    @Test
    public void testCompleTerm3(){
        String xml = '''
        <tokens>                     
            <identifier> abc </identifier>       
            <symbol>[</symbol>  
            <integerConstant>1</integerConstant>
            <symbol>]</symbol>  
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<term>",
                "<identifier>abc</identifier>",
                "<symbol>[</symbol>",
                "<expression>",
                "<term>",
                "<integerConstant>1</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>]</symbol>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        //complex expression in an array
        xml = '''
        <tokens>                     
            <identifier> abc </identifier>       
            <symbol>[</symbol>  
            <integerConstant>1</integerConstant>
            <symbol>+</symbol>
            <identifier>d</identifier>
            <symbol>-</symbol>
            <identifier>e</identifier>
            <symbol>]</symbol>  
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        compilationEngine = new CompilationEngine();
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList(
                "<term>",
                "<identifier>abc</identifier>",
                "<symbol>[</symbol>",
                "<expression>",
                "<term>",
                "<integerConstant>1</integerConstant>",
                "</term>",
                "<symbol>+</symbol>",
                "<term>",
                "<identifier>d</identifier>",
                "</term>",
                "<symbol>-</symbol>",
                "<term>",
                "<identifier>e</identifier>",
                "</term>",
                "</expression>",
                "<symbol>]</symbol>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    //test unary operator
    @Test
    public void testCompleTerm4(){
        String xml = '''
        <tokens>                     
            <symbol> ~ </symbol>         
            <identifier> abc </identifier>         
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<term>",
                "<symbol>~</symbol>",
                "<term>",
                "<identifier>abc</identifier>",
                "</term>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    //test subroutineCall
    @Test
    public void testCompleTerm5(){
        String xml = '''
        <tokens>                                  
            <identifier> subRoutine </identifier>         
            <symbol> ( </symbol>
            <symbol> ) </symbol>                
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<term>",
                "<identifier>subRoutine</identifier>",
                "<symbol>(</symbol>",
                "<expressionList>",
                "</expressionList>",
                "<symbol>)</symbol>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        xml = '''
        <tokens>                                  
            <identifier> SomeClass </identifier>  
            <symbol> . </symbol>       
            <identifier> someMethod </identifier>         
            <symbol> ( </symbol>
            <integerConstant> 2 </integerConstant>   
            <symbol> ) </symbol>                
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        compilationEngine = new CompilationEngine();
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileTerm()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList(
                "<term>",
                "<identifier>SomeClass</identifier>",
                "<symbol>.</symbol>",
                "<identifier>someMethod</identifier>",
                "<symbol>(</symbol>",
                "<expressionList>",
                "<expression>",
                "<term>",
                "<integerConstant>2</integerConstant>",
                "</term>",
                "</expression>",
                "</expressionList>",
                "<symbol>)</symbol>",
                "</term>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    //test basic expression a + x
    @Test
    public void testCompileExpression(){
        String xml = '''
        <tokens>                              
            <integerConstant> 2 </integerConstant>   
            <symbol> + </symbol>
            <identifier> b </identifier>               
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileExpression()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<expression>",
                "<term>",
                "<integerConstant>2</integerConstant>",
                "</term>",
                "<symbol>+</symbol>",
                "<term>",
                "<identifier>b</identifier>",
                "</term>",
                "</expression>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

        xml = '''
        <tokens>                              
            <integerConstant> 2 </integerConstant>   
            <symbol> + </symbol>
            <identifier> b </identifier>    
             <symbol> + </symbol>
            <identifier> d </identifier>              
        </tokens>
        '''
        parsedXml = new XmlSlurper().parseText(xml)
        compilationEngine = new CompilationEngine();
        tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileExpression()
        programStructure = compilationEngine.getProgramStructure()
        expected = Arrays.asList(
                "<expression>",
                "<term>",
                "<integerConstant>2</integerConstant>",
                "</term>",
                "<symbol>+</symbol>",
                "<term>",
                "<identifier>b</identifier>",
                "</term>",
                "<symbol>+</symbol>",
                "<term>",
                "<identifier>d</identifier>",
                "</term>",
                "</expression>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    //test let statement
    @Test
    public void testCompileLet(){
        String xml = '''
        <tokens>                     
            <keyword> let </keyword>         
            <identifier> abc </identifier>         
            <symbol> [ </symbol> 
            <identifier> index </identifier>         
            <symbol> ] </symbol>         
            <symbol> = </symbol>  
            <identifier> a </identifier>  
            <symbol> + </symbol>     
            <integerConstant> 2 </integerConstant>     
            <symbol> ; </symbol>         
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileLet()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList(
                "<letStatement>",
                "<keyword>let</keyword>",
                "<identifier>abc</identifier>",
                "<symbol>[</symbol>",
                "<expression>",
                "<term>",
                "<identifier>index</identifier>",
                "</term>",
                "</expression>",
                "<symbol>]</symbol>",
                "<symbol>=</symbol>",
                "<expression>",
                "<term>",
                "<identifier>a</identifier>",
                "</term>",
                "<symbol>+</symbol>",
                "<term>",
                "<integerConstant>2</integerConstant>",
                "</term>",
                "</expression>",
                "<symbol>;</symbol>",
                "</letStatement>",
        )
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());
    }

    @org.junit.jupiter.api.Test
    public void testAll(){

        String xml = '''
        <tokens>               
            <keyword> class </keyword>
            <identifier> Square </identifier>
            <symbol> { </symbol>
            <keyword> field </keyword>
            <identifier> Square </identifier>
            <identifier> square </identifier>
            <symbol> ; </symbol>
            <keyword> field </keyword>
            <keyword> int </keyword>
            <identifier> direction </identifier>
            <symbol> ; </symbol>
            <keyword> static </keyword>
            <keyword> boolean </keyword>
            <identifier> abc </identifier>
            <symbol> , </symbol>
            <identifier> xyz </identifier>
            <symbol> ; </symbol>
            <keyword> static </keyword>
            <keyword> char </keyword>
            <identifier> def </identifier>
            <symbol> ; </symbol>
            <symbol> } </symbol>                                                              
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        FieldUtils.writeField(compilationEngine, "currentIndex", 0, true);
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileClass()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<class>", "<keyword>class</keyword>", "<identifier>Square</identifier>", "<symbol>{</symbol>",
                "<classVarDec>", "<keyword>field</keyword>", "<identifier>Square</identifier>", "<identifier>square</identifier>",
                "<symbol>;</symbol>", "</classVarDec>", "<classVarDec>", "<keyword>field</keyword>", "<keyword>int</keyword>", "<identifier>direction</identifier>",
                "<symbol>;</symbol>", "</classVarDec>", "<classVarDec>", "<keyword>static</keyword>", "<keyword>boolean</keyword>",
                "<identifier>abc</identifier>", "<symbol>,</symbol>", "<identifier>xyz</identifier>", "<symbol>;</symbol>", "</classVarDec>",
                "<classVarDec>", "<keyword>static</keyword>", "<keyword>char</keyword>",
                "<identifier>def</identifier>", "<symbol>;</symbol>", "</classVarDec>",
                "<symbol>}</symbol>", "</class>")
        Assertions.assertArrayEquals(programStructure.toArray(), expected.toArray());

    }
}
