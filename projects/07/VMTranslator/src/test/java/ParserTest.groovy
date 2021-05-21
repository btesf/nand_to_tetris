import java.util.ArrayList;
import java.util.List;
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail;

public class ParserTest {

    @Test
    public void test_command_destination() {
        List<String> sourceLines = []
        sourceLines << "function fact 2";
        sourceLines << "push constant 1";
        sourceLines << "pop local 0";
        sourceLines << "label loop";
        sourceLines << "if-goto end";
        sourceLines << "call mult 2";
        sourceLines << "return";
        sourceLines << "push constant 4001";
        Parser parser = new Parser(sourceLines);

        //function fact 2
        assertEquals(parser.commandType(), CommandType.C_FUNCTION)
        assertEquals(parser.arg1(), "fact")
        assertEquals(parser.arg2(), 2)
        //push constant 1
        parser.advance()
        assertEquals(parser.commandType(), CommandType.C_PUSH)
        assertEquals(parser.arg1(), "constant")
        assertEquals(parser.arg2(), 1)
        //pop local 0
        parser.advance()
        assertEquals(parser.commandType(), CommandType.C_POP)
        assertEquals(parser.arg1(), "local")
        assertEquals(parser.arg2(), 0)
        //label loop
        parser.advance()
        assertEquals(parser.commandType(), CommandType.C_LABEL)
        assertEquals(parser.arg1(), "loop")
        //if-goto end
        parser.advance()
        assertEquals(parser.commandType(), CommandType.C_IF)
        assertEquals(parser.arg1(), "end")
        //call mult 2
        parser.advance()
        assertEquals(parser.commandType(), CommandType.C_CALL)
        assertEquals(parser.arg1(), "mult")
        assertEquals(parser.arg2(), 2)
        //return
        parser.advance()
        assertEquals(parser.commandType(), CommandType.C_RETURN)
        try{
            assertEquals(parser.arg1(), "return")
            fail("OperationNotSupportedException should be thrown here")
        } catch(Exception e){}

        //push constant 4001
        parser.advance()
        assertEquals(parser.commandType(), CommandType.C_PUSH)
        assertEquals(parser.arg1(), "constant")
        assertEquals(parser.arg2(), 4001)
    }
}
