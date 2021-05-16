import groovy.io.FileType

class VMTranslator {


    static void main(def args){
        List<String> vmSourceFiles = []
        File sourceFile = new File( args[1])

        if(sourceFile.isDirectory()){
            sourceFile.eachFileRecurse (FileType.FILES) { file ->
                vmSourceFiles << file.absolutePath
            }
        } else {
            vmSourceFiles << args[0]
        }

        CodeWriter codeWriter = null
        try{

            String destinationDirectory = args[1].substring(0, args[1].lastIndexOf("/") + 1) //include the last slash
            for(String fileName : vmSourceFiles){
                String destinationFileName = fileName.replaceAll(destinationDirectory, "") //only leave the file name with extension
                        .replaceAll("[.].*", "")
                if(!fileName.endsWith(".vm")) continue //only accept .VM files
                Parser parser = new Parser(fileName)
                String destinationPath = destinationDirectory + destinationFileName + ".asm"
                codeWriter = new CodeWriter(destinationPath)

                String line = ""
                codeWriter.writeLineToFile(line)

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

    private static String getDesinationFile(String[] args) {
        String destinationDirectory = args[1].substring(0, args[1].lastIndexOf("/") + 1) //include the last slash
        String destinationFileName = args[1].toString().replaceAll(destinationDirectory, "") //only leave the file name with extension
                .replaceAll("[.].*", "")
        String destinationPath = destinationDirectory + destinationFileName + ".asm"
        return destinationPath
    }
}
