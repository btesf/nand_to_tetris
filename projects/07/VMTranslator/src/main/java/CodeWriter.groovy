import javax.naming.OperationNotSupportedException

class CodeWriter {

    private static ins_counter = 0;
    private final Map<String, String> memorySegmentToRegisterMap = ["local" : "LCL", "argument" : "ARG", "this" : "THIS", "that" : "THAT", "pointer" : "pointer" /* will be resolved by getWorkingAddressForMemorySegment*/,
                                                                    "static" : "16", "temp" : "temp" /* will be resolved by getWorkingAddressForMemorySegment*/]
    private final List<String> TEMP_REGISTERS = ["R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12"]
    private FileOutputStream fileOutputStream

    public CodeWriter(String outputFileName){
        fileOutputStream = new FileOutputStream(outputFileName)
    }

    void setOutputFile(String outputFileName) {
        this.close()
        fileOutputStream = new FileOutputStream(outputFileName)
    }

    void writeArithmetic(String operation){
        String arithmeticAsm = "";
        arithmeticAsm += "@SP \n"
        arithmeticAsm += "A=M-1 \n" //go one level below to the current stack pointer (which is always one above the top most stack element) to pick the top most element

        if(operation == "neg"){
            arithmeticAsm += "D=M \n" //keep the value in register D
            arithmeticAsm += "D=!D \n"

            arithmeticAsm += "@SP \n"
            //replace the top most stack element
            arithmeticAsm += "A=M-1 \n" //remove the first operand (y)

            arithmeticAsm += "M=D \n" //replace second operand with a value from D
            //@SP don't need to be updated

        } else if(operation == "not"){

            arithmeticAsm += "D=M \n" //keep the value in register D
            arithmeticAsm += "D=!D \n"
            arithmeticAsm += "A=-1 \n"
            arithmeticAsm += "D=D-A \n"

            arithmeticAsm += "@SP \n"
            //replace the top most stack element
            arithmeticAsm += "A=M-1 \n" //remove the first operand (y)

            arithmeticAsm += "M=D \n" //replace second operand with a value from D
            //@SP don't need to be updated

        } else {
            arithmeticAsm += "A=A-1 \n" //go one level below to the current stack pointer (which is always one above the top most stack element) to pick the top most element
            arithmeticAsm += "D=M \n" //keep the value in register D
            arithmeticAsm += "A=A+1 \n" //go one level down again to read the second element in the stack
            //D contains "x" (operand 1), M[A] contains 'y' (operand 2)
            arithmeticAsm += getAsmForArithmeticOperation(operation)


            arithmeticAsm += "@SP \n"
            arithmeticAsm += "A=M-1 \n" //remove the first operand (y)
            arithmeticAsm += "A=A-1 \n" //remove the second operand (x)

            arithmeticAsm += "M=D \n" //replace second operand with a value from D
            //decrement the stack pointer value by one
            arithmeticAsm += "@SP \n"
            arithmeticAsm += "D=M-1 \n"
            arithmeticAsm += "M=D \n"

        }
        //getAsmForArithmeticOperation always saves the arithmetic result in Register D; Thus save the value back on the top of the stack
        //where the saving stack index is already pointed by register "A" above
        writeLineToFile(arithmeticAsm)
    }

    void writePushPop(CommandType commandType, String memorySegment, int memoryIndex){
        String pushPopLine;
        if(commandType == CommandType.C_PUSH) pushPopLine = getPushAsm(memorySegment, memoryIndex)
        else if(commandType == CommandType.C_POP) pushPopLine = getPopAsm(memorySegment, memoryIndex)
        else throw new IllegalArgumentException("Push/Pop code translation does not apply to ${commandType.toString()}")
        writeLineToFile(pushPopLine)
    }

    private String getPushAsm(String memorySegment, int memoryIndex){
        String line = "";
        if(memorySegment.toLowerCase() == "constant"){
            line += "@${memoryIndex} \n"
            line += "D=A \n" //store the constant value to D register
        } else {
            String register = getRegisterForSegment(memorySegment)
            line += getWorkingAddressForMemorySegment(register, memoryIndex)
            line += "D=M \n" //store the value of the selected address to D register
        }
        line += "@SP \n" //select the stack pointer address
        line += "A=M \n" //read the value stored in the SP register (the stored value is the top index of the stack memory segment)
        line += "M=D \n" //put the D register value to the top of the stack
        //increment SP by 1
        line += "@SP \n"
        line += "M=M+1 \n"

        return line;
    }

