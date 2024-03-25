import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class InstructionWriter {
    public void writeInstructions(ArrayList<String> instructions, File file){
        try {
            int indent = 0;
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (String instruction:instructions) {
                if(instruction.startsWith("</")) indent--; //end of terminal
                bw.write("   ".repeat(indent)+ instruction + "\n");
                if (!instruction.contains("/"))indent++; //begin of terminal
            }
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
