import javax.sound.midi.Soundbank;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Tokenizer {
    private final File inPutFile;
    private final File outPutFile;
    private String currentToken;
    private final HashMap<String, TOKENTYPE> tokenType;
    private final HashMap<String, KEYWORD> keyWord;
    private int indent = 0;
    private final List<KEYWORD> kwdStack = new ArrayList<>();
    BufferedWriter bw;

    public Tokenizer(File inPutFile){
        this.inPutFile = inPutFile;
        this.outPutFile = new File(inPutFile.getAbsolutePath().substring(0, inPutFile.getAbsolutePath().lastIndexOf(".")) + "_out.xml");
        this.tokenType = new HashMap<>();
        this.keyWord = new HashMap<>();
        currentToken = "";
        initMaps();

        try {
            bw = new BufferedWriter(new FileWriter(outPutFile));
            tokenize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasMoreTokens(){
        return false; //TODO: IMPLEMENT
    }

    public TOKENTYPE tokentype(){
        if (currentToken.matches("-?\\d"))return TOKENTYPE.INT_CONST; //-? => INT can be negative; \\d => checks for Numbers only
        else if (currentToken.matches("\"*\"")) return TOKENTYPE.STRING_CONST; // currentToken has to start and end with " to be a String
        else if (tokenType.containsKey(currentToken)) return tokenType.get(currentToken);
        return TOKENTYPE.INVALID;
    }

    public char symbol(){
        assert currentToken.length() == 1;//SYMBOLS have to be length 1
        if (!(tokentype() == TOKENTYPE.SYMBOL)) return currentToken.charAt(0);
        throw new IllegalArgumentException("Not a SYMBOL: " + currentToken);
    }

    public String identifier(){
        if (!(tokentype() == TOKENTYPE.IDENTIFIER)) return currentToken;
        throw new IllegalArgumentException("Not a IDENTIFIER: " + currentToken);
    }

    public int intVal(){
        if (!(tokentype() == TOKENTYPE.INT_CONST)) return Integer.parseInt(currentToken);
        throw new IllegalArgumentException("Not a IDENTIFIER: " + currentToken);
    }

    public String stringVal(){
        if (!(tokentype() == TOKENTYPE.STRING_CONST)) return currentToken;
        throw new IllegalArgumentException("Not a STRING_CONST: " + currentToken);
    }

    public KEYWORD keyWord(){
        assert tokentype() == TOKENTYPE.KEYWORD;
        return keyWord.get(currentToken);
    }

    public void tokenize() throws Exception {//TODO: REMOVE throws [...] (if possible)
        boolean multipleLineComment = false;
        String beginnCommentString = "/[*]";//TODO: FIX MULTI-LINE COMMENT
        String endCommentString = "[*]/";//TODO: FIX MULTI-LINE COMMENT
        Object[] lines;
        try {
            BufferedReader br = new BufferedReader(new FileReader(inPutFile));
            lines = br.lines().toArray();
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Object iterate: lines){
            if (multipleLineComment) continue;

            String line = ((String) iterate).split("//")[0];
            line = line.stripLeading().stripTrailing();

            if (line.isBlank()) continue;

            boolean beginComment = line.contains(beginnCommentString);
            boolean endComment = line.contains(endCommentString);
            if (line.startsWith("/")) continue; //TODO: REMOVE, WHEN MULTI-LINE COMMENT IS FIXED

            if (beginComment && endComment){
                line = line.split(beginnCommentString)[1].split(endCommentString)[1];
            } else if (beginComment) {
                line = line.split(beginnCommentString)[0];
                multipleLineComment = true;
            }else if (endComment){
                multipleLineComment = false;
                line = line.split(endCommentString)[1];
            }

            StringBuilder lineBuilder = new StringBuilder();
            char c;
            for (int i = 0; i < line.length(); i++) {
                c = line.charAt(i);
                currentToken = String.valueOf(c);
                TOKENTYPE tk = tokentype();
                if (tk == TOKENTYPE.SYMBOL) {
                    if (!String.valueOf(lineBuilder).isBlank()) {
                        evaluateString(String.valueOf(lineBuilder));
                        lineBuilder = new StringBuilder();
                    }
                    if (c == '{' || c == '(') handleOpeningBracket(c, String.valueOf(lineBuilder));
                    else if (kwdStack.size() == 0) throw new RuntimeException("kwdStack.size == 0");//TODO: REMOVE IN FINAL VERSION
                    else if (c == '}' || c == ')') handleClosingBracket(c, String.valueOf(lineBuilder));
                    else if (c == ';') handleSemiColon(c, String.valueOf(lineBuilder));
                    else if (c == '=') {
                        writeOpeningClosing(tk, String.valueOf(c));
                        writeOpeningTag(KEYWORD.expression);
                        writeOpeningTag(KEYWORD.term);
                    } else {
                        writeOpeningClosing(tk, String.valueOf(c));
                        continue;
                    }
                    lineBuilder = new StringBuilder();
                } else if (c == ' ' || c == '.') {
                    evaluateString(String.valueOf(lineBuilder));
                    lineBuilder = new StringBuilder();
                } else if (tk == TOKENTYPE.INVALID) lineBuilder.append(c);
            }
        }
        bw.close();
        if (!kwdStack.isEmpty()) System.out.println(Arrays.toString(kwdStack.toArray()) + " <= is end of file");
    }

    private void writeAndPopTimes(int i){
        for (int j = 0; j < i; j++) {
            writeClosingTag(kwdStack.get(kwdStack.size() - 1));
            kwdStack.remove(kwdStack.size() - 1);
        }
    }

    private void handleOpeningBracket(char c, String line){//TODO: refactor
        final TOKENTYPE tk = TOKENTYPE.SYMBOL;
        KEYWORD add;
        evaluateString(line);

        if (c == '{' && kwdStack.get(kwdStack.size() - 1) == KEYWORD.CLASS) {
            add = KEYWORD.classVarDec;
            writeOpeningClosing(tk, String.valueOf(c));
        } else if (c == '(' && kwdStack.get(kwdStack.size() - 2) == KEYWORD.subroutineDec) {
            add = KEYWORD.parameterList;
            writeOpeningClosing(tk, String.valueOf(c));
        } else if (c == '{' && kwdStack.get(kwdStack.size() - 2) == KEYWORD.subroutineDec){
            add = KEYWORD.subroutineBody;
            writeOpeningTag(add);
            writeOpeningClosing(tk, String.valueOf(c));
            add = KEYWORD.varDec;
        } else {
            add = KEYWORD.expressionList;
            writeOpeningClosing(tk, String.valueOf(c));
        }
        writeOpeningTag(add);
    }

    private void handleClosingBracket(char c, String line){
        TOKENTYPE tk = TOKENTYPE.SYMBOL;
        evaluateString(line);
        if (c == ')' && kwdStack.get(kwdStack.size()-2) == KEYWORD.term && kwdStack.get(kwdStack.size()-3) == KEYWORD.expression){
            writeAndPopTimes(1);
            writeOpeningClosing(tk, String.valueOf(c));
            writeAndPopTimes(2);
        } else if (c == '}' && kwdStack.size()>=2 && kwdStack.get(kwdStack.size()-2) == KEYWORD.subroutineBody) {
            writeAndPopTimes(1);
            writeOpeningClosing(tk, String.valueOf(c));
            writeAndPopTimes(1);
            kwdStack.remove(kwdStack.size()-1);//removes return values such as VOID
            writeAndPopTimes(1);
        } else if (indent > 0) {
            writeAndPopTimes(1);
            writeOpeningClosing(tk, String.valueOf(c));
        } else {
            writeOpeningClosing(tk, String.valueOf(c));
        }
    }

    private void handleSemiColon(char c, String line){
        evaluateString(line);
        KEYWORD lastElementInKwdStack = kwdStack.get(kwdStack.size()-1);
        while (lastElementInKwdStack != KEYWORD.STATIC &&
                lastElementInKwdStack != KEYWORD.letStatement &&
                lastElementInKwdStack != KEYWORD.VAR &&
                lastElementInKwdStack != KEYWORD.doStatement &&
                lastElementInKwdStack != KEYWORD.RETURN &&
                lastElementInKwdStack != KEYWORD.returnStatement){
            kwdStack.remove(kwdStack.size()-1);
            if (kwdStack.size() == 0){
                if (lastElementInKwdStack == KEYWORD.CLASS) break;
                throw new RuntimeException("kwdStack empty before program finished, " + line);
            }
            lastElementInKwdStack = kwdStack.get(kwdStack.size()-1);
        }
        if (lastElementInKwdStack == KEYWORD.STATIC || lastElementInKwdStack == KEYWORD.VAR)kwdStack.remove(kwdStack.size()-1);
        writeOpeningClosing(TOKENTYPE.SYMBOL, String.valueOf(c));
        if (kwdStack.get(kwdStack.size()-1) == KEYWORD.letStatement ||
                kwdStack.get(kwdStack.size()-1) == KEYWORD.doStatement ||
                kwdStack.get(kwdStack.size()-1) == KEYWORD.returnStatement)
            writeAndPopTimes(1);
    }

    private void evaluateString(String input){
        if (input.isBlank()) return;
        currentToken = input;
        TOKENTYPE tk = tokentype();
        if (tk == TOKENTYPE.KEYWORD) {
            KEYWORD kwd = keyWord();
            if (kwd == KEYWORD.CLASS){
                writeOpeningTag(kwd);
            }else if (kwd == KEYWORD.FUNCTION) {
                if (kwdStack.contains(KEYWORD.classVarDec)) writeAndPopTimes(1);
                writeOpeningTag(KEYWORD.subroutineDec);
            }else if (kwd == KEYWORD.LET || kwd == KEYWORD.DO || kwd == KEYWORD.RETURN){
                if (kwdStack.get(kwdStack.size()-1) == KEYWORD.varDec){
                    writeAndPopTimes(1);
                    writeOpeningTag(KEYWORD.statements);
                }
                if (kwd == KEYWORD.LET) kwd = KEYWORD.letStatement;
                else if (kwd == KEYWORD.DO) kwd = KEYWORD.doStatement;
                else kwd = KEYWORD.returnStatement;
                writeOpeningTag(kwd);
            }else{
                kwdStack.add(kwd);
            }
            writeOpeningClosing(tk, input);
        } else {
            if (tk == TOKENTYPE.INVALID) {//Handle unknown identifiers
                tk = TOKENTYPE.IDENTIFIER;
                tokenType.put(input, tk);
            }
            if (tk == TOKENTYPE.IDENTIFIER) writeOpeningClosing(tk, input);
            else if (tk == TOKENTYPE.INT_CONST) System.out.print("");//TODO
            else if (tk == TOKENTYPE.STRING_CONST) System.out.print("");//TODO
        }
    }

    private void writeToFile(String line){
        try {
            bw.write(line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeOpeningClosing(Enum E, String tag){
        writeToFile("  ".repeat(indent) + "<" + E + "> " + tag + " </" + E + ">\n");
    }

    private void writeOpeningTag(Enum tag){
        kwdStack.add((KEYWORD) tag);
        writeToFile("  ".repeat(indent) + "<" + tag + ">\n");
        indent++;
    }

    private void writeClosingTag(Enum E){
        indent--;
        writeToFile("  ".repeat(indent) + "</" + E + ">\n");
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
        tokenType.put("&/", TOKENTYPE.SYMBOL);
        tokenType.put("|", TOKENTYPE.SYMBOL);
        tokenType.put("<", TOKENTYPE.SYMBOL);
        tokenType.put(">", TOKENTYPE.SYMBOL);
        tokenType.put("=", TOKENTYPE.SYMBOL);
        tokenType.put("~", TOKENTYPE.SYMBOL);

        keyWord.put("class", KEYWORD.CLASS);
        keyWord.put("method", KEYWORD.METHOD);
        keyWord.put("function", KEYWORD.FUNCTION);
        keyWord.put("constructor", KEYWORD.CONSTRUCTOR);
        keyWord.put("int", KEYWORD.INT);
        keyWord.put("boolean", KEYWORD.BOOLEAN);
        keyWord.put("char", KEYWORD.CHAR);
        keyWord.put("void", KEYWORD.VOID);
        keyWord.put("var", KEYWORD.VAR);
        keyWord.put("static", KEYWORD.STATIC);
        keyWord.put("field", KEYWORD.FIELD);
        keyWord.put("let", KEYWORD.LET);
        keyWord.put("do", KEYWORD.DO);
        keyWord.put("if", KEYWORD.IF);
        keyWord.put("else", KEYWORD.ELSE);
        keyWord.put("while", KEYWORD.WHILE);
        keyWord.put("return", KEYWORD.RETURN);
        keyWord.put("true", KEYWORD.TRUE);
        keyWord.put("false", KEYWORD.FALSE);
        keyWord.put("null", KEYWORD.NULL);
        keyWord.put("this",KEYWORD.THIS);
    }
}