    private String getWorkingAddressForMemorySegment(String register, int memoryIndex){
        String line = ""
        if(register == "temp"){
            line += ("@" + TEMP_REGISTERS.get(memoryIndex) + " \n")
        }
        else if(register == "pointer"){
            if(memoryIndex != 0 && memoryIndex != 1) throw new RuntimeException("Memory index for pointer can only be 0 or 1")
            line += (memoryIndex == 0 ? "@THIS" : "@THAT")
            line += " \n"
        }else {
            line += "@${register} \n"
            line += "D=M \n"
            line += "@${memoryIndex} \n"
            line += "A=D+A \n"
        }
        return line;
    }

    private String getPopAsm(String memorySegment, int memoryIndex){
        String line = "\n";
        //decrement SP by 1
        line += "@SP \n"
        line += "M=M-1 \n"

        String register = getRegisterForSegment(memorySegment)
        line += getWorkingAddressForMemorySegment(register, memoryIndex)
        //put the destination address in temporary register
        line += "D=A \n"
        line += "@13 \n"
        line += "M=D \n"

        //read current value at @SP
        line += "@SP \n"
        line += "A=M \n"
        line += "D=M \n"
        line += "@13 \n" //destination address is temporarily saved here
        line += "A=M \n"
        line += "M=D \n"

        line += "\n"
        return line;
    }

    /*   Method operates on operand "x" (which is the top most element in the stack) which is already stored in register 'D' before this method is invoked.
     *   Method always save the final result/outcome on Register D
     */
    private String getAsmForArithmeticOperation(String operation) {
        String arithmeticAsm = "";
        switch (operation) {
            case "add":
                arithmeticAsm += "D=D+M \n"
                break;
            case "sub":
                arithmeticAsm += "D=D-M \n"
                break;
            case "eq":
                arithmeticAsm += "D=D-M \n"
                arithmeticAsm += "@vm.logic.cmp.true.${ins_counter} \n"
                arithmeticAsm += "D;JEQ \n"
                arithmeticAsm += "D=0 \n"
                arithmeticAsm += "@vm.logic.cmp.end.${ins_counter} \n"
                arithmeticAsm += "0;JEQ \n"
                arithmeticAsm += "(vm.logic.cmp.true.${ins_counter}) \n"
                arithmeticAsm += "D=-1 \n"
                arithmeticAsm += "(vm.logic.cmp.end.${ins_counter}) \n"
                break;
            case "gt":
                arithmeticAsm += "D=D-M \n"
                arithmeticAsm += "@vm.logic.cmp.true.${ins_counter} \n"
                arithmeticAsm += "D;JGT \n"
                arithmeticAsm += "D=0 \n"
                arithmeticAsm += "@vm.logic.cmp.end.${ins_counter} \n"
                arithmeticAsm += "0;JEQ \n"
                arithmeticAsm += "(vm.logic.cmp.true.${ins_counter}) \n"
                arithmeticAsm += "D=-1 \n"
                arithmeticAsm += "(vm.logic.cmp.end.${ins_counter}) \n"
                break;
            case "lt":
                arithmeticAsm += "D=D-M \n"
                arithmeticAsm += "@vm.logic.cmp.true.${ins_counter} \n"
                arithmeticAsm += "D;JLT \n"
                arithmeticAsm += "D=0 \n"
                arithmeticAsm += "@vm.logic.cmp.end.${ins_counter} \n"
                arithmeticAsm += "0;JEQ \n"
                arithmeticAsm += "(vm.logic.cmp.true.${ins_counter}) \n"
                arithmeticAsm += "D=-1 \n"
                arithmeticAsm += "(vm.logic.cmp.end.${ins_counter}) \n"
                break;
            case "and":
                arithmeticAsm += "D=D&M \n"
                break;
            case "or":
                arithmeticAsm += "D=D|M \n"
                break;
            default:
                throw new OperationNotSupportedException("Arithmetic operatiorn '${operation}' is not supported. ")
        }
        ins_counter++;
        return arithmeticAsm
    }

    private String getRegisterForSegment(String memorySegment) {
        String register = memorySegmentToRegisterMap.get(memorySegment)
        if (register == null) throw new RuntimeException("cannot find pointer register for ${memorySegment}")
        return register
    }

    public void writeLineToFile(String line){
        if(line){
            fileOutputStream.write(line.getBytes());
        }
    }

    void close(){
        fileOutputStream.close()
    }
}
