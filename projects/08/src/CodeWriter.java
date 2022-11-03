import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class CodeWriter {
    private BufferedWriter bufferedWriter;
    private String fileName;
    private int jmpIndex = 0;
    private String label;
    private final HashMap<String, String> dictionary;
    
    public CodeWriter(File outputFile) {
        dictionary = new HashMap<>();
        setUpDict();
        updateLabel();
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
            setFileName(outputFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpDict() {
        dictionary.put("add","D+M");
        dictionary.put("sub","M-D");//DO NOT CHANGE!!!
        dictionary.put("and","D&M");
        dictionary.put("or","D|M");
        dictionary.put("neg","-M");
        dictionary.put("not","!M");
        dictionary.put("eq","JEQ");
        dictionary.put("gt","JGT");
        dictionary.put("lt","JLT");
        dictionary.put("static","16");//1st static index; possibly unnecessary
        dictionary.put("this","THIS");
        dictionary.put("that", "THAT");
        dictionary.put("local", "LCL");
        dictionary.put("argument", "ARG");
    }

    public void writeArithmetic(String command) {
        if(!dictionary.containsKey(command)){
            writeToFile("\nnot a arithmetic command " + command);
            return;//GUARD CLAUSE, method cannot handle unknown commands
        }
        switch (command) {
            case "eq": {
                writeToFile("\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nD=M-D\nM=-1\n@" + label + "\nD;" + dictionary.get(command) + "\n@SP\nA=M-1\nM=0\n(" + label + ")");
                updateLabel();
            }
            case "gt": {
                writeToFile("\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nD=M-D\nM=-1\n@" + label + "\nD;" + dictionary.get(command) + "\n@SP\nA=M-1\nM=0\n(" + label + ")");
                updateLabel();
            }
            case "lt": {
                writeToFile("\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nD=M-D\nM=-1\n@" + label + "\nD;" + dictionary.get(command) + "\n@SP\nA=M-1\nM=0\n(" + label + ")");
                updateLabel();
            }//(FOLLOWING KNOWN TO BE WORKING)
            case "not": {
                writeToFile("\n@SP\nAM=M-1\nM=" + dictionary.get(command) + "\n@SP\nM=M+1");
            }
            case "neg": {
                writeToFile("\n@SP\nAM=M-1\nM=" + dictionary.get(command) + "\n@SP\nM=M+1");
            }
            default:{
                writeToFile("\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=" + dictionary.get(command));//Handles all other arithmetic operations (i.e. add, sub, and, or)
            }
        }
    }

    //ALL PUSH/POP COMMANDS KNOWN TO BE WORKING
    public void writePushPop(COMMANDTYPE commandtype, String segment, int index){
        if(commandtype != COMMANDTYPE.C_POP && commandtype != COMMANDTYPE.C_PUSH) return;//method must be called with @param commandtype == COMMANDTYPE.C_POP || COMMANDTYPE.C_PUSH
        if(commandtype == COMMANDTYPE.C_PUSH){
            if ("pointer".equals(segment)) {
                writeToFile("\n@R" + (3 + index) + "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//1st index of pointer @ memory address no 3
                return;
            } else if ("temp".equals(segment)) {
                writeToFile("\n@R" + (5 + index) + "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//1st index of temp @ memory address no 5
                return;
            } else if ("constant".equals(segment)) {
                writeToFile("\n@" + index + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1");//@index to push numeric value of index
                return;
            } else if ("static".equals(segment)) {
                writeToFile("\n@" + (16 + index) + "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");
                return;
            }
            if(dictionary.containsKey(segment)) writeToFile("\n@" + dictionary.get(segment) + "\nD=M\n@" + index + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//segment must have value in dictionary
            else writeToFile("\nnot a push command " + segment);
        } else {//C_POP commands only
            if ("pointer".equals(segment)) {
                writeToFile("\n@SP\nAM=M-1\nD=M\n@R" + (index + 3) + "\nM=D");//Writes to R3+index, R3 == 1st index of pointer
                return;
            } else if ("temp".equals(segment)) {
                writeToFile("\n@SP\nAM=M-1\nD=M\n@R" + (index + 5) + "\nM=D");//Writes to R5+index, R5 == 1st index of temp
                return;
            } else if ("static".equals(segment)) {
                writeToFile("\n@SP\nAM=M-1\nD=M\n@" + (16 + index) + "\nM=D");
                return;
            } else if ("constant".equals(segment)) {
                throw new IllegalArgumentException("cannot pop a constant");
            }
            if(dictionary.containsKey(segment)) {
                /*if(index > 1) */       writeToFile("\n@" + dictionary.get(segment) + "\nD=M\n@" + index + "\nD=D+A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D");//writes to field specified by content of segment + index; R13 == one of the fields to store data within one command (here: which field to pop to), can also be R14..R16
                //else if (index == 1) writeToFile("\n@SP\nAM=M-1\nD=M\n@" + dictionary.get(segment) + "\nA=A+1\nM=D");//index ==1 hence A=M+1
                //else writeToFile("\n@SP\nAM=M-1\nD=M\n@" + dictionary.get(segment) + "\nM=D");//index <=0, hence no addition to A-Register
            }
            else writeToFile("\nnot a pop command " + segment);
        }
    }

    public void writeInit(){//TODO: USE CASE?

    }

    public void writeLabel(String label){
        writeToFile("\n@" + label);
    }

    public void writeGoTo(String label){
        writeToFile("(" + label + ")\n0;JMP");
    }

    public void writeIf(String label){
        writeToFile("\n@SP\nAMD=M-1\n@" + label + "D;JNE");//pop top of stack AND check condition
    }
/*
*  M = MEMORY
*  D = STORED VALUE
*  A = INDEX
*/
    public void writeCall(String label, int numArgs){
        writeLabel(label);//push return
        writeToFile("\n@LCL\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//push LCL
        writeToFile("\n@ARG\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//push ARG
        writeToFile("\n@THIS\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//push THIS
        writeToFile("\n@THAT\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//push THAT
        writeToFile("\n@" + (numArgs + 5) + "\nA=M\n@SP\nD=A\nA=M-D");//ARG=@SP-numArgs-5
        writeToFile("\n@SP\nA=M\n@LCL\nD=A");//LCL=SP
        writeGoTo(label);//goto f -> label
    }

    public void writeReturn(){
        writeToFile("\n@LCL\nD=M\n@R5\nM=D");//temp var -> R5 is chosen; R5 = FRAME
        writeToFile("\n@R5\nD=M\n@5\nD=D-A\n@R6\nM=D");//store ret in temp var; ret = R6 -> if not working: "\n@R5\nD=M\n@5\nA=D-A\nD=M\n@R6\nM=D"
        writeToFile("\n@SP\nADM=M-1\n@ARG\nA=M\nM=D");//reposition return value -> *ARG = pop() ???
        writeToFile("\n@ARG\nD=M+1\n@SP\nM=D");//restore SP
        writeToFile("\n@R5\nAMD=M-1\n@THAT\nM=D");//restore THAT
        writeToFile("\n@R5\nAMD=M-1\n@THIS\nM=D");//restore THIS
        writeToFile("\n@R5\nAMD=M-1\n@ARG\nM=D");//restore ARG
        writeToFile("\n@R5\nAMD=M-1\n@LCL\nM=D");//restore LCL
        writeToFile("\n@R6\nA=M\n0;JMP");//goto return address
    }

    public void writeFunction(String functionName, int numLocals){
        writeLabel(functionName);
        for (int i = 0; i < numLocals; i++)
            writePushPop(COMMANDTYPE.C_PUSH, "constant", 0);
    }

    public void setFileName(String fileName){//only used to set content of String label (ATM: single call in constructor)
        this.fileName = fileName.substring(0,fileName.length()-4);//-4 because file ends in .asm
    }

    private void updateLabel(){
        label = "jmpTo" + fileName + jmpIndex;
        jmpIndex++;
    }

    public void writeToFile(String in){
        try {
            //System.out.println(in);
            bufferedWriter.write(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}