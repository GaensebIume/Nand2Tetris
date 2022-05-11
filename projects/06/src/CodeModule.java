import java.util.HashMap;

public class CodeModule {
    private final HashMap<String, String> dest;
    private final HashMap<String, String> comp;
    private final HashMap<String, String> jump;
    private final SymbolTable symbolTable;
    private int nextBin = 16;

    public CodeModule() {
        dest = new HashMap<>();
        comp = new HashMap<>();
        jump = new HashMap<>();
        symbolTable = new SymbolTable();
        //Fill arraylists
        dest.put("ADM", "111");
        dest.put("AMD", "111");
        dest.put("DAM", "111");
        dest.put("DMA", "111");
        dest.put("MAD", "111");
        dest.put("MDA", "111");
        dest.put("AM", "101");
        dest.put("AD", "110");
        dest.put("DM", "011");
        dest.put("MA", "101");
        dest.put("DA", "110");
        dest.put("MD", "011");
        dest.put("M", "001");
        dest.put("D", "010");
        dest.put("A", "100");
        dest.put("null", "000");
        dest.put("", "000");
        jump.put("null", "000");
        jump.put("JGT", "001");
        jump.put("JEQ", "010");
        jump.put("JGE", "011");
        jump.put("JLT", "100");
        jump.put("JNE", "101");
        jump.put("JLE", "110");
        jump.put("JMP", "111");
        jump.put("", "000");
        comp.put("0", "0101010");
        comp.put("1", "0111111");
        comp.put("-1", "0111010");
        comp.put("D", "0001100");
        comp.put("A", "0110000");
        comp.put("!D", "0001101");
        comp.put("!A", "0110001");
        comp.put("-D", "0001111");
        comp.put("-A", "0110011");
        comp.put("D+1", "0011111");
        comp.put("A+1", "0110111");
        comp.put("D-1", "0001110");
        comp.put("A-1", "0110010");
        comp.put("D+A", "0000010");
        comp.put("D-A", "0010011");
        comp.put("A-D", "0000111");
        comp.put("D&A", "0000000");
        comp.put("D|A", "0010101");
        comp.put("M", "1110000");
        comp.put("!M", "1110001");
        comp.put("-M", "1110011");
        comp.put("M+1", "1110111");
        comp.put("M-1", "1110010");
        comp.put("D+M", "1000010");
        comp.put("D-M", "1010011");
        comp.put("M-D", "1000111");
        comp.put("D&M", "1000000");
        comp.put("D|M", "1010101");
    }


    public String cToBinary(String dest, String comp, String jmp){
        return this.comp.get(comp) + this.dest.get(dest) + this.jump.get(jmp);
    }

    public String toBinary(String symbol){//Used for A_ and L_INSTRUCTIONS only
        if(symbolTable.getMap().containsKey(symbol))return symbolTable.getMap().get(symbol);
        try {
            String binSymbol = formatToBin(Integer.parseInt(symbol));
            addCustomA(symbol, binSymbol);
            return binSymbol;
        }catch (Exception ignored){
            String in = formatToBin(nextBin);
            addCustomA(symbol, in);
            nextBin++;
            return in;
        }
    }

    public String formatToBin(int in){
        String binaryString = Integer.toBinaryString(in);
        return String.format("%16s", binaryString).replaceAll(" ", "0");
    }

    public void addCustomA(String asm, String bin){
        symbolTable.getMap().put(asm, bin);
    }
}