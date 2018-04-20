/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package virtual.machine.ii;

import java.io.File;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Luis Andrino
 */
public class VM2Master {
     boolean finish = false;
    String retorno = "";
    String error = "";
    
 public void Convertir (String ruta){
       if (ruta == ""){

            System.out.println("no ingreso ningun archivo");

        }else {

            String fileInName = ruta;

            File fileIn = new File(fileInName);

            String fileOutPath = "";

            File fileOut;

            CodeWriter writer;

            ArrayList<File> vmFiles = new ArrayList<File>();

            if (fileIn.isFile()) {

                
                String path = fileIn.getAbsolutePath();

                if (!Parser.getExt(path).equals(".vm")) {

                    throw new IllegalArgumentException(" se necesita algun archivo .vm");

                }

                vmFiles.add(fileIn);

                fileOutPath = fileIn.getAbsolutePath().substring(0, fileIn.getAbsolutePath().lastIndexOf(".")) + ".asm";

            } else if (fileIn.isDirectory()) {

                
                vmFiles = getVMFiles(fileIn);

                
                if (vmFiles.size() == 0) {

                    throw new IllegalArgumentException("no hay ningun archivo .vm");

                }

                fileOutPath = fileIn.getAbsolutePath() + "/" +  fileIn.getName() + ".asm";
            }

            fileOut = new File(fileOutPath);
            writer = new CodeWriter(fileOut);

            writer.writeInit();

            for (File f : vmFiles) {

                writer.setFileName(f);

                Parser parser = new Parser(f);

                int type = -1;

                //empieza el parseo
                while (parser.hasMoreCommands()) {

                    parser.advance();

                    type = parser.commandType();

                    if (type == Parser.ARITHMETIC) {

                        writer.writeArithmetic(parser.arg1());

                    } else if (type == Parser.POP || type == Parser.PUSH) {

                        writer.writePushPop(type, parser.arg1(), parser.arg2());

                    } else if (type == Parser.LABEL) {

                        writer.writeLabel(parser.arg1());

                    } else if (type == Parser.GOTO) {

                        writer.writeGoto(parser.arg1());

                    } else if (type == Parser.IF) {

                        writer.writeIf(parser.arg1());

                    } else if (type == Parser.RETURN) {

                        writer.writeReturn();

                    } else if (type == Parser.FUNCTION) {

                        writer.writeFunction(parser.arg1(),parser.arg2());

                    } else if (type == Parser.CALL) {

                        writer.writeCall(parser.arg1(),parser.arg2());

                    }

                }

            }
            writer.close();
            finish = true; 
            retorno = "Archivo Creado : " + fileOutPath;
            System.out.println("Archivo creado : " + fileOutPath);
            
            
     }
    
    
            
            
 }
 public static ArrayList<File> getVMFiles(File dir){

        File[] files = dir.listFiles();

        ArrayList<File> result = new ArrayList<File>();

        for (File f:files){

            if (f.getName().endsWith(".vm")){

                result.add(f);

            }

        }

        return result;

    }
 
 public String termino(){
     if (finish) {
          
         return retorno;
         
     }
     return "";
 }
    
}
