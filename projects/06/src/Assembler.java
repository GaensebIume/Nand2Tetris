import java.io.*;

public class Assembler {
    public static void convertToBin(File file){
        CodeModule codeModule = new CodeModule();
        Parser parser = new Parser(file);
        int lineNumber = 0;
        while (parser.hasMoreLines(parser.getLScanner())) {//1st loop: only L_INSTRUCTIONS are used
            parser.advance(parser.getLScanner());
            INSTRUCTION type = parser.instructionType();
            if (type == INSTRUCTION.L_INSTRUCTION) codeModule.addCustomA(parser.symbol(type), codeModule.formatToBin(lineNumber));
            else lineNumber++;
        }
        parser.getLScanner().close();
        StringBuilder out = new StringBuilder();
        while (parser.hasMoreLines(parser.getAsmScanner())) {
            String addToFinal = "";
            parser.advance(parser.getAsmScanner());
            INSTRUCTION type = parser.instructionType();
            if (type == INSTRUCTION.L_INSTRUCTION) continue;
            else if (type == INSTRUCTION.C_INSTRUCTION) addToFinal = "111" + codeModule.cToBinary(parser.dest(), parser.comp(), parser.jump());
            else if (type == INSTRUCTION.C_INSTRUCTIONS) addToFinal = "111" + codeModule.cToBinary("", parser.dest(), parser.jump());
            else if (type == INSTRUCTION.A_INSTRUCTION) addToFinal = codeModule.toBinary(parser.symbol(type));
            out.append(addToFinal).append("\n");
        }
        System.out.println(out);
        try {
            File fileOut = new File("./out.hack");
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileOut));
            bufferedWriter.write(String.valueOf(out));
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}