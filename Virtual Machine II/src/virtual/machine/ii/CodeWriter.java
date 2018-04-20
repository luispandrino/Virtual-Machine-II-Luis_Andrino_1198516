/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtual.machine.ii;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author Luis Andrino 
 */
public class CodeWriter {
     
 
    private int JumpFlag;
    private PrintWriter outPrinter;
    private static final Pattern labelReg = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");
    private static int labelCnt = 0;
    private static String fileName = "";


    public CodeWriter(File fileOut) {

        try {

            fileName = fileOut.getName();
            outPrinter = new PrintWriter(fileOut);
            JumpFlag = 0;

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        }

    }

 
    public void setFileName(File fileOut){

        fileName = fileOut.getName();

    }


    public void writeArithmetic(String command){

        if (command.equals("add")){

            outPrinter.print(arithmeticTemplate1() + "M=M+D\n");

        }else if (command.equals("sub")){

            outPrinter.print(arithmeticTemplate1() + "M=M-D\n");

        }else if (command.equals("and")){

            outPrinter.print(arithmeticTemplate1() + "M=M&D\n");

        }else if (command.equals("or")){

            outPrinter.print(arithmeticTemplate1() + "M=M|D\n");

        }else if (command.equals("gt")){

            outPrinter.print(arithmeticTemplate2("JLE"));//not <=
            JumpFlag++;

        }else if (command.equals("lt")){

            outPrinter.print(arithmeticTemplate2("JGE"));//not >=
            JumpFlag++;

        }else if (command.equals("eq")){

            outPrinter.print(arithmeticTemplate2("JNE"));//not <>
            JumpFlag++;

        }else if (command.equals("not")){

            outPrinter.print("@SP\nA=M-1\nM=!M\n");

        }else if (command.equals("neg")){

            outPrinter.print("D=0\n@SP\nA=M-1\nM=D-M\n");

        }else {

            throw new IllegalArgumentException("error");

        }

    }

    /**
     * Write the assembly code that is the translation of the given command
     * where the command is either PUSH or POP
     * @param command PUSH or POP
     * @param segment
     * @param index
     */
    public void writePushPop(int command, String segment, int index){

        if (command == Parser.PUSH){

            if (segment.equals("constant")){

                outPrinter.print("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

            }else if (segment.equals("local")){

                outPrinter.print(pushTemplate1("LCL",index,false));

            }else if (segment.equals("argument")){

                outPrinter.print(pushTemplate1("ARG",index,false));

            }else if (segment.equals("this")){

                outPrinter.print(pushTemplate1("THIS",index,false));

            }else if (segment.equals("that")){

                outPrinter.print(pushTemplate1("THAT",index,false));

            }else if (segment.equals("temp")){

                outPrinter.print(pushTemplate1("R5", index + 5,false));

            }else if (segment.equals("pointer") && index == 0){

                outPrinter.print(pushTemplate1("THIS",index,true));

            }else if (segment.equals("pointer") && index == 1){

                outPrinter.print(pushTemplate1("THAT",index,true));

            }else if (segment.equals("static")){
                outPrinter.print("@" + fileName + index + "\n" + "D=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

            }

        }else if(command == Parser.POP){

            if (segment.equals("local")){

                outPrinter.print(popTemplate1("LCL",index,false));

            }else if (segment.equals("argument")){

                outPrinter.print(popTemplate1("ARG",index,false));

            }else if (segment.equals("this")){

                outPrinter.print(popTemplate1("THIS",index,false));

            }else if (segment.equals("that")){

                outPrinter.print(popTemplate1("THAT",index,false));

            }else if (segment.equals("temp")){

                outPrinter.print(popTemplate1("R5", index + 5,false));

            }else if (segment.equals("pointer") && index == 0){

                outPrinter.print(popTemplate1("THIS",index,true));

            }else if (segment.equals("pointer") && index == 1){

                outPrinter.print(popTemplate1("THAT",index,true));

            }else if (segment.equals("static")){
                
                outPrinter.print("@" + fileName + index + "\nD=A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n");

            }

        }else {

            throw new IllegalArgumentException("error");

        }

    }

 
    public void writeLabel(String label){

        Matcher m = labelReg.matcher(label);

        if (m.find()){

            outPrinter.print("(" + label +")\n");

        }else {

            throw new IllegalArgumentException("error");

        }

    }

    
    public void writeGoto(String label){

        Matcher m = labelReg.matcher(label);

        if (m.find()){

            outPrinter.print("@" + label +"\n0;JMP\n");

        }else {

            throw new IllegalArgumentException("error");

        }

    }

  
    public void writeIf(String label){

        Matcher m = labelReg.matcher(label);

        if (m.find()){

            outPrinter.print(arithmeticTemplate1() + "@" + label +"\nD;JNE\n");

        }else {

            throw new IllegalArgumentException("error");

        }

    }


    public void writeInit(){

        outPrinter.print("@256\n" +
                         "D=A\n" +
                         "@SP\n" +
                         "M=D\n");
        writeCall("Sys.init",0);

    }

   
    public void writeCall(String functionName, int numArgs){

        String newLabel = "RETURN_LABEL" + (labelCnt++);

        outPrinter.print("@" + newLabel + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        outPrinter.print(pushTemplate1("LCL",0,true));//push LCL
        outPrinter.print(pushTemplate1("ARG",0,true));//push ARG
        outPrinter.print(pushTemplate1("THIS",0,true));//push THIS
        outPrinter.print(pushTemplate1("THAT",0,true));//push THAT

        outPrinter.print("@SP\n" +
                        "D=M\n" +
                        "@5\n" +
                        "D=D-A\n" +
                        "@" + numArgs + "\n" +
                        "D=D-A\n" +
                        "@ARG\n" +
                        "M=D\n" +
                        "@SP\n" +
                        "D=M\n" +
                        "@LCL\n" +
                        "M=D\n" +
                        "@" + functionName + "\n" +
                        "0;JMP\n" +
                        "(" + newLabel + ")\n"
                        );

    }

 
    public void writeReturn(){

        outPrinter.print(returnTemplate());

    }


    public void writeFunction(String functionName, int numLocals){

        outPrinter.print("(" + functionName +")\n");

        for (int i = 0; i < numLocals; i++){

            writePushPop(Parser.PUSH,"constant",0);

        }

    }

  
    public String preFrameTemplate(String position){

        return "@R11\n" +
                "D=M-1\n" +
                "AM=D\n" +
                "D=M\n" +
                "@" + position + "\n" +
                "M=D\n";

    }


    public String returnTemplate(){

        return "@LCL\n" +
                "D=M\n" +
                "@R11\n" +
                "M=D\n" +
                "@5\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@R12\n" +
                "M=D\n" +
                popTemplate1("ARG",0,false) +
                "@ARG\n" +
                "D=M\n" +
                "@SP\n" +
                "M=D+1\n" +
                preFrameTemplate("THAT") +
                preFrameTemplate("THIS") +
                preFrameTemplate("ARG") +
                preFrameTemplate("LCL") +
                "@R12\n" +
                "A=M\n" +
                "0;JMP\n";
    }


    public void close(){

        outPrinter.close();

    }

 
    private String arithmeticTemplate1(){

        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n";

    }


    private String arithmeticTemplate2(String type){

        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                "D=M-D\n" +
                "@FALSE" + JumpFlag + "\n" +
                "D;" + type + "\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=-1\n" +
                "@CONTINUE" + JumpFlag + "\n" +
                "0;JMP\n" +
                "(FALSE" + JumpFlag + ")\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=0\n" +
                "(CONTINUE" + JumpFlag + ")\n";

    }


  
    private String pushTemplate1(String segment, int index, boolean isDirect){

        //When it is a pointer, just read the data stored in THIS or THAT
        String noPointerCode = (isDirect)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";

        return "@" + segment + "\n" +
                "D=M\n"+
                noPointerCode +
                "@SP\n" +
                "A=M\n" +
                "M=D\n" +
                "@SP\n" +
                "M=M+1\n";

    }

  
    private String popTemplate1(String segment, int index, boolean isDirect){

        
        String noPointerCode = (isDirect)? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";

        return "@" + segment + "\n" +
                noPointerCode +
                "@R13\n" +
                "M=D\n" +
                "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "@R13\n" +
                "A=M\n" +
                "M=D\n";

    }
}
