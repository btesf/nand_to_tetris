package com.bereket.compiler

class VMWriter {

    private FileOutputStream fileOutputStream
    private StringBuilder stringBuilder = new StringBuilder();

    public VMWriter(String outputFileName){
        fileOutputStream = new FileOutputStream(outputFileName)
    }

    public void writePush(MemorySegment segment, int index){

    }

    public void writePop(MemorySegment segment, int index){

    }

    public void writeArithmetic(Command command){}

    public void writeLabel(String label){}

    public void writeGoto(String label){}

    public void writeIf(String label){}

    public void writeCall(String name, int nArgs){}

    public void writeFunction(String name, int nLocals){}

    public void writeReturn(){}

    public void close(){
        if(fileOutputStream != null) fileOutputStream.close()
    }

    void writeFile(){
        fileOutputStream.write(stringBuilder.toString().getBytes());
        close()
    }
}
