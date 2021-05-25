import groovy.io.FileType

class VMTranslator {

    public static void main(def args){
        List<String> vmSourceFiles = []
        File sourceFile = new File( args[0])
        //if a the source file is a directory, read all the files
        if(sourceFile.isDirectory()){
            sourceFile.eachFileRecurse (FileType.FILES) { file ->
                if(file.absolutePath.endsWith(".vm")) vmSourceFiles << file.absolutePath
            }

        } else {
            String filename = args[0]
            if(filename.endsWith(".vm")) vmSourceFiles << filename
        }

        String destinationPath = getDestinationAsmFile(sourceFile);
        CodeWriter codeWriter = new CodeWriter(destinationPath)

        try{
            for(String fileName : vmSourceFiles){
                Parser parser = new Parser(fileName)
                codeWriter.setFileName(fileName)//this will make sure the symbols and labels will be prefixed with the vm filename
                while(parser.hasMoreCommands()){
                    CommandType commandType = parser.commandType();
                    switch(commandType){
                        case CommandType.C_ARITHMETIC:
                            codeWriter.writeArithmetic(parser.arg1())
                            break;
                        case CommandType.C_PUSH:
                        case CommandType.C_POP:
                            codeWriter.writePushPop(commandType, parser.arg1(), parser.arg2())
                            break;
                        case CommandType.C_LABEL:
                            codeWriter.writeLabel(parser.arg1());
                            break;
                        case CommandType.C_GOTO:
                            codeWriter.writeGoto(parser.arg1());
                            break;
                        case CommandType.C_IF:
                            codeWriter.writeIf(parser.arg1());
                            break;
                        case CommandType.C_FUNCTION:
                            codeWriter.writeFunction(parser.arg1(), parser.arg2());
                            break;
                        case CommandType.C_RETURN:
                            codeWriter.writeReturn();
                            break;
                        case CommandType.C_CALL:
                            codeWriter.writeCall(parser.arg1(), parser.arg2());
                            break;
                    }
                    parser.advance();
                }
            }
        } finally {
            if(codeWriter != null) codeWriter.close()
        }

        println "Translation completed";
    }

    private static String getDestinationAsmFile(File file) {
        String fileName = file.absolutePath
        String destinationDirectory =  file.isDirectory() ? fileName : (fileName.substring(0, fileName.lastIndexOf("/") + 1)) //include the last slash
        String destinationFileName = fileName.substring(fileName.lastIndexOf("/") + 1) //only leave the last token after /
                .replaceAll("[.].*", "")
        String destinationPath = destinationDirectory + "/" + destinationFileName + ".asm"
        return destinationPath
    }
}
