import java.io.File;
import java.util.ArrayList;

public class CompliationEngine {
    private ArrayList<Enum> tokens;
    private ArrayList<String> instructions;
    private ArrayList<Enum> heap;
    private InstructionWriter writer;
    private ArrayList<String> rawTokens;
    private File outFile;
    private int counter;


    public CompliationEngine(ArrayList<Enum> tokens, File outFile){
        this.tokens = tokens;
        this.outFile = outFile;
        this.instructions = new ArrayList<>();
        this.heap = new ArrayList<>();
        this.counter = 0;
        writer = new InstructionWriter();
    }

    public void compile(){
        compileClass();
        writer.writeInstructions(instructions, outFile);
    }

    public void compileClass(){
        System.out.println("class");
        instructions.add("<" + KEYWORD.CLASS + ">");
        heap.add(KEYWORD.CLASS);
        instructions.add("<" + TOKENTYPE.KEYWORD + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.KEYWORD + ">");
        counter++; //element now is idenetifier
        instructions.add("<" + TOKENTYPE.IDENTIFIER + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.IDENTIFIER + ">");
        counter++; //element now is '{'
        instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");
        counter++; //element now is either static (for classVarDec) or function (for subroutineDec)
        while(!rawTokens.get(counter).equals("}")){
            if (rawTokens.get(counter).equals("static")) compileClassVarDec();
            else if (rawTokens.get(counter).equals("function")) compileSubroutineDec();
            if (counter == rawTokens.size()) break;
        }
        instructions.add("<" + TOKENTYPE.SYMBOL + "> } </" + TOKENTYPE.SYMBOL + ">");//final cloning bracket
        popHeap();
    }

    public void compileClassVarDec(){//element now is 'static'
    System.out.println("classvardec");
        if (heap.getLast() != KEYWORD.classVarDec) {
            instructions.add("<" + KEYWORD.classVarDec + ">");
            heap.add(KEYWORD.classVarDec);
        }
        instructions.add("<" + TOKENTYPE.KEYWORD + ">" + rawTokens.get(counter) + "</" + TOKENTYPE.KEYWORD + ">");
        counter++; //element now is dataType of Var (e.g. INT, BOOLEAN, ...)
        instructions.add("<" + TOKENTYPE.KEYWORD + ">" + rawTokens.get(counter) + "</" + TOKENTYPE.KEYWORD + ">");
        counter++; //element now is identifier (Varname)
        instructions.add("<" + TOKENTYPE.IDENTIFIER + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.IDENTIFIER + ">");
        counter++; //element now is semicolon
        instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");
        counter++; // element now is next line; eiter another static or a function
        if (!rawTokens.get(counter).equals("static")) popHeap();
        else compileClassVarDec();
    }

    public void compileSubroutineDec(){
        System.out.println("subdec");
        instructions.add("<" + KEYWORD.subroutineDec + ">");
        heap.add(KEYWORD.subroutineDec);
        instructions.add("<" + TOKENTYPE.KEYWORD + ">" + rawTokens.get(counter) + "<" + TOKENTYPE.KEYWORD + "/>");//element now is 'function'
        counter++; //element now is function return-Type
        instructions.add("<" + TOKENTYPE.KEYWORD + ">" + rawTokens.get(counter) + "<" + TOKENTYPE.KEYWORD + "/>");
        counter++; //element now is identifier
        instructions.add("<" + TOKENTYPE.IDENTIFIER + ">" + rawTokens.get(counter) + "<" + TOKENTYPE.IDENTIFIER + "/>");
        counter++; //element now is '('
        instructions.add("<" + TOKENTYPE.SYMBOL + "> ( </" + TOKENTYPE.SYMBOL + ">");
        compileParameterList();//element now is ')'
        instructions.add("<" + TOKENTYPE.SYMBOL + "> ) </" + TOKENTYPE.SYMBOL + ">"); 
        counter++; //element now is '{'
        compileSubroutineBody();//element now is '}'
        popHeap();
    }

    public void compileParameterList(){
        //element now is '('
        instructions.add("<" + KEYWORD.parameterList + ">");
        heap.add(KEYWORD.parameterList);
        counter++; //element now is either ')' (no parameterlist) or dataType of first parameter
        while (!rawTokens.get(counter).equals(")")){
            instructions.add("<" + TOKENTYPE.KEYWORD + ">" + rawTokens.get(counter) + "<" + TOKENTYPE.KEYWORD + "/>");
            counter++; //element now is idenetifier
            instructions.add("<" + TOKENTYPE.IDENTIFIER + ">" + rawTokens.get(counter) + "<" + TOKENTYPE.IDENTIFIER + "/>");
            counter++; //element now is either dataType of next parameter (loop repeats) or ')'
        }
        popHeap();
    }

    public void compileSubroutineBody(){//element now is '{'
        System.out.println("subbody");
        instructions.add("<" + KEYWORD.subroutineBody + ">");
        heap.add(KEYWORD.subroutineBody);
        instructions.add("<" + TOKENTYPE.SYMBOL + "> { </" + TOKENTYPE.SYMBOL + "/>");
        counter++; //element now is either 'var', or a statment
        while(!rawTokens.get(counter).equals("}")){
            switch (rawTokens.get(counter)) {
                case "let", "do", "if", "while", "return":
                    compileStatements();
                    break;
                case "var":
                    compileVarDec();
                    break;
            }
        }
        counter++; //element is now either 'static', 'function' or '}' (end of class)
        instructions.add("<" + TOKENTYPE.SYMBOL + "> } </" + TOKENTYPE.SYMBOL + ">"); 
        popHeap();
    }

    public void compileVarDec(){//element now is var
        System.out.println("vardec");
        if (heap.getLast() != KEYWORD.varDec) {
            heap.add(KEYWORD.varDec);
            instructions.add("<" + KEYWORD.varDec + ">");
        }
        instructions.add("<" + TOKENTYPE.SYMBOL + ">" + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");
        counter++; //element now is dataType of var
        instructions.add("<" + TOKENTYPE.SYMBOL + ">" + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");
        while (!rawTokens.get(counter).equals(";")){
            counter++; //element now is identifier of var
            instructions.add("<" + TOKENTYPE.IDENTIFIER + ">" + rawTokens.get(counter) + " </" + TOKENTYPE.IDENTIFIER +">");
            counter++; //element now is ';' or ',' (multiple vardec in one line)
            instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");
        }
        counter++; //element now is in next line; either another varDec or a statment
        popHeap();
        if (rawTokens.get(counter).equals("var")) compileVarDec();
    }

    public void compileStatements(){//element is now on of the statements
        System.out.println("let");
        if(heap.getLast() != KEYWORD.statements){
            System.out.println(heap.getLast());
            
            instructions.add("<" + KEYWORD.statements + ">");
            heap.add(KEYWORD.statements);
        }
        System.out.println(rawTokens.get(counter));
        switch (rawTokens.get(counter)) {
            case "let":
                compileLet();
                break;
            case "do":
                compileDo();
                break;
            case "if":
                compileIf();
                break;
            case "while":
                compileWhile();
                break;
            case "return":
                compileReturn();
                break;
        }
        String top = rawTokens.get(counter);        
        if (top.matches("do|let|if|while|return")) compileStatements(); // next line is either statement,
        else popHeap();
        if (top.equals("var")) compileVarDec(); // var, or function has ended
    }

    public void compileDo(){//element now is 'do'
        System.out.println("do");
        instructions.add("<" + KEYWORD.doStatement + ">");
        heap.add(KEYWORD.doStatement);
        instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");//writes do
        counter++; //element now is function or object name
        while (!rawTokens.get(counter).equals(";")){
            if (rawTokens.get(counter).equals(".")) instructions.add("<" + TOKENTYPE.SYMBOL + "> . </" + TOKENTYPE.SYMBOL + ">");
            else if (rawTokens.get(counter).equals("(")) {
                instructions.add("<" + TOKENTYPE.SYMBOL + "> ( </" + TOKENTYPE.SYMBOL + ">");
                compileExpressionList();//element now is ')'
                instructions.add("<" + TOKENTYPE.SYMBOL + "> ) </" + TOKENTYPE.SYMBOL + ">");
                counter++; //element now is ';' or '.'
            } else instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");
            counter++; //element is now either dot '.', name of an object/function, '(' or ';' (end of line)
        }
        instructions.add("<" + TOKENTYPE.SYMBOL + "> ; </" + TOKENTYPE.SYMBOL + ">");
        counter++; //element now is in next line
        popHeap();//end of do statment
    }

    public void compileLet(){//element is now 'let'
        instructions.add("<" + KEYWORD.letStatement + ">");
        heap.add(KEYWORD.letStatement);
        instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");//writes let
        counter++; //element now is function or object name
        while (!rawTokens.get(counter).equals("=")){
            if (rawTokens.get(counter).equals(".")) instructions.add("<" + TOKENTYPE.SYMBOL + "> . </" + TOKENTYPE.SYMBOL + ">");
            else if (rawTokens.get(counter).equals("[")){
                instructions.add("<" + TOKENTYPE.SYMBOL + "> [ </" + TOKENTYPE.SYMBOL + ">");
                compileExpression();
                instructions.add("<" + TOKENTYPE.SYMBOL + "> ] </" + TOKENTYPE.SYMBOL + ">");
            }
            else instructions.add("<" + TOKENTYPE.IDENTIFIER + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.IDENTIFIER + ">");
            counter++; //element is now either dot '.', name of an object/function or '='
        }
        instructions.add("<" + TOKENTYPE.SYMBOL + "> = </" + TOKENTYPE.SYMBOL + ">");
        counter++;
        compileExpression();//element now is semicolon
        instructions.add("<" + TOKENTYPE.SYMBOL + "> ; </" + TOKENTYPE.SYMBOL + ">");
        counter++; //element now is in next line
        popHeap();//end of let statment
    }

    public void compileWhile(){//element now is 'while'
        System.out.println("while");
        instructions.add("<" + KEYWORD.WHILE + ">");
        heap.add(KEYWORD.ifStatement);
        instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");//writes if
        counter++; //element now is '('
        instructions.add("<" + TOKENTYPE.SYMBOL + "> ( </" + TOKENTYPE.SYMBOL + ">");
        counter++;
        compileExpression();//element now is ')'
        instructions.add("<" + TOKENTYPE.SYMBOL + "> ) </" + TOKENTYPE.SYMBOL + ">");
        counter++; //element now is '{'
        instructions.add("<" + TOKENTYPE.SYMBOL + "> { </" + TOKENTYPE.SYMBOL + ">");
        counter++;//element is now first command in while-Block
        while (!rawTokens.get(counter).equals("}")){
            if (rawTokens.get(counter).equals("var")) compileVarDec();
            else compileStatements();
        }//element is now '}'
        instructions.add("<" + TOKENTYPE.SYMBOL + "> } </" + TOKENTYPE.SYMBOL + ">");
        counter++;//element is now in the next line
        popHeap();//end of while statment
    }

    public void compileReturn(){//element now is 'return'
        System.out.println("return");
        instructions.add("<" + KEYWORD.returnStatement + ">");
        heap.add(KEYWORD.returnStatement);
        instructions.add("<" + TOKENTYPE.SYMBOL + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.SYMBOL + ">");//writes return
        counter++; //element now is function or object name
        while (!rawTokens.get(counter).equals(";")){
            if (rawTokens.get(counter).equals(".")) instructions.add("<" + TOKENTYPE.SYMBOL + "> . </" + TOKENTYPE.SYMBOL + ">");
            else instructions.add("<" + TOKENTYPE.IDENTIFIER + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.IDENTIFIER + ">");
            counter++; //element is now either dot '.', name of an object/function or ';'
        }//element now is semicolon
        instructions.add("<" + TOKENTYPE.SYMBOL + "> ; </" + TOKENTYPE.SYMBOL + ">");
        counter++; //element now is in next line
        popHeap();//end of return statment
    }

    public void compileIf(){//element now is 'if' or 'else'
    System.out.println("if");
        if (rawTokens.get(counter).equals("if")) {
            instructions.add("<" + KEYWORD.ifStatement + ">");
            heap.add(KEYWORD.ifStatement);
            instructions.add("<" + TOKENTYPE.KEYWORD + "> " + rawTokens.get(counter) + " </" + TOKENTYPE.KEYWORD + ">");//writes if
            counter++; //element now is '('
            instructions.add("<" + TOKENTYPE.SYMBOL + "> ( </" + TOKENTYPE.SYMBOL + ">");
            counter++;
            compileExpression();//element now is ')'
            instructions.add("<" + TOKENTYPE.SYMBOL + "> ) </" + TOKENTYPE.SYMBOL + ">");
        } else instructions.add("<" + TOKENTYPE.KEYWORD + ">" + rawTokens.get(counter) + "<" + TOKENTYPE.KEYWORD + "/>"); //writes else
        counter++; //element now is '{'
        instructions.add("<" + TOKENTYPE.SYMBOL + "> { </" + TOKENTYPE.SYMBOL + ">");
        counter++;//element is now first command in if-Block
        while (!rawTokens.get(counter).equals("}") && !rawTokens.get(counter).equals("else")){
            System.out.println(rawTokens.get(counter) + "lopiupiou3948");
            if (rawTokens.get(counter).equals("var")) compileVarDec();
            else if (rawTokens.get(counter).matches("do|let|if|while|return")) compileStatements(); 
        }//element is now '}'
        instructions.add("<" + TOKENTYPE.SYMBOL + "> } </" + TOKENTYPE.SYMBOL + ">");
        System.out.println(rawTokens.get(counter) + "sakjdhks");
        if (rawTokens.get(counter).equals("else")) {
            System.out.println("trigger");
            compileIf();
        }
        counter++; //new command
        popHeap();//end of if statment
        if (rawTokens.get(counter).equals("var")) {
            System.out.println("var");
            compileVarDec();
        }
        else if (rawTokens.get(counter).matches("do|let|if|while|return")) {
            System.out.println("statment");
            compileStatements(); 
        }
    }

    public void compileExpression(){
        System.out.println("expression");
        instructions.add("<" + KEYWORD.expression + ">");
        heap.add(KEYWORD.expression);
        compileTerm();//TODO: correct?
        popHeap();
    }

    public void compileTerm(){
        System.out.println("term");
        while(!rawTokens.get(counter).equals(";") && !rawTokens.get(counter).equals(")")){
            counter++;//TODO: IMPLEMENT
            System.out.println(rawTokens.get(counter));
        }
        counter++;
    }

    public void compileExpressionList(){
        System.out.println("exlist");
        instructions.add("<" + KEYWORD.expressionList + ">");
        heap.add(KEYWORD.expressionList);
        if(!rawTokens.get(counter + 1).equals(")")){
            //do something // TODO: IMPLEMENT
        }
        popHeap();
    }

    public void popHeap(){
        try {
            
        instructions.add("</" + heap.getLast() + ">");
        heap.remove(heap.size() - 1);
        } catch (Exception e) {
            System.out.println(e);
            // TODO: handle exception
        }
    }

    public void setRawTokens(ArrayList<String> rawTokens){
        this.rawTokens = rawTokens;
    }
}
