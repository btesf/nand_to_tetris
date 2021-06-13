package com.bereket.compiler

class VMWriter {

    private FileOutputStream fileOutputStream
    private StringBuilder stringBuilder = new StringBuilder();

    public VMWriter(String outputFileName){
        if(outputFileName) fileOutputStream = new FileOutputStream(outputFileName)
    }

    public void writePush(MemorySegment segment, int index){
        stringBuilder.append("push ${segment.name} ${index} \n")
    }

    public void writePop(MemorySegment segment, int index){
        stringBuilder.append("pop ${segment.name} ${index} \n")
    }

    public void writeArithmetic(Command command){
        String cmd = command.toString().toLowerCase()
        stringBuilder.append("${cmd} \n")
    }

    public void writeLabel(String label){
        stringBuilder.append("label ${label} \n")
    }

    public void writeGoto(String label){
        stringBuilder.append("goto ${label} \n")
    }

    public void writeIf(String label){
        stringBuilder.append("if-goto ${label} \n")
    }

    public void writeCall(String subroutineName, int nArgs){
        stringBuilder.append("call ${subroutineName} ${nArgs} \n")
    }

    public void writeFunction(String subroutineName, int nLocals){
        stringBuilder.append("function ${subroutineName} ${nLocals} \n")
    }

    public void writeReturn(){
        stringBuilder.append("return \n")
    }

    public void close(){
        if(fileOutputStream != null) fileOutputStream.close()
    }

    void writeFile(){
        if(fileOutputStream != null) {
            fileOutputStream.write(stringBuilder.toString().getBytes());
            close()
        }
    }
}
