import com.bereket.compiler.CompilationEngine
import com.bereket.compiler.IdentifierKind
import com.bereket.compiler.JackTokenizer
import com.bereket.compiler.SymbolTable
import com.bereket.compiler.TokenInformation
import org.apache.commons.lang3.reflect.FieldUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

public class CompilationEngineTest {

    @org.junit.jupiter.api.Test
    public void testSubroutineTable(){

        String source ="""
                class Square {
                    field Square square;
                    field int direction;
                    static boolean abc, xyz;
                    static char def;

                    constructor void method1(){
                        var Square method1Square;
                        var char method1Char;                       
                    }
                    
                    method void method2(){                      
                        var char method2Char;
                        var int method2Int;                       
                    }
                }
         """

        JackTokenizer jackTokenizer = new JackTokenizer()
        jackTokenizer.filterSourceFile(JackTokenizer.readAndCleanSource(source))
        jackTokenizer.tokenize()

        CompilationEngine compilationEngine = new CompilationEngine();
        compilationEngine.setTokenizedTokens(jackTokenizer.getTokenizedTokens())

        compilationEngine.compileClass()

        SymbolTable symbolTable = compilationEngine.getSymbolTable()
        SymbolTable.SymbolEntry entry =  symbolTable.getSymbolEntry("method1Square")
        //method1Square should not exist - it is out of scope
        Assertions.assertNull(entry)

        entry =  symbolTable.getSymbolEntry("method1Char")
        //method1Char should not exist - it is out of scope
        Assertions.assertNull(entry)

        entry =  symbolTable.getSymbolEntry("method2Char")
        Assertions.assertEquals(entry.type, "char")
        Assertions.assertEquals(entry.kind, IdentifierKind.VAR)

        entry =  symbolTable.getSymbolEntry("method2Int")
        Assertions.assertEquals(entry.type, "int")
        Assertions.assertEquals(entry.kind, IdentifierKind.VAR)
    }

}
