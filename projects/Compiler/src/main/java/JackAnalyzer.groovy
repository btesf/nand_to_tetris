import groovy.io.FileType

class JackAnalyzer {

    public static void main(def args){
        final String JACK_FILE_EXTENSIONS = ".jack"
        List<String> jackSourceFiles = []
        File sourceFile = new File( args[0])
        //if a the source file is a directory, read all the files
        if(sourceFile.isDirectory()){
            sourceFile.eachFileRecurse (FileType.FILES) { file ->
                if(file.absolutePath.endsWith(JACK_FILE_EXTENSIONS)) jackSourceFiles << file.absolutePath
            }
        } else {
            String filename = args[0]
            if(filename.endsWith(JACK_FILE_EXTENSIONS)) jackSourceFiles << filename
        }

        for(String fileName : jackSourceFiles){
            String jackTokenizerDestination = getDestinationFile(new File(fileName), "T");
            JackTokenizer tokenizer = new JackTokenizer(fileName, jackTokenizerDestination)
            tokenizer.tokenize()
            String codeAnalyzerDestination = getDestinationFile(sourceFile);
            CompilationEngine compilationEngine = new CompilationEngine(jackTokenizerDestination, codeAnalyzerDestination)

            if(compilationEngine != null) compilationEngine.close()
        }

        println "Translation completed";
    }

    private static String getDestinationFile(File file, String suffix = null) {
        String fileName = file.absolutePath
        String destinationDirectory =  file.isDirectory() ? fileName : (fileName.substring(0, fileName.lastIndexOf("/") + 1)) //include the last slash
        String destinationFileName = fileName.substring(fileName.lastIndexOf("/") + 1) //only leave the last token after /
                .replaceAll("[.].*", "")
        String destinationPath = destinationDirectory + "/" + destinationFileName + (suffix?:"") + ".xml"
        return destinationPath
    }
}
