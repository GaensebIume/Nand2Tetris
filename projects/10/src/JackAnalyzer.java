import java.io.File;
import java.util.ArrayList;

public class JackAnalyzer {
    public static void main(String[] args) {
        //assert args.length == 1 && args[0].endsWith(".jack");// TODO: UNCOMMENT for final release
        //String filepath = args[0];
        String filepath = "projects/10/Square/Main.jack"; // TODO: REMOVE line when finished

        //Object Setup
        Tokenizer tokenizer = new Tokenizer(new File(filepath));
        File outFile = new File(filepath.substring(0,filepath.length() - 5) + "_out.xml");
        CompliationEngine engine = new CompliationEngine(tokenizer.getTokens(), outFile);
        
        System.out.println(tokenizer.getRaw());
        System.out.println(tokenizer.getTokens());

        //Execution
        ArrayList<Enum> tokens = tokenizer.getTokens();
        ArrayList<String> raw = tokenizer.getRaw();
        engine.setRawTokens(raw);
        engine.compile();
    }
}
