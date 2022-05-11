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
            if(line.isEmpty())advance();//TODO: FIND BETTER IMPLEMENTATION
        } while (line.charAt(0) == '/' && hasMoreCommands());
        line = line.split("//")[0];
    }

    public COMMANDTYPE commandType(){
        args = line.split(" ");
        switch (args[0]) {
            case "push":
                return COMMANDTYPE.C_PUSH;
            case "pop":
                return COMMANDTYPE.C_POP;
            case "function":
                return COMMANDTYPE.C_FUNCTION;
            case "call":
                return COMMANDTYPE.C_CALL;
            case "return":
                return COMMANDTYPE.C_RETURN;
            case "if":
                return COMMANDTYPE.C_IF;
            case "goto":
                return COMMANDTYPE.C_GOTO;
            case "label":
                return COMMANDTYPE.C_LABEL;
            default:
                return COMMANDTYPE.C_ARITHMETIC;
        }
    }

    public String[] getArgs(){
        return args;
    }

    public String getLine() {
        return line;
    }
}