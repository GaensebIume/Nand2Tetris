import java.io.*;
import java.util.Scanner;

public class Parser {
    private String line;
    private Scanner asmScanner;
    private Scanner lScanner;

    public Parser(File asmFile) {
        try {
            this.asmScanner = new Scanner(asmFile);
            this.lScanner = new Scanner(asmFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean hasMoreLines(Scanner scanner) {
        return scanner.hasNextLine();
    }

    public void advance(Scanner scanner) {
        do {
            line = scanner.nextLine();
            line = line.replaceAll(" ", "");
            if (line.isEmpty())line = scanner.nextLine();
        } while (line.charAt(0) == '/' && hasMoreLines(scanner));
        line = line.split("//")[0];
    }

    public INSTRUCTION instructionType() {
        if (line.contains("@")) return INSTRUCTION.A_INSTRUCTION;
        else if (line.contains("=")) return INSTRUCTION.C_INSTRUCTION;
        else if (line.contains("(")) return INSTRUCTION.L_INSTRUCTION;
        else if (line.contains(";")) return INSTRUCTION.C_INSTRUCTIONS;
        else throw new IllegalArgumentException("Illegal line, no INSTRUCTION found");
    }

    public String symbol(INSTRUCTION type) {
        if (type == INSTRUCTION.A_INSTRUCTION) return line.substring(1);
        else if (type == INSTRUCTION.L_INSTRUCTION) return line.substring(1, line.length() - 1);
        else throw new IllegalArgumentException("Illegal line");
    }

    public String dest() {
        if (line.contains("=")) return line.split("=")[0];
        else return line.split(";")[0];
    }

    public String comp() {
        if (line.contains("=")) {
            String[] subString = line.split("=");
            if (line.contains(";")) return subString[1].split(";")[0];
            else return subString[1];
        } else if (line.contains(";")) return line.split(";")[0];
            else return "null";
    }

    public String jump() {
        if (line.contains(";")) return line.split(";")[1];
        else return  "";
    }

    //Getter & Setter
    public Scanner getLScanner(){
        return lScanner;
    }

    public Scanner getAsmScanner() {
        return asmScanner;
    }
}