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
        if("push".contains(tokens[0].toLowerCase())){
            return CommandType.C_PUSH;
        }
        if("pop".contains(tokens[0].toLowerCase())){
            return CommandType.C_POP;
        }
        throw new OperationNotSupportedException("${tokens[0]} Not implemented".toString());
    }

    String arg1(){
        String line = getCurrentLine();
        String[] tokens = line.split("\\s+")
        CommandType commandType = commandType(line)
        if(commandType == CommandType.C_ARITHMETIC) return tokens[0]
        else if(commandType == CommandType.C_POP || commandType == CommandType.C_PUSH) return tokens[1]
        throw new OperationNotSupportedException("arg1 cannot be determined for command type ${commandType}");
    }

    int arg2(){
        String line = getCurrentLine();
        String[] tokens = line.split("\\s+")
        CommandType commandType = commandType(line)
        if(commandType == CommandType.C_POP || commandType == CommandType.C_PUSH){
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
