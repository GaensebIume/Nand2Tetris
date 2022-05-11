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
        dictionary.put("static","16");//1st static index TODO: SAFE DELETE
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
            case "eq":
            case "gt":
            case "lt":
                writeToFile("\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nD=M-D\nM=-1\n@" + label + "\nD;" + dictionary.get(command) + "\n@SP\nA=M-1\nM=0\n("+label+")");//TODO: FIND WAY TO TEST
                //LEGACY:writeToFile("\n@SP\nD=M-D\nM=-1\n@" + label + "\nD;" + dictionary.get(command) + "\n@SP\nA=M-1\nM=0\n(" + label + ")");TODO: SAFE DELETE IF WORKING
                updateLabel();
                return;
            case "neg"://(FOLLOWING KNOWN TO BE WORKING)
            case "not":
                writeToFile("\n@SP\nAM=M-1\nM=" + dictionary.get(command) + "\n@SP\nM=M+1");
                return;
            default:
                writeToFile("\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=" + dictionary.get(command));//Handles all other arithmetic operations (i.e. add, sub, and, or)
        }
    }
    //ALL PUSH/POP COMMANDS KNOWN TO BE WORKING
    public void writePushPop(COMMANDTYPE commandtype, String segment, int index){
        if(commandtype != COMMANDTYPE.C_POP && commandtype != COMMANDTYPE.C_PUSH) return;//method must be called with @param commandtype == COMMANDTYPE.C_POP || COMMANDTYPE.C_PUSH
        if(commandtype == COMMANDTYPE.C_PUSH){
            switch (segment) {
                case "pointer":
                    writeToFile("\n@R" + (3 + index) + "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//1st index of pointer @ memory address no 3
                    return;
                case "temp":
                    writeToFile("\n@R" + (5 + index) + "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//1st index of temp @ memory address no 5
                    return;
                case "constant":
                    writeToFile("\n@" + index + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1");//@index to push numeric value of index
                    return;
                case "static":
                    writeToFile("\n@" + (16 + index) + "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");
                    return;
            }                                                                                    //vvvvv
            if(dictionary.containsKey(segment)) writeToFile("\n@" + dictionary.get(segment) + "\nD=M\n@" + index + "\nA=D+A\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1");//segment must have value in dictionary
            else writeToFile("\nnot a push command " + segment);
        } else {//C_POP commands only
            switch (segment) {
                case "pointer":
                    writeToFile("\n@SP\nAM=M-1\nD=M\n@R" + (index + 3) + "\nM=D");//Writes to R3+index, R3 == 1st index of pointer
                    return;
                case "temp":
                    writeToFile("\n@SP\nAM=M-1\nD=M\n@R" + (index + 5) + "\nM=D");//Writes to R5+index, R5 == 1st index of temp
                    return;
                case "static":
                    writeToFile("\n@SP\nAM=M-1\nD=M\n@" + (16 + index) + "\nM=D");
                    return;
                case "constant":
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

    public void setFileName(String fileName){//only used to set content of String label (ATM: single call in constructor)
        this.fileName = fileName.substring(0,fileName.length()-4);//-4 because file ends in .asm
    }

    private void updateLabel(){
        label = "jmpTo" + fileName + jmpIndex;
        jmpIndex++;
    }

    public void writeToFile(String in){
        try {
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