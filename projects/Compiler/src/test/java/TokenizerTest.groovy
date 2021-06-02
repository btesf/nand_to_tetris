import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail;

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


        String abc = "int i = 0; while(2=x){s=s+1;2;3<2; one & two =~|[23]};.one.,three,";
        StringTokenizer tokenizer = new StringTokenizer(abc, "s+{}()[].,;+-*/&<>=~", true)
        while (tokenizer.hasMoreTokens()) {
            System.out.println(tokenizer.nextToken("\n"));
            println "bereket \n"
            //tokenizer.nextToken("\n")
            println ""
        }


        /*List<String> str1 = abc.tokenize("\\s+|\\{|\\}|\\(|\\)|\\[|\\]|\\.|,|;|\\+|-|\\*|/|&|<|>|=|~|\\|");
        for(String s : str1){
            println s
        }*/
    }
}
