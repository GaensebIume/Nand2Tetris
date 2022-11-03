import java.io.File;

public class VM {
    private static int filesTranslated = 0;
    public static void main(String[] args){
        String[] files = new String[]{"ProgramFlow/", "FunctionCalls/"};//DEBUG INPUT for manual file/folder insertion empty to use args input for translation
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
            }
        }
    }

    //@param inputFilePath can be relative or absolute filepath
    public static void translate(String inputFilePath) {
        Parser parser = new Parser(new File(inputFilePath));
        CodeWriter codeWriter = new CodeWriter(new File(inputFilePath.substring(0, inputFilePath.length() - 3) + ".asm"));//Generates output-file ending in .asm instead of .vm
        while (parser.hasMoreCommands()) {
            parser.advance();
            COMMANDTYPE type = parser.commandType();
            System.out.println(type);
            String[] args = parser.getArgs();
            String line = parser.getLine();
            System.out.println(line);
            codeWriter.writeToFile(("\n//" + line));
            if (args.length > 2) args[2] = args[2].toLowerCase().strip().replaceAll(" ", "");
            if (type == COMMANDTYPE.C_PUSH || type == COMMANDTYPE.C_POP){
                codeWriter.writePushPop(type, args[1], Integer.parseInt(args[2]));
            } else if (type == COMMANDTYPE.C_FUNCTION) {
                codeWriter.writeFunction(args[1], Integer.parseInt(args[2]));
            } else if (type == COMMANDTYPE.C_RETURN) {
                codeWriter.writeReturn();
            } else if (type == COMMANDTYPE.C_CALL) {
                codeWriter.writeCall(args[1], Integer.parseInt(args[2]));
            } else if (type == COMMANDTYPE.C_IF) {
                codeWriter.writeIf(args[1]);
            } else if (type == COMMANDTYPE.C_ARITHMETIC) {
                codeWriter.writeArithmetic(args[0]);
            } else if (type == COMMANDTYPE.C_LABEL) {
                codeWriter.writeLabel(args[0]);
            } else if (type == COMMANDTYPE.C_GOTO) {
                codeWriter.writeGoTo(args[0]);
            }
        }
        codeWriter.close();
    }
}