import com.bereket.compiler.CompilationEngine
import com.bereket.compiler.TokenInformation
import org.apache.commons.lang3.reflect.FieldUtils
import org.codehaus.groovy.reflection.ReflectionUtils
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
            <symbol> ; </symbol>
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
        List<String> expected = Arrays.asList("<class>", "<keyword>class</keyword>", "<identifier>Square</identifier>", "<symbol>{</symbol>", "<symbol>;</symbol>", "<symbol>}</symbol>", "</class>")
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
        </tokens>
        '''
        def parsedXml = new XmlSlurper().parseText(xml)
        CompilationEngine compilationEngine = new CompilationEngine();
        List<TokenInformation> tokenInformation = new ArrayList<>()
        compilationEngine.setTokenizedTokens(tokenInformation)
        CompilationEngine.readXmlToTokenInformation(parsedXml, tokenInformation)
        compilationEngine.compileSubroutineDec()
        List<String> programStructure = compilationEngine.getProgramStructure()
        List<String> expected = Arrays.asList("<subroutineDec>", "<keyword>constructor</keyword>", "<keyword>void</keyword>", "<identifier>method1</identifier>", "<symbol>(</symbol>", "<parameterList>", "</parameterList>", "<symbol>)</symbol>", "</subroutineDec>")
        //assertThat(programStructure, is(expected))
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
