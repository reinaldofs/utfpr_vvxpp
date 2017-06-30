package langXrt;

import java.io.*;

import syntacticTree.OutFile;


public class Runtime {
    static BufferedReader in;
    private final static String fileName = "saida.txt";
    
    static public int initialize() {
        System.out.println("Language X runtime system. Version 0.1");
        // reseta o arquivo de log de codigos
        resetFile();
        in = new BufferedReader(new InputStreamReader(System.in));

        if (in == null) {
            System.err.println("Error initializing X language runtime system");

            return -1;
        }

        return 0;
    }

    static public void finilizy() {
        try {
            in.close();
        } catch (IOException e) {
        }
    }

    static public int readInt() {
        String s = null;
        int k;

        try {
            s = in.readLine();
            k = Integer.parseInt(s);
        } catch (IOException e) {
            System.err.println("Error reading from standard input");
            System.err.println("Reason: " + e.getMessage());

            return 0;
        } catch (NumberFormatException f) {
            return 0;
        }

        return k;
    }

    static public String readString() {
        String s = null;

        try {
            s = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading from standard input");
            System.err.println("Reason: " + e.getMessage());
        }

        return s;
    }
    
    
    // escreve em um arquivo
    static public void writeFile(String text){
     OutFile.append(fileName, text);
    }
    
    static public void resetFile(){
	    OutFile.write(fileName, "");
	   }
       
}
