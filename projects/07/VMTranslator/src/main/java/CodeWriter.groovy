import javax.naming.OperationNotSupportedException

class CodeWriter {

    private int vmFileLabelCounter = 0;
    private final Map<String, String> memorySegmentToRegisterMap = ["local" : "LCL", "argument" : "ARG", "this" : "THIS", "that" : "THAT"]
    private final List<String> TEMP_REGISTERS = ["R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12"]
    private FileOutputStream fileOutputStream
    private String vmFileName

    public CodeWriter(String outputFileName){
        fileOutputStream = new FileOutputStream(outputFileName)
        vmFileLabelCounter = 0;
        //writeInit();
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
        writeAssemblyLines(arithmeticAsm)
    }

    void writePushPop(CommandType commandType, String memorySegment, int memoryIndex){
        String pushPopLine;
        if(commandType == CommandType.C_PUSH) pushPopLine = getPushAsm(memorySegment, memoryIndex)
        else if(commandType == CommandType.C_POP) pushPopLine = getPopAsm(memorySegment, memoryIndex)
        else throw new IllegalArgumentException("Push/Pop code translation does not apply to ${commandType.toString()}")
        writeAssemblyLines(pushPopLine)
    }

    void writeLabel(String label){
        String line = "";
        line += "(${label}) \n";
        writeAssemblyLines(line)
    }

    /*
     * Writes assembly code that affects the VM initialization, also called bootstrap code. This must be placed at the beginning of the output file
     */
    void writeInit(){
        String line = "";
        line += "@256 \n"
        line += "D=A \n"
        line += "@SP \n"
        line += "M=D \n"
        writeAssemblyLines(line)
        writeCall("Sys.init", 0);
        line = "(END) \n";
        line += "@END \n"
        line += "0;JMP \n"
        writeAssemblyLines(line)
    }

    /**
     * writes @label \n 0;JMP
     */
    void writeGoto(String label){
        String line = "";
        line += "@${label} \n";
        line += "0;JMP \n";
        writeAssemblyLines(line)
    }

    /**
     * Pops the top most element in the stack (which is assumed a result of some condition)
     * and if result is -1 (all bits 1) it is true, thus jumps to label, otherwise do nothing
     * @param condition
     */
    void writeIf(String label){
        String line = "@SP \n"; //
        line += "AM=M-1 \n" //go to the top most stack element and pop the stack
        line += "D=M \n" //read element
        line += "@${label} \n";
        line += "D;JNE \n"; //if the value is non-zero (zero denotes false), jump
        writeAssemblyLines(line)
    }

    /**
     *    (f)              => Declare a label for the function entry
     *    repeat k times:   => k 1⁄4 number of local variables
     *    PUSH 0            => Initialize all of them to 0
     */
    void writeFunction(String functionName, int numLocals){
        String line = "(${vmFileName}.${functionName}) \n"; //
        for(int i = 0; i < numLocals; i++){
            line += getPushAsm("constant", 0)
        }
        writeAssemblyLines(line)
    }


    /** This method does the ff:
     *
     * push return-address  => (Using the label declared below)
     * push LCL             => Save LCL of the calling function
     * push ARG             => Save ARG of the calling function
     * push THIS            => Save THIS of the calling function
     * push THAT            => Save THAT of the calling function
     * ARG = SP-n-5         => Reposition ARG (n 1⁄4 number of args.)
     * LCL = SP             => Reposition LCL
     * goto f               => Transfer control
     * (return-address)     => Declare a label for the return-address
     */
    void writeCall(String functionName, int numArgs){
        String returnAddress = "${vmFileName}.\$${vmFileLabelCounter++}"
        String line = "";
        //next lines: push return address
        line += pushConstantToStack(returnAddress)
        line += pushMemoryContentToStack("LCL")
        line += pushMemoryContentToStack("ARG")
        line += pushMemoryContentToStack("THIS")
        line += pushMemoryContentToStack("THAT")
        //calculate "ARG" value (ARG=SP-n-5)
        line += "@SP \n"
        line += "D=M \n" //temporarily hold SP value in D
        line += "@5 \n"
        line += "D=D-A \n" //D=@SP-5
        line += "@${numArgs} \n" //load the numOfArgs value
        line += "D=D-A \n" //D=D-n (now D becomes = @SP-5-n)
        line += "@ARG \n"
        line += "M=D \n" //ARG=D
        //set LCL = SP
        line += "@SP \n"
        line += "D=M \n"
        line += "@LCL \n"
        line += "M=D \n" //LCL=D (in general: LCL=@SP)
        //goto function
        line += "@${vmFileName}.${functionName} \n"; //load the called function address
        line += "0;JMP \n";
        //put the return address label
        line += "(${returnAddress}) \n"
        writeAssemblyLines(line)
    }

