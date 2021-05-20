import javax.naming.OperationNotSupportedException

class CodeWriter {

    private static ins_counter = 0;
    private final Map<String, String> memorySegmentToRegisterMap = ["local" : "LCL", "argument" : "ARG", "this" : "THIS", "that" : "THAT"]
    private final List<String> TEMP_REGISTERS = ["R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12"]
    private FileOutputStream fileOutputStream
    private String vmFileName

    public CodeWriter(String outputFileName){
        fileOutputStream = new FileOutputStream(outputFileName)
    }

    /**
     * @SP is a pointer to the stack "segment" and it points to one level above the top most stack element address.
     * e.g. if the top element is found at addr M[256], @SP points to M[257]
     *
     * To write to the top most element in the stack, we subtract one from the @SP memory value to find the address of the "slot"
     * @param operation
     */
    void writeArithmetic(String operation){
        String arithmeticAsm;
        if(operation == "neg"){
            arithmeticAsm = writeNegArithmetic()
        } else if(operation == "not"){
            arithmeticAsm = writeNotArithmetic();
        } else {
            arithmeticAsm = writeTwoOperandArithmetic(operation)
        }
        writeLineToFile(arithmeticAsm)
    }

    void writePushPop(CommandType commandType, String memorySegment, int memoryIndex){
        String pushPopLine;
        if(commandType == CommandType.C_PUSH) pushPopLine = getPushAsm(memorySegment, memoryIndex)
        else if(commandType == CommandType.C_POP) pushPopLine = getPopAsm(memorySegment, memoryIndex)
        else throw new IllegalArgumentException("Push/Pop code translation does not apply to ${commandType.toString()}")
        writeLineToFile(pushPopLine)
    }

    void writeLabel(String label){

    }

    void writeGoto(String label){

    }

    void writeIf(String condition){

    }

    void writeReturn(){

    }

    void writeFunction(String functionName, int numLocals){

    }

    void writeCall(String functionName, int numArgs){

    }

    private String getPushAsm(String memorySegment, int memoryIndex){
        String line = "";
        if(memorySegment.toLowerCase() == "constant"){
            line += "@${memoryIndex} \n"
            line += "D=A \n" //store the constant value to D register
        } else {
            line += getWorkingAddressForMemorySegment(memorySegment, memoryIndex)
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

    private String getWorkingAddressForMemorySegment(String memorySegment, int memoryIndex){
        String line = ""
        if(memorySegment == "temp"){
            line += ("@" + TEMP_REGISTERS.get(memoryIndex) + " \n")
        } else if(memorySegment == "static"){ //static variables (symbols) are labelled as <vm_filename>.<index>
            line += "${vmFileName}.${memoryIndex}"
        } else if(memorySegment == "pointer"){
            if(memoryIndex != 0 && memoryIndex != 1) throw new RuntimeException("Memory index for pointer can only be 0 or 1")
            line += (memoryIndex == 0 ? "@THIS" : "@THAT")
            line += " \n"
        } else { //will be calculated from an offset found in the segment pointer registers
            String register = getRegisterForSegment(memorySegment)
            line += "@${register} \n"
            line += "D=M \n"
            line += "@${memoryIndex} \n"
            line += "A=D+A \n"
        }
        return line;
    }

    private String getPopAsm(String memorySegment, int memoryIndex){
        //decrement SP by 1
        String line = "@SP \n"
        line += "M=M-1 \n"
        line += getWorkingAddressForMemorySegment(memorySegment, memoryIndex)
        line += "D=A \n"  //put the destination address in temporary register;
        line += "@13 \n"  //@13 is a general purpose register. (other general purpose registers are @14 and @15);
        line += "M=D \n" //save the destination address (saved in D-register) in to the temporary general purpose register @13 because register D is going to be reused.
        //read current value at @SP
        line += "@SP \n"
        line += "A=M \n" //go to the stack memory stack top most element
        line += "D=M \n" //save the value into D register
        line += "@13 \n" //destination address is temporarily saved here
        line += "A=M \n" //load destination address to reg-A to select the memory
        line += "M=D \n" //save the value of the top most element in the address saved at @13
        return line;
    }

    private String getAsmForArithmeticOperation(String operation) {
        String arithmeticAsm = "";
        switch (operation) {
            case "add":
                arithmeticAsm += "D=D+M \n"
                break;
            case "sub":
                arithmeticAsm += "D=D-M \n"
                break;
            case "eq": //set D = -1 if eq or 0 if not
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
            case "gt": //set D = -1 if gt or 0 if not
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
            case "lt": //set D = -1 if lt or 0 if not
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

    private String writeTwoOperandArithmetic(String operation) {
        String line = "@SP \n" //select stack pointer (equivalent to R0 or @0)
        line += "A=M-1 \n" //go to the stack segment pointed by @SP and find top most stack element (the stack pointer is always one above the top most stack element)
        line += "A=A-1 \n"  //go one level down again to read the second element (operand) in the stack
        line += "D=M \n" //get the second top element (operand x) in the stack and keep the value in register D
        line += "A=A+1 \n"  //get the address of the top most element (operand y) in the stack
        //D contains "x" (operand 1), M[A] contains 'y' (operand 2)
        line += getAsmForArithmeticOperation(operation)
        line += "@SP \n"
        line += "A=M-1 \n" //go to the second operand (y) address
        line += "A=A-1 \n" //go to the first operand (x) address
        line += "M=D \n" //replace first operand with a value from D
        line += "@SP \n"
        line += "D=M-1 \n" //calculate the new address of the stack pointer; which is one value down from previous; i.e. it points above the x operand (y is discarded)
        line += "M=D \n" //write the calculated stack pointer address to SP
        line
    }

    private String writeNegArithmetic() {
        String line = "";
        line += "@SP \n" //select stack pointer (equivalent to R0 or @0)
        line += "A=M-1 \n" //go to the stack segment pointed by @SP and find top most stack element (the stack pointer is always one above the top most stack element)
        line += "D=M \n"
        line += "D=!D \n"
        line += "@SP \n"
        line += "A=M-1 \n"
        line += "M=D \n"
        return line
    }

    private String writeNotArithmetic() {
        String line = "";
        line += "@SP \n" //select stack pointer (equivalent to R0 or @0)
        line += "A=M-1 \n"
        line += "D=M \n" //keep the value in register D
        line += "D=!D \n"
        line += "A=-1 \n"
        line += "D=D-A \n"
        //replace the top most stack element
        line += "@SP \n"
        line += "A=M-1 \n"
        line += "M=D \n"
        return line
    }

    /*   Method operates on operand "x" (which is the top most element in the stack) which is already stored in register 'D' before this method is invoked.
     *   Method always save the final result/outcome on Register D
     */
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

    void setFileName(String vmFileName) {
        //only keep the file name without an extension from the whole URI (e.g. /abc/def/xyz.123 => xyz
        File file = new File(vmFileName);
        this.vmFileName = file.getName().replaceAll("\\..*", "");
    }
}
