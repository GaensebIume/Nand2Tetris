import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tokenizer {
    private final File inPutFile;
    private String currentToken;
    private final HashMap<String, TOKENTYPE> tokenType;
    private final HashMap<String, KEYWORD> keyWord;
    private int indent = 0;
    private final List<KEYWORD> kwdStack = new ArrayList<>();
    private final ArrayList<String> tokens = new ArrayList<>();
    private final ArrayList<String> instructions = new ArrayList<>();
    private int instructionIndex = 0;

    public Tokenizer(File inPutFile){
        this.inPutFile = inPutFile;
        this.tokenType = new HashMap<>();
        this.keyWord = new HashMap<>();
        currentToken = "";
        initMaps();

        //translate File
        tokenize();
        createInstructions();
        writeInstructions();
        System.out.println(tokens);
    }

    //Turns inputFile into arraylist of tokens
    public void tokenize() {
        boolean multipleLineComment = false;
        String beginnCommentString = "/[*]";//TODO: FIX MULTI-LINE COMMENT
        String endCommentString = "[*]/";//TODO: FIX MULTI-LINE COMMENT
        Object[] lines;
        boolean stringConst = false;

        try {
            BufferedReader br = new BufferedReader(new FileReader(inPutFile));
            lines = br.lines().toArray();
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Object iterate : lines) {
            //Filter comments
            String line = ((String) iterate).split("//")[0].stripLeading().stripTrailing();
            boolean beginComment = line.contains(beginnCommentString);
            boolean endComment = line.contains(endCommentString);

            if (multipleLineComment || line.isBlank()) continue;
            if (line.startsWith("/")) continue; //TODO: REMOVE, WHEN MULTI-LINE COMMENT IS FIXED

            if (beginComment && endComment) {
                line = line.split(beginnCommentString)[1].split(endCommentString)[1];
            } else if (beginComment) {
                line = line.split(beginnCommentString)[0];
                multipleLineComment = true;
            } else if (endComment) {
                multipleLineComment = false;
                line = line.split(endCommentString)[1];
            }

            //create Tokens (split by ' ' or a TOKENTYPE:SYMBOL
            StringBuilder lineBuilder = new StringBuilder();
            for (int i = 0; i < line.length(); i++) {
                char cChar = line.charAt(i);
                if (cChar == '"' && line.charAt(i-1) != '\\') {//used to ignore escaped quotation-mark
                    stringConst = !stringConst;
                }
                if (!stringConst && (cChar == ' ' || tokenType.containsKey(String.valueOf(cChar)))) {
                    if (lineBuilder.isEmpty() && cChar == ' ') {//TODO: REWORK LOGIC
                    } else if (cChar == ' ') {
                        tokens.add(lineBuilder.toString());
                    }else {
                        tokens.add(lineBuilder.toString());
                        tokens.add(String.valueOf(cChar));
                    }
                    lineBuilder = new StringBuilder();
                } else {
                    lineBuilder.append(cChar);
                }
            }
        }
    }

    public void createInstructions(){
        for (String currentToken:tokens) {
            if (currentToken.isEmpty()) continue;
            if (tokenType.containsKey(currentToken)){
                if (tokenType.get(currentToken) == TOKENTYPE.KEYWORD) handleKeyWord(currentToken);
                else handleSymbol(currentToken);
            } else if (currentToken.charAt(0) == '"') {//.startswith('"') was not working
                stringVal();
            } else if (currentToken.matches("-?\\d+")) {
                intVal();
            } else { //Identifiers
                writeOpeningClosing(TOKENTYPE.IDENTIFIER, currentToken);
            }
            System.out.println(kwdStack);

            instructionIndex++;
        }
    }

    public void writeInstructions(){
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(inPutFile.getAbsolutePath().substring(0, inPutFile.getAbsolutePath().lastIndexOf(".")) + "_out.xml"));
            for (String instruction:instructions) {
                bw.write(instruction + "\n");
            }
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleKeyWord(String kwd){
        if (!kwd.equals("static") && !kwd.equals("boolean")) writeOpeningTag(keyWord.get(kwd));
        writeOpeningClosing(TOKENTYPE.KEYWORD, kwd);
    }

    private void handleSymbol(String symbol){
        writeOpeningClosing(TOKENTYPE.SYMBOL, symbol);
        if (symbol.equals("{")) handleCurlyOpening();
    }

    public void handleCurlyOpening(){
        String reference = tokens.get(instructionIndex - 2);
        String next = tokens.get(instructionIndex + 1);
        switch (reference) {
            case "class" -> {
                if (!next.equals("function")) writeOpeningTag(KEYWORD.classVarDec);
            }
            case "FUNCTION" -> {
                while (kwdStack.get(kwdStack.size()-2) != KEYWORD.CLASS) {
                    System.out.println("executed");
                    writeClosingTag(getStackTop());
                }
                System.out.println("happens");
                writeOpeningTag(KEYWORD.subroutineDec);
            }
        }
    }


    // OLD CODE

    //tokenise()
    /*
            StringBuilder lineBuilder = new StringBuilder();
            char c;
            for (int i = 0; i < line.length(); i++) {
                c = line.charAt(i);
                currentToken = String.valueOf(c);
                TOKENTYPE tk = tokentype();
                if (c == '"') {
                    stringConst = !stringConst;
                    if (!stringConst) {
                        writeOpeningClosing(TOKENTYPE.STRING_CONST, String.valueOf(lineBuilder));
                        lineBuilder = new StringBuilder();
                        writeAndPopTimes(2);
                    }
                } else if (stringConst){
                    lineBuilder.append(c);
                } else if (Character.isDigit(c)) {
                    if (line.charAt(i-1) == '(' || line.charAt(i-1) == '{' || line.charAt(i-1) == '[') writeOpeningTag(KEYWORD.term);
                    if (line.charAt(i+1) == ' ' || line.charAt(i+1) == ';' || line.charAt(i+1) == ']' || line.charAt(i+1) == ')') {
                        writeOpeningClosing(KEYWORD.integerConstant, String.valueOf(lineBuilder.append(c)));
                        lineBuilder = new StringBuilder();
                    } else lineBuilder.append(c);
                } else if (tk == TOKENTYPE.IDENTIFIER) {
                    if (!(tokenType.containsKey(String.valueOf(line.charAt(i + 1))) || line.charAt(i + 1) == ' '))//filters misidentified chars (such as 'i') when used in non-identifier cases (such as 'String')
                        lineBuilder.append(c);
                    else {
                        evaluateString(String.valueOf(lineBuilder.append(c)));
                        lineBuilder = new StringBuilder();
                    }
                } else if (tk == TOKENTYPE.SYMBOL) {
                    if (!String.valueOf(lineBuilder).isBlank()) {
                        evaluateString(String.valueOf(lineBuilder));
                        lineBuilder = new StringBuilder();
                    }
                    if (c == '{' || c == '(' || c == '[') handleOpeningBracket(c, String.valueOf(lineBuilder));
                    else if (c == '}' || c == ')' || c == ']') handleClosingBracket(c, String.valueOf(lineBuilder));
                    else if (c == ';') handleSemiColon(c, String.valueOf(lineBuilder));
                    else if (c == '=') {
                        writeOpeningClosing(tk, String.valueOf(c));
                        writeOpeningTag(KEYWORD.expression);
                        writeOpeningTag(KEYWORD.term);
                    } else if (c == '+' || c == '/' || c == '*') {
                        if (c == '*')writeAndPopTimes(1);
                        writeOpeningClosing(tk, String.valueOf(c));
                        writeOpeningTag(KEYWORD.term);
                        System.out.println(Arrays.toString(kwdStack.toArray()));
                    } else if (c == '-') {
                            writeOpeningTag(KEYWORD.term);
                            writeOpeningClosing(tk, String.valueOf(c));
                            writeOpeningTag(KEYWORD.term);
                        System.out.println(Arrays.toString(kwdStack.toArray()));
                    } else {
                        writeOpeningClosing(tk, String.valueOf(c));
                        continue;
                    }
                    lineBuilder = new StringBuilder();
                } else if (c == ' ' || c == '.' || c == ',') {
                    evaluateString(String.valueOf(lineBuilder));
                    lineBuilder = new StringBuilder();
                } else if (tk == TOKENTYPE.INVALID) lineBuilder.append(c);
            }
     */
    private void writeAndPopTimes(int i){
        for (int j = 0; j < i; j++) {
            writeClosingTag(kwdStack.get(kwdStack.size() - 1));
            kwdStack.remove(kwdStack.size() - 1);
        }
      }

    public boolean hasMoreTokens(){
        return !tokens.isEmpty();//TODO: WRONG
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

    public int intVal(){//TODO: IMPLEMENT
        /*if (!(tokentype() == TOKENTYPE.INT_CONST)) return Integer.parseInt(currentToken);
        throw new IllegalArgumentException("Not a IDENTIFIER: " + currentToken);*/
        return 0;
    }

    public String stringVal(){
        /*if (currentToken.split("\"")[1].contains("\"")) return currentToken.split("\"")[1].split("\"")[0];
        throw new RuntimeException("NOT A STRING_CONST: " + currentToken + "; " + currentToken.split("\"")[1]);*/
        return "";
    }

    public KEYWORD keyWord(){
        assert tokentype() == TOKENTYPE.KEYWORD;
        return keyWord.get(currentToken);
    }

    private void handleOpeningBracket(char c, String line){//TODO: refactor
        final TOKENTYPE tk = TOKENTYPE.SYMBOL;
        KEYWORD add;
        evaluateString(line);

        if (c == '{' && kwdStack.get(kwdStack.size() - 1) == KEYWORD.CLASS) add = KEYWORD.classVarDec;
        else if (c == '{' && kwdStack.get(kwdStack.size() - 2) == KEYWORD.subroutineDec){
            add = KEYWORD.subroutineBody;
            writeOpeningTag(add);
            writeOpeningClosing(tk, String.valueOf(c));
            return;
        } else if (c == '{' && kwdStack.get(kwdStack.size() - 1) == KEYWORD.ifStatement || getStackTop() == KEYWORD.ELSE) {
            //add = KEYWORD.statements;
            writeOpeningClosing(tk, String.valueOf(c));
            return;
        }
        else if (c == '(' && getStackTop() == KEYWORD.term) add = KEYWORD.expression;
        else if (c == '(' && kwdStack.get(kwdStack.size() - 2) == KEYWORD.subroutineDec) add = KEYWORD.parameterList;
        else if (c == '(' && getStackTop() == KEYWORD.ifStatement) add = KEYWORD.expression;
        else if (c == '[') add = KEYWORD.expression;
        else add = KEYWORD.expressionList;
        writeOpeningClosing(tk, String.valueOf(c));
        writeOpeningTag(add);
    }

    private void handleClosingBracket(char c, String line){
        TOKENTYPE tk = TOKENTYPE.SYMBOL;
        evaluateString(line);
        if ((c == ')') &&
                kwdStack.get(kwdStack.size()-2) == KEYWORD.term &&
                kwdStack.get(kwdStack.size()-3) == KEYWORD.expression){
            writeAndPopTimes(1);
            writeOpeningClosing(tk, String.valueOf(c));
            writeAndPopTimes(2);
        } else if ((c == ')' || c == ']') &&
                kwdStack.get(kwdStack.size() - 1) == KEYWORD.term &&
                kwdStack.get(kwdStack.size() - 2) == KEYWORD.expression){
            writeAndPopTimes(2);
            writeOpeningClosing(tk, String.valueOf(c));
        } else if (c == '}' && kwdStack.size() >= 2 && kwdStack.get(kwdStack.size() - 2) == KEYWORD.subroutineBody) {
            writeAndPopTimes(1);
            writeOpeningClosing(tk, String.valueOf(c));
            writeAndPopTimes(1);
            kwdStack.remove(kwdStack.size() - 1);//removes return values such as VOID
            writeAndPopTimes(1);
        } else {
            writeAndPopTimes(1);
            writeOpeningClosing(tk, String.valueOf(c));
        }
    }

    private void handleSemiColonOLD(char c, String line){
        evaluateString(line);
        KEYWORD lastElementInKwdStack = getStackTop();
        while (lastElementInKwdStack != KEYWORD.STATIC &&
                lastElementInKwdStack != KEYWORD.letStatement &&
                lastElementInKwdStack != KEYWORD.varDec &&
                lastElementInKwdStack != KEYWORD.doStatement &&
                lastElementInKwdStack != KEYWORD.returnStatement){
            if (lastElementInKwdStack == KEYWORD.term || lastElementInKwdStack == KEYWORD.expression) writeAndPopTimes(1);
            else kwdStack.remove(kwdStack.size()-1);
            if (kwdStack.size() == 0){
                if (lastElementInKwdStack == KEYWORD.CLASS) break;
                throw new RuntimeException("kwdStack empty before program finished, " + line);
            }
            lastElementInKwdStack = getStackTop();
        }
        if (lastElementInKwdStack == KEYWORD.STATIC) kwdStack.remove(kwdStack.size()-1);
        writeOpeningClosing(TOKENTYPE.SYMBOL, String.valueOf(c));
        if (getStackTop() == KEYWORD.letStatement ||
                getStackTop() == KEYWORD.doStatement ||
                getStackTop() == KEYWORD.returnStatement||
                getStackTop() == KEYWORD.varDec)
            writeAndPopTimes(1);
    }

    private void evaluateString(String input){
        if (input.isBlank()) return;
        currentToken = input;
        TOKENTYPE tk = tokentype();
        if (tk == TOKENTYPE.KEYWORD) {
            KEYWORD kwd = keyWord();
            if (kwd == KEYWORD.CLASS) writeOpeningTag(kwd);
            else if (kwd == KEYWORD.FUNCTION) {
                if (kwdStack.contains(KEYWORD.classVarDec)) writeAndPopTimes(1);
                writeOpeningTag(KEYWORD.subroutineDec);
            }else if (kwd == KEYWORD.letStatement ||
                    kwd == KEYWORD.returnStatement ||
                    kwd == KEYWORD.doStatement ||
                    kwd == KEYWORD.ifStatement ||
                    kwd == KEYWORD.varDec){
                if (getStackTop() != KEYWORD.statements) writeOpeningTag(KEYWORD.statements);
                writeOpeningTag(kwd);
            } else if (kwd == KEYWORD.FALSE && getStackTop() != KEYWORD.term) writeOpeningTag(KEYWORD.term);
            else kwdStack.add(kwd);
            writeOpeningClosing(tk, input);
        } else {
            if (tk == TOKENTYPE.INVALID) {//Handle unknown identifiers (new variables)
                tk = TOKENTYPE.IDENTIFIER;
                tokenType.put(input, tk);
            }
            if (tk == TOKENTYPE.IDENTIFIER) writeOpeningClosing(tk, input);
            else if (tk == TOKENTYPE.INT_CONST) System.out.print("");//TODO
            else if (tk == TOKENTYPE.STRING_CONST) System.out.print("");//TODO
        }
    }

    private KEYWORD getStackTop(){
        return kwdStack.get(kwdStack.size()-1);
    }

    private void writeOpeningClosing(Enum E, String tag){
        instructions.add("  ".repeat(indent) + "<" + E + "> " + tag + " </" + E + ">");
    }

    private void writeOpeningTag(Enum tag){
        kwdStack.add((KEYWORD) tag);
        instructions.add("  ".repeat(indent) + "<" + tag + ">");
        indent++;
    }

    private void writeClosingTag(Enum E){
        indent--;
        instructions.add("  ".repeat(indent) + "</" + E + ">");
        kwdStack.remove(kwdStack.get(kwdStack.size()-1));
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