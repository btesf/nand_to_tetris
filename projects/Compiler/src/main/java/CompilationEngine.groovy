import org.apache.commons.lang3.StringUtils

class CompilationEngine {

    private FileOutputStream fileOutputStream
    private List<String> sourceCode = new ArrayList<>();

    public CompilationEngine(String inputFileName, String outputFileName){
        List<String> rawLines = new File(inputFileName).readLines()
        fileOutputStream = new FileOutputStream(outputFileName)
    }

    public void compileClass(){

    }

    public void compileClassVarDec(){

    }

    public void compileSubroutine(){

    }

    public void compileParameterList(){

    }

    public void compileVarDec(){

    }

    public void compileStatements(){

    }

    public void compileDo(){

    }

    public void compileLet(){

    }

    public void compileWhile(){

    }

    public void compileReturn(){

    }

    public void compileIf(){

    }

    public void compileExpression(){

    }

    public void compileTerm(){

    }

    public void compileExpressionList(){

    }

    public void close(){
        fileOutputStream.close()
    }
}
