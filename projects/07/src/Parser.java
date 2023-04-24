import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
    private Scanner scanner;
    private String line;
    private String[] args;

    public Parser(File inputFile) {
        try {
            this.scanner = new Scanner(inputFile);
            line = scanner.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMoreCommands(){
        return scanner.hasNext();
    }

    public void advance(){
        do {
            line = scanner.nextLine();
            if(line.isEmpty())advance();
        } while (line.charAt(0) == '/' && hasMoreCommands());
        line = line.split("//")[0];
    }

    public COMMANDTYPE commandType(){
        args = line.split(" ");
        return switch (args[0]) {
            case "push" -> COMMANDTYPE.C_PUSH;
            case "pop" -> COMMANDTYPE.C_POP;
            case "function" -> COMMANDTYPE.C_FUNCTION;
            case "call" -> COMMANDTYPE.C_CALL;
            case "return" -> COMMANDTYPE.C_RETURN;
            case "if" -> COMMANDTYPE.C_IF;
            case "goto" -> COMMANDTYPE.C_GOTO;
            case "label" -> COMMANDTYPE.C_LABEL;
            default -> COMMANDTYPE.C_ARITHMETIC;
        };
    }

    public String[] getArgs(){
        return args;
    }

    public String getLine() {
        return line;
    }
}