package com.bereket.compiler

class VMWriter {

    private FileOutputStream fileOutputStream
    private List<String> vmLines = new ArrayList<>()

    public VMWriter(String outputFileName){
        if(outputFileName) fileOutputStream = new FileOutputStream(outputFileName)
    }

    public void writePush(MemorySegment segment, int index){
        vmLines.add("push ${segment.name} ${index}")
    }

    public void writePop(MemorySegment segment, int index){
        vmLines.add("pop ${segment.name} ${index}")
    }

    public void writeArithmetic(Command command){
        String cmd = command.toString().toLowerCase()
        vmLines.add("${cmd}")
    }

    public void writeLabel(String label){
        vmLines.add("label ${label}")
    }

    public void writeGoto(String label){
        vmLines.add("goto ${label}")
    }

    public void writeIf(String label){
        vmLines.add("if-goto ${label}")
    }

    public void writeCall(String subroutineName, int nArgs){
        vmLines.add("call ${subroutineName} ${nArgs}")
    }

    public void writeFunction(String subroutineName, int nLocals){
        vmLines.add("function ${subroutineName} ${nLocals}")
    }

    public void writeReturn(){
        vmLines.add("return")
    }

    public void close(){
        if(fileOutputStream != null) fileOutputStream.close()
    }

    void writeFile(){
        if(fileOutputStream != null) {
            fileOutputStream.write(vmLines.join("\n").getBytes());
            close()
        }
    }

    void resetVmLines(){
        vmLines = new ArrayList<>();
    }

    public void replaceString(String find, String replace){

        int index = vmLines.indexOf(find)
        if(index > -1){
            vmLines.set(index, replace)
        }
    }
}
