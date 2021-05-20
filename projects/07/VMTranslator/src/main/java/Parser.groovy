import org.apache.commons.lang3.StringUtils
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import javax.naming.OperationNotSupportedException

class Parser {

    private final List<String> ARITHMETIC_COMMANDS = ["add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"];
    private List<String> sourceCode = new ArrayList<>();
    private int lineCounter = 0;

    public Parser(String sourceFile){
        List<String> rawLines = new File(sourceFile).readLines()
        filterSourceFile(rawLines)
    }

    public Parser(List<String> sourceCode){
        this.sourceCode = sourceCode;
    }

    public boolean hasMoreCommands(){
        return this.lineCounter < this.sourceCode.size();
    }

    public void advance(){
        this.lineCounter++;
    }

    CommandType commandType(){
        String line = getCurrentLine()
        commandType(line)
    }

    CommandType commandType(String line){
        String[] tokens = line.split("\\s+")
        if(tokens.length == 0) throw new RuntimeException("Line ${this.lineCounter} is empty")
        if(ARITHMETIC_COMMANDS.contains(tokens[0].toLowerCase())){
            return CommandType.C_ARITHMETIC;
        }
        if("push" == tokens[0].toLowerCase()){
            return CommandType.C_PUSH;
        }
        if("pop"== tokens[0].toLowerCase()){
            return CommandType.C_POP;
        }
        if("label" == tokens[0].toLowerCase()){
            return CommandType.C_LABEL;
        }
        if("goto" == tokens[0].toLowerCase()){
            return CommandType.C_GOTO;
        }
        if("if-goto" == tokens[0].toLowerCase()){
            return CommandType.C_IF;
        }
        if("function" == tokens[0].toLowerCase()){
            return CommandType.C_FUNCTION;
        }
        if("return" == tokens[0].toLowerCase()){
            return CommandType.C_RETURN;
        }
        if("call" == tokens[0].toLowerCase()){
            return CommandType.C_CALL;
        }
        throw new OperationNotSupportedException("${tokens[0]} Not implemented".toString());
    }

    String arg1(){
        String line = getCurrentLine();
        String[] tokens = line.split("\\s+")
        CommandType commandType = commandType(line)
        switch(commandType){
            case CommandType.C_ARITHMETIC:
                return tokens[0];
            case CommandType.C_POP:
            case CommandType.C_PUSH:
            case CommandType.C_GOTO:
            case CommandType.C_IF:
            case CommandType.C_FUNCTION:
            case CommandType.C_CALL:
            case CommandType.C_LABEL:
                return tokens[1]
            default:
                throw new OperationNotSupportedException("arg1 cannot be determined for command type ${commandType}");
        }
    }

    int arg2(){
        String line = getCurrentLine();
        String[] tokens = line.split("\\s+")
        CommandType commandType = commandType(line)
        if(commandType == CommandType.C_POP || commandType == CommandType.C_PUSH ||
                commandType == CommandType.C_FUNCTION || commandType == CommandType.C_CALL){
            String arg2 = tokens[2]
            return Integer.valueOf(arg2).intValue()
        }
        else throw new OperationNotSupportedException("arg2 cannot be determined for command type ${commandType}");
    }

    private String getCurrentLine(){
        return this.sourceCode.get(lineCounter);
    }

    private void filterSourceFile(List<String> rawLines) {
        for(String line : rawLines){
            if(!StringUtils.isEmpty(line)) line.trim()
            if(!StringUtils.isEmpty(line)){
                if(line.startsWith("//")) continue; //remove comment lines
                line = line.replaceAll("//.*", ""); //remove comments at the end of a statement
                if(!StringUtils.isEmpty(line)) this.sourceCode.add(line);
            }
        }
    }
}
