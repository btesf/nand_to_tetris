import groovy.io.FileType

class VMTranslator {

    public static void main(def args){
        List<String> vmSourceFiles = []
        File sourceFile = new File( args[1])
        //if a the source file is a directory, read all the files
        if(sourceFile.isDirectory()){
            sourceFile.eachFileRecurse (FileType.FILES) { file ->
                if(file.absolutePath.endsWith(".vm")) vmSourceFiles << file.absolutePath
            }
        } else {
            String filename = args[1]
            if(filename.endsWith(".vm")) vmSourceFiles << filename
        }

        CodeWriter codeWriter = null

        try{
            for(String fileName : vmSourceFiles){
                Parser parser = new Parser(fileName)
                String destinationPath = getDestinationAsmFile(fileName);
                codeWriter = new CodeWriter(destinationPath)
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
                        case CommandType.C_GOTO:
                        case CommandType.C_IF:
                        case CommandType.C_FUNCTION:
                        case CommandType.C_RETURN:
                        case CommandType.C_CALL:
                            break;
                    }
                    parser.advance();
                }
                if(codeWriter != null) codeWriter.close();
            }
        } finally {
            if(codeWriter != null) codeWriter.close()
        }

        println "Translation completed";
    }

    private static String getDestinationAsmFile(String fileName) {
        String destinationDirectory = fileName.substring(0, fileName.lastIndexOf("/") + 1) //include the last slash
        String destinationFileName =fileName.toString().replaceAll(destinationDirectory, "") //only leave the file name with extension
                .replaceAll("[.].*", "")
        String destinationPath = destinationDirectory + destinationFileName + ".asm"
        return destinationPath
    }
}
