package parser;

import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Parser {

    private List<String> sourceCode = new ArrayList<>();
    private int lineCounter = 0;
    private Map<String, String> compRef = new HashMap<>();
    private Map<String, String> jumpRef = new HashMap<>();
    private Map<String, String> destRef = new HashMap<>();
    private Map<String, String> symbolTable = new HashMap<>();
    private List<String> variables = new ArrayList<>();
    private final int VARIABLE_OFFSET = 16;
    {
        compRef.put("0", "0101010");
        compRef.put("1", "0111111");
        compRef.put("-1", "0111010");
        compRef.put("D", "0001100");
        compRef.put("A", "0110000");
        compRef.put("M", "1110000");
        compRef.put("!D", "0001101");
        compRef.put("!A", "0110001");
        compRef.put("!M", "1110001");
        compRef.put("-D", "0001111");
        compRef.put("-A", "0110011");
        compRef.put("-M", "1110011");
        compRef.put("D+1", "0011111");
        compRef.put("A+1", "0110111");
        compRef.put("M+1", "1110111");
        compRef.put("D-1", "0001110");
        compRef.put("A-1", "0110010");
        compRef.put("M-1", "1110010");
        compRef.put("D+A", "0000010");
        compRef.put("D+M", "1000010");
        compRef.put("D-A", "0010011");
        compRef.put("D-M", "1010011");
        compRef.put("A-D", "0000111");
        compRef.put("M-D", "1000111");
        compRef.put("D&A", "0000000");
        compRef.put("D&M", "1000000");
        compRef.put("D|A", "0010101");
        compRef.put("D|M", "1010101");

        jumpRef.put("JGT", "001");
        jumpRef.put("JEQ", "010");
        jumpRef.put("JGE", "011");
        jumpRef.put("JLT", "100");
        jumpRef.put("JNE", "101");
        jumpRef.put("JLE", "110");
        jumpRef.put("JMP", "111");
        jumpRef.put("", "000");

        destRef.put("M", "001");
        destRef.put("D", "010");
        destRef.put("MD", "011");
        destRef.put("A", "100");
        destRef.put("AM", "101");
        destRef.put("AD", "110");
        destRef.put("AMD", "111");
        destRef.put("", "000");

        symbolTable.put("SP", "0");
        symbolTable.put("LCL", "1");
        symbolTable.put("ARG", "2");
        symbolTable.put("THIS", "3");
        symbolTable.put("THAT", "4");
        symbolTable.put("SCREEN", "16384");
        symbolTable.put("KBD", "24576");
        symbolTable.put("R0", "0");
        symbolTable.put("R1", "1");
        symbolTable.put("R2", "2");
        symbolTable.put("R3", "3");
        symbolTable.put("R4", "4");
        symbolTable.put("R5", "5");
        symbolTable.put("R6", "6");
        symbolTable.put("R7", "7");
        symbolTable.put("R8", "8");
        symbolTable.put("R9", "9");
        symbolTable.put("R10", "10");
        symbolTable.put("R11", "11");
        symbolTable.put("R12", "12");
        symbolTable.put("R13", "13");
        symbolTable.put("R14", "14");
        symbolTable.put("R15", "15");
    }

    public List<String> getSourceCode() {
        return sourceCode;
    }

    private String currentLine = "";

    public Parser(String inputFile) throws IOException {
        List<String> rawLines = Files.readAllLines(Paths.get(inputFile));
        filterSourceFile(rawLines);

    }

    public void filterSourceFile(List<String> rawLines) {
        for(String line : rawLines){
            line = line.trim().replaceAll("\\s+", "");
            if(!StringUtils.isEmpty(line)){
                if(line.startsWith("//")) continue;
                line = line.replaceAll("//.*", "");
                if(!StringUtils.isEmpty(line)) this.sourceCode.add(line);
            }
        }
    }

    public boolean hasMoreCommands(){
        return this.lineCounter < this.sourceCode.size();
    }

    public void advance(){
        this.lineCounter++;
    }

    public CommandType commandType(String line){
        char character = line.charAt(0);
        switch(character){
            case '@':
                return CommandType.A_COMMAND;
            case '(':
                return CommandType.L_COMMAND;
            default:
                return CommandType.C_COMMAND;
        }
    }

    public String symbol(String line){
        if(commandType(line) == CommandType.C_COMMAND)  return "";
        //remove all (, ) and @ chars
        return line.replaceAll("[()@]+", "");
    }

    public String comp(String line){
        if(commandType(line) != CommandType.C_COMMAND) return "";
        if(line.contains(";")){
            int index = line.indexOf(";");
            if(index >= 0){
                line = line.replaceAll(";.*", "");
            }
        }
        if(line.contains("=")){
            int index = line.indexOf("=");
            if(index >= 0){
                line = line.substring(index+1);
            }
        }
        return line;
    }

    public String jump(String line){
        if(commandType(line) == CommandType.C_COMMAND) {
            if(line.contains(";")){
                int index = line.indexOf(";");
                if(index >= 0){
                    return line.substring(index+1);
                }
            }
        }
        return "";
    }

    public String dest(String line){
        if(commandType(line) == CommandType.C_COMMAND) {
            if(line.contains("=")){
                int index = line.indexOf("=");
                if(index >= 0){
                    return line.substring(0, index);
                }
            }
        }
        return "";
    }

    private String getCurrentLine(){
        return this.sourceCode.get(lineCounter);
    }

    public void parseSource(String outputFile) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        scanJumpLabels();
        scanSymbols();
        while(hasMoreCommands()){
            String parsedLine = "";
            CommandType commandType = commandType(getCurrentLine());
            switch(commandType){
                case A_COMMAND:
                    String symbol = symbol(getCurrentLine());
                    if(symbol.matches("[0-9]+")) {
                        parsedLine = Integer.toBinaryString(Integer.parseInt(symbol));
                        parsedLine = "0" + StringUtils.leftPad(parsedLine, 15, '0');
                    } else {
                        String memoryAddress = symbolTable.get(symbol);
                        parsedLine = Integer.toBinaryString(Integer.parseInt(memoryAddress));
                        parsedLine = "0" + StringUtils.leftPad(parsedLine, 15, '0');
                    }
                    break;
                case C_COMMAND:
                    String comp = comp(getCurrentLine());
                    parsedLine += compRef.get(comp);
                    String dest = dest(getCurrentLine());
                    parsedLine += destRef.get(dest);
                    String jmp = jump(getCurrentLine());
                    parsedLine += jumpRef.get(jmp);
                    parsedLine = "111" + parsedLine;
                    break;
                case L_COMMAND:
                    break;
                default:
            }
            if(!StringUtils.isEmpty(parsedLine)){
                parsedLine+="\n";
                outputStream.write(parsedLine.getBytes());
            }
            advance();
        }
    }

    public void scanJumpLabels(){
        this.lineCounter = 0;
        int instructionAddress = 0;
        while(hasMoreCommands()){
            CommandType commandType = commandType(getCurrentLine());
            if(commandType == CommandType.L_COMMAND){
                String symbol = symbol(getCurrentLine());
                if(symbolTable.get(symbol) == null){
                    //since L_COMMAND will be removed from the binary, the next will take the current instruction address
                    symbolTable.put(symbol, String.valueOf(instructionAddress));
                }
            }
            if(commandType != CommandType.L_COMMAND) instructionAddress++;
            advance();
        }
        this.lineCounter = 0;
    }

    public void scanSymbols(){
        this.lineCounter = 0;
        while(hasMoreCommands()){
            CommandType commandType = commandType(getCurrentLine());
            if(commandType == CommandType.A_COMMAND){
                String symbol = symbol(getCurrentLine());
                if(!symbol.matches("[0-9]+")) {
                    if(symbolTable.get(symbol) == null){
                        variables.add(symbol);
                        //add it to symbol table
                        int index = variables.indexOf(symbol);
                        symbolTable.put(symbol, String.valueOf(index + VARIABLE_OFFSET));
                    }
                }
            }
           advance();
        }
        this.lineCounter = 0;
    }
}