    /**
     * FRAME = LCL         => FRAME is a temporary variable
     * RET = *(FRAME-5)    => Put the return-address in a temp. var.
     * *ARG = pop()        => Reposition the return value for the caller
     * SP = ARG+1          => Restore SP of the caller
     * THAT = *(FRAME-1)   => Restore THAT of the caller
     * THIS = *(FRAME-2)   => Restore THIS of the caller
     * ARG = *(FRAME-3)    => Restore ARG of the caller
     * LCL = *(FRAME-4)    => Restore LCL of the caller
     * goto RET            => Goto return-address (in the caller’s code)
     */
    void writeReturn(){
        //FRAME = LCL
        String line = "@LCL \n";
        line += "D=M \n" //temporarily keep the @LCL memory value in D
        line += "@FRAME \n" //create a temporary variable name FRAME
        line += "M=D \n" //copy D value to FRAME
        //RET = *(FRAME-5)
        line += "@FRAME \n" //end of caller function frame is just above current function's FRAME (@FRAME -1)
        line += "D=M \n" //
        line += "@5 \n" //
        line += "A=D-A \n" //D=@FRAME-5
        line += "D=M \n"
        line += "@RET \n" //create a temporary variable to save return address
        line += "M=D \n" //(D contains the return address of the caller function)
        // *ARG = pop()
        line += getPopAsm("argument", 0);
        //SP = ARG+1
        line += "@ARG \n"
        line += "D=M+1 \n" //add 1 to the value pointed by ARG => D=@ARG+1
        line += "@SP \n"
        line += "M=D \n" //update @SP to D (@ARG+1)
        //THAT = *(FRAME-1)
        line += "@FRAME \n"
        line += "D=M \n" //
        line += "@1 \n" //
        line += "A=D-A \n" //D=@FRAME-1
        line += "D=M \n"
        line += "@THAT \n"
        line += "M=D \n"
        //THIS = *(FRAME-2)
        line += "@FRAME \n"
        line += "D=M \n" //
        line += "@2 \n" //
        line += "A=D-A \n" //D=@FRAME-2
        line += "D=M \n"
        line += "@THIS \n"
        line += "M=D \n"
        //ARG = *(FRAME-3)
        line += "@FRAME \n"
        line += "D=M \n" //
        line += "@3 \n" //
        line += "A=D-A \n" //D=@FRAME-3
        line += "D=M \n"
        line += "@ARG \n"
        line += "M=D \n"
        //LCL = *(FRAME-4)
        line += "@FRAME \n"
        line += "D=M \n" //
        line += "@4 \n" //
        line += "A=D-A \n" //D=@FRAME-4
        line += "D=M \n"
        line += "@LCL \n"
        line += "M=D \n"
        //goto RET
        line += "@RET \n"
        line += "A=M \n"
        line += "0;JMP \n"
        writeAssemblyLines(line)
    }

    private String pushConstantToStack(String value) {
        String line = "";
        line += "@${value} \n" //set A-register to a value stored in variable 'value'. This in effect selects the memory location located at a value we set to it.
        line += "D=A \n"; //keep it temporarily in D
        line += "@SP \n"; //
        line += "A=M \n"; // go to the top position in stack
        line += "M=D \n"; // put the D-register value in the stack
        line += "@SP \n";
        line += "M=M+1 \n"; // increment SP pointer
        return line
    }

    private String pushMemoryContentToStack(String value) {
        String line = "";
        line += "@${value} \n" //set A-register to a value stored in variable 'value'. This in effect selects the memory location located at a value we set to it.
        line += "D=M \n"; //keep it temporarily in D
        line += "@SP \n"; //
        line += "A=M \n"; // go to the top position in stack
        line += "M=D \n"; // put the D-register value in the stack
        line += "@SP \n";
        line += "M=M+1 \n"; // increment SP pointer
        return line
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
                arithmeticAsm += "@${vmFileName}.logic.cmp.true.${vmFileLabelCounter} \n"
                arithmeticAsm += "D;JEQ \n"
                arithmeticAsm += "D=0 \n"
                arithmeticAsm += "@${vmFileName}.logic.cmp.end.${vmFileLabelCounter} \n"
                arithmeticAsm += "0;JEQ \n"
                arithmeticAsm += "(${vmFileName}.logic.cmp.true.${vmFileLabelCounter}) \n"
                arithmeticAsm += "D=-1 \n"
                arithmeticAsm += "(${vmFileName}.logic.cmp.end.${vmFileLabelCounter}) \n"
                break;
            case "gt": //set D = -1 if gt or 0 if not
                arithmeticAsm += "D=D-M \n"
                arithmeticAsm += "@${vmFileName}.logic.cmp.true.${vmFileLabelCounter} \n"
                arithmeticAsm += "D;JGT \n"
                arithmeticAsm += "D=0 \n"
                arithmeticAsm += "@${vmFileName}.logic.cmp.end.${vmFileLabelCounter} \n"
                arithmeticAsm += "0;JEQ \n"
                arithmeticAsm += "(${vmFileName}.logic.cmp.true.${vmFileLabelCounter}) \n"
                arithmeticAsm += "D=-1 \n"
                arithmeticAsm += "(${vmFileName}.logic.cmp.end.${vmFileLabelCounter}) \n"
                break;
            case "lt": //set D = -1 if lt or 0 if not
                arithmeticAsm += "D=D-M \n"
                arithmeticAsm += "@${vmFileName}.logic.cmp.true.${vmFileLabelCounter} \n"
                arithmeticAsm += "D;JLT \n"
                arithmeticAsm += "D=0 \n"
                arithmeticAsm += "@v${vmFileName}.m.logic.cmp.end.${vmFileLabelCounter} \n"
                arithmeticAsm += "0;JEQ \n"
                arithmeticAsm += "(${vmFileName}.logic.cmp.true.${vmFileLabelCounter}) \n"
                arithmeticAsm += "D=-1 \n"
                arithmeticAsm += "(${vmFileName}.logic.cmp.end.${vmFileLabelCounter}) \n"
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
        vmFileLabelCounter++;
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
        //    line += "A=-1 \n"
        //    line += "D=D-A \n"
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

    public void writeAssemblyLines(String line){
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
        this.vmFileLabelCounter = 0;
    }
}
