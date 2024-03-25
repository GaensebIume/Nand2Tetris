import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tokenizer {
    private List<String> lines;
    private String currentToken;
    private int tokenIndex;
    private HashMap<String, TOKENTYPE> tokenType;
    private HashMap<String, KEYWORD> keyWord;
    private ArrayList<String> rawTokens;
	private ArrayList<Enum> tokens;
    private boolean multipleLineComment;

    public Tokenizer(File inFile){
        multipleLineComment = false;
        tokenIndex = 0;
        this.tokenType = new HashMap<>();
        this.keyWord = new HashMap<>();
        this.rawTokens = new ArrayList<>();
        this.tokens = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(inFile));
            lines = br.lines().toList();
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initMaps();
        generateRaw();
        generateTokens();
    }
    
    private void generateTokens(){
        currentToken = rawTokens.get(0);
        while (hasMoreTokens()) {
            if(tokenType() == TOKENTYPE.KEYWORD) tokens.add(keyWord());
            else tokens.add(tokenType());
            advance();
        }
    }

    /*
     * generates raw tokens. Comments are ignored, but some rare constructs of (multiline-) comments might not be handled properly.
     */
    private void generateRaw(){
        String multiStringholer = "";
        for (int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            if (line.contains("//")) line = line.split("//")[0];
            if (line.contains("/[*]")) {
                line = line.split("/[*]")[0];
                multipleLineComment = true;
            }
            if (line.contains("[*]/")){
                line = line.split("[*]/")[1];
                multipleLineComment = false;
            }

            line = line.stripLeading().stripTrailing();
            if (line.isEmpty() || multipleLineComment) continue;

            int lastI = 0;
            boolean stringConst = false;
            for (int j = 0; j < line.length(); j++){
                char cChar = line.charAt(j);
                if (cChar == '"' && line.charAt(j - 1) != '\\') { //used to ignore escaped quotation-mark (might be used in Strings)
                    stringConst = !stringConst;
                    if (!stringConst){
                        addToRaw(multiStringholer + line.substring(lastI, j - 1));
                        lastI = j;
                        multiStringholer = "";
                    }
                }
                if (stringConst) continue;      
                else if (cChar == ' ') {
                    addToRaw(line.substring(lastI, j));
                    lastI = j + 1;
                } else if (tokenType.get(String.valueOf(cChar)) != null){
                    addToRaw(line.substring(lastI, j));
                    addToRaw(String.valueOf(line.charAt(j)));
                    lastI = j + 1;
                }
                int s = rawTokens.size() - 1;
                if (!rawTokens.isEmpty() && (rawTokens.get(s).equals(" ") || 
                    rawTokens.get(s).equals(""))) rawTokens.remove(s);//removes spaces that are added
                //no need to check for end of line, since every line ends in a symbol. 
            }
            if(stringConst) multiStringholer += line.substring(lastI, line.length() - 1);
        }
    }

    private boolean hasMoreTokens(){
        return tokenIndex < rawTokens.size() - 1;
    }

    private void advance(){
        assert hasMoreTokens();
        tokenIndex++;
        currentToken = rawTokens.get(tokenIndex);
    }

    private TOKENTYPE tokenType(){
        if (currentToken.matches("-?\\d"))return TOKENTYPE.INT_CONST; //-? => INT can be negative; \\d => checks for Numbers only
        else if (currentToken.startsWith("\"") && currentToken.endsWith("\"")) return TOKENTYPE.STRING_CONST;// currentToken has to start and end with " to be a String
        else if (tokenType.containsKey(currentToken)) return tokenType.get(currentToken);
        else return TOKENTYPE.IDENTIFIER;//might need to be TOKENTYPE.INVALID
    }

    private KEYWORD keyWord(){
        assert tokenType() == TOKENTYPE.KEYWORD;
        return keyWord.get(currentToken);
    }

    /* 
These method are, as of now, obsolete.

    private char symbol(){
        assert tokenType() == TOKENTYPE.SYMBOL;
        return currentToken.charAt(0);
    }

    private String identifier(){
        assert tokenType() == TOKENTYPE.IDENTIFIER;
        return currentToken;
    }

    private int intVal(){
        assert tokenType() == TOKENTYPE.INT_CONST;
        return Integer.valueOf(currentToken);
    }

    private String stringVal(){
        assert tokenType() == TOKENTYPE.STRING_CONST;
        return currentToken.substring(1, currentToken.length() - 2);
    }
*/
    public ArrayList<Enum> getTokens(){
        return tokens;
    }

    public ArrayList<String> getRaw(){
        return rawTokens;
    }

    public void addToRaw(String s){
        if(!s.isEmpty()) rawTokens.add(s);
    }

    private void initMaps(){
        tokenType.put("class", TOKENTYPE.KEYWORD);
        tokenType.put("method", TOKENTYPE.KEYWORD);
        tokenType.put("function", TOKENTYPE.KEYWORD);
        tokenType.put("constructor",TOKENTYPE.KEYWORD);
        tokenType.put("int", TOKENTYPE.KEYWORD);
        tokenType.put("boolean", TOKENTYPE.KEYWORD);
        tokenType.put("char", TOKENTYPE.KEYWORD);
        tokenType.put("void", TOKENTYPE.KEYWORD);
        tokenType.put("var", TOKENTYPE.KEYWORD);
        tokenType.put("static", TOKENTYPE.KEYWORD);
        tokenType.put("field", TOKENTYPE.KEYWORD);
        tokenType.put("let", TOKENTYPE.KEYWORD);
        tokenType.put("do", TOKENTYPE.KEYWORD);
        tokenType.put("if", TOKENTYPE.KEYWORD);
        tokenType.put("else", TOKENTYPE.KEYWORD);
        tokenType.put("while", TOKENTYPE.KEYWORD);
        tokenType.put("return", TOKENTYPE.KEYWORD);
        tokenType.put("true", TOKENTYPE.KEYWORD);
        tokenType.put("false", TOKENTYPE.KEYWORD);
        tokenType.put("null", TOKENTYPE.KEYWORD);
        tokenType.put("this",TOKENTYPE.KEYWORD);
        tokenType.put("{", TOKENTYPE.SYMBOL);
        tokenType.put("}", TOKENTYPE.SYMBOL);
        tokenType.put("(", TOKENTYPE.SYMBOL);
        tokenType.put(")", TOKENTYPE.SYMBOL);
        tokenType.put("[", TOKENTYPE.SYMBOL);
        tokenType.put("]", TOKENTYPE.SYMBOL);
        tokenType.put(".", TOKENTYPE.SYMBOL);
        tokenType.put(",", TOKENTYPE.SYMBOL);
        tokenType.put(";", TOKENTYPE.SYMBOL);
        tokenType.put("+", TOKENTYPE.SYMBOL);
        tokenType.put("-", TOKENTYPE.SYMBOL);
        tokenType.put("*", TOKENTYPE.SYMBOL);
        tokenType.put("/", TOKENTYPE.SYMBOL);
        tokenType.put("&", TOKENTYPE.SYMBOL);
        tokenType.put("|", TOKENTYPE.SYMBOL);
        tokenType.put("<", TOKENTYPE.SYMBOL);
        tokenType.put(">", TOKENTYPE.SYMBOL);
        tokenType.put("=", TOKENTYPE.SYMBOL);
        tokenType.put("~", TOKENTYPE.SYMBOL);

        keyWord.put("class", KEYWORD.CLASS);
        keyWord.put("method", KEYWORD.METHOD);
        keyWord.put("function", KEYWORD.subroutineDec);
        keyWord.put("constructor", KEYWORD.CONSTRUCTOR);
        keyWord.put("int", KEYWORD.INT);
        keyWord.put("boolean", KEYWORD.BOOLEAN);
        keyWord.put("char", KEYWORD.CHAR);
        keyWord.put("void", KEYWORD.VOID);
        keyWord.put("var", KEYWORD.varDec);
        keyWord.put("static", KEYWORD.STATIC);
        keyWord.put("field", KEYWORD.FIELD);
        keyWord.put("let", KEYWORD.letStatement);
        keyWord.put("do", KEYWORD.doStatement);
        keyWord.put("if", KEYWORD.ifStatement);
        keyWord.put("else", KEYWORD.ELSE);
        keyWord.put("while", KEYWORD.WHILE);
        keyWord.put("return", KEYWORD.returnStatement);
        keyWord.put("true", KEYWORD.TRUE);
        keyWord.put("false", KEYWORD.FALSE);
        keyWord.put("null", KEYWORD.NULL);
        keyWord.put("this",KEYWORD.THIS);
    }
}
