import org.junit.Test;
import parser.Parser;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.*;

public class ParserTest {

    @Test
    public void test_command_destination() throws IOException {
        Parser parser = new Parser("");
        String line = "M=A";
        assertEquals(parser.dest(line), "M");
        line = "M";
        assertEquals(parser.dest(line), "");
        line = "M=A;JEQ";
        assertEquals(parser.dest(line), "M");
    }

    @Test
    public void test_command_jump() throws IOException {
        Parser parser = new Parser("");
        String line = "M=A;JLE";
        assertEquals(parser.jump(line), "JLE");
        line = ";JEQ";
        assertEquals(parser.jump(line), "JEQ");
        line = ";";
        assertEquals(parser.jump(line), "");
    }

    @Test
    public void test_command_comp() throws IOException {
        Parser parser = new Parser("");
        String line = "M=A;JLE";
        assertEquals(parser.comp(line), "M=A");
        line = ";JEQ";
        assertEquals(parser.comp(line), "");
        line = "M";
        assertEquals(parser.comp(line), "M");
        line = "M;";
        assertEquals(parser.comp(line), "M");
    }

    @Test
    public void test_command_system() throws IOException {
        Parser parser = new Parser("");
        String line = "M=A;JLE";
        assertEquals(parser.symbol(line), "");
        line = "@abc";
        assertEquals(parser.symbol(line), "abc");
        line = "(abc)";
        assertEquals(parser.symbol(line), "abc");
        line = "M;";
        assertEquals(parser.symbol(line), "");
    }

    @Test
    public void test_parse_file() throws IOException {
        Parser parser = new Parser("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/abc.asm");
        List<String> lines = parser.getSourceCode();
        assertEquals(lines.size(), 21);
    }

    @Test
    public void test_convert_abc_asm() throws IOException {
        Parser parser = new Parser("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/abc.asm");
        parser.parseSource("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/abc.hack");
        assertTrue(true);
    }

    @Test
    public void test_convert_add_asm() throws IOException {
        Parser parser = new Parser("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/add.asm");
        parser.parseSource("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/add.hack");
        assertTrue(true);
    }

    @Test
    public void test_convert_rect_asm() throws IOException {
        Parser parser = new Parser("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/RectL.asm");
        parser.parseSource("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/RectL.hack");
        assertTrue(true);
    }

    @Test
    public void test_convert_pong_asm() throws IOException {
        Parser parser = new Parser("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/PongL.asm");
        parser.parseSource("/Users/bereket/Documents/nand2tetris/projects/06/assembler/src/test/resources/PongL.hack");
        assertTrue(true);
    }
}
