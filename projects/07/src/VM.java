import java.io.File;

public class VM {
    private static int filesTranslated = 0;
    public static void main(String[] args){
        String[] files = new String[]{"StackArithmetic/", "MemoryAccess/"};//DEBUG INPUT for manual file/folder insertion empty to use args input for translation
        runScript(files);
        System.out.println("Translated " + filesTranslated + " files in total");
    }

    /*
    Method runScript() handles the files that have to be translated
    It supports translating single files and whole folder structures (nested folders included)
    Gives status updates in console
     */
    public static void runScript (String[] args){
        for (String fileLocation : args) {
            if (fileLocation.endsWith(".vm")) {
                System.out.println("Translated .asm file at " + fileLocation);
                filesTranslated++;
                translate(fileLocation);
            }
            else if (fileLocation.endsWith("/")) {
                String[] filesInDir = new File(fileLocation).list();
                if (filesInDir == null) continue;
                for (int i = 0; i < filesInDir.length; i++){
                    filesInDir[i] = fileLocation + filesInDir[i];
                    if (!filesInDir[i].contains(".")) filesInDir[i] += "/";
                }
                runScript(filesInDir);
            }else System.out.println("Did not translate " + fileLocation + " as it is not a folder or a .vm file");
        }
    }

    //@param inputFilePath can be relative or absolute filepath
    public static void translate(String inputFilePath) {
        Parser parser = new Parser(new File(inputFilePath));
        CodeWriter codeWriter = new CodeWriter(new File(inputFilePath.substring(0, inputFilePath.length() - 3) + ".asm"));//Generates output-file ending in .asm instead of .vm
        while (parser.hasMoreCommands()) {
            parser.advance();
            COMMANDTYPE type = parser.commandType();
            String[] args = parser.getArgs();
            codeWriter.writeToFile(("\n//" + parser.getLine()));
            if (type == COMMANDTYPE.C_PUSH || type == COMMANDTYPE.C_POP){

                codeWriter.writePushPop(type, args[1], Integer.parseInt(args[2]));
            }
            else if (type == COMMANDTYPE.C_FUNCTION || type == COMMANDTYPE.C_CALL) codeWriter.writeArithmetic(args[0] + args[1] + args[2]);//Copy from book, no function implemented yet
            else if(type == COMMANDTYPE.C_ARITHMETIC) codeWriter.writeArithmetic(args[0]);
        }
        codeWriter.close();
    }
}