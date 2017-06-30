package syntacticTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
// Classe de manipulacao de arquivos
public class OutFile {
	public static boolean write(String file, String text){
		try{
		    PrintWriter writer = new PrintWriter(file, "UTF-8");
		    writer.print(text);
		    writer.close();
		    return true;
		} catch (IOException e) {
		  return false;
		}
	}
	
	public static boolean append(String file, String text){
		try{
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		    writer.print(text);
		    writer.close();
		    return true;
		} catch (IOException e) {
		  return false;
		}
	}
}
