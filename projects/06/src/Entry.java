public class Entry {
    private final String asm;
    private final String bin;

    public Entry(String asm, String bin) {
        this.asm = asm;
        this.bin = bin;
    }

    public String getAsm() {
        return asm;
    }

    public String getBin() {
        return bin;
    }
}