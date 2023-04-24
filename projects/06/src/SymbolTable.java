import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, String> lookUpMap;

    public SymbolTable(){
        lookUpMap = new HashMap<>();
        lookUpMap.put("SP", "0000000000000000");
        lookUpMap.put("LCL", "0000000000000001");
        lookUpMap.put("ARG", "0000000000000010");
        lookUpMap.put("THIS", "0000000000000011");
        lookUpMap.put("THAT", "0000000000000100");
        lookUpMap.put("R0", "0000000000000000");
        lookUpMap.put("R1", "0000000000000001");
        lookUpMap.put("R2", "0000000000000010");
        lookUpMap.put("R3", "0000000000000011");
        lookUpMap.put("R4", "0000000000000100");
        lookUpMap.put("R5", "0000000000000101");
        lookUpMap.put("R6", "0000000000000110");
        lookUpMap.put("R7", "0000000000000111");
        lookUpMap.put("R8", "0000000000001000");
        lookUpMap.put("R9", "0000000000001001");
        lookUpMap.put("R10", "0000000000001010");
        lookUpMap.put("R11", "0000000000001011");
        lookUpMap.put("R12", "0000000000001100");
        lookUpMap.put("R13", "0000000000001101");
        lookUpMap.put("R14", "0000000000001110");
        lookUpMap.put("R15", "0000000000001111");
        lookUpMap.put("SCREEN", "0100000000000000");
        lookUpMap.put("KBD", "1100000000000000");
    }

    public HashMap<String, String> getMap(){
        return lookUpMap;
    }
}