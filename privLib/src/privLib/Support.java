package privLib;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Support {
	
	//Our own print mechanism
	public void print(String s) {
		System.out.println(s);
	}
	
	// Read file
	public static String readFile(String filepath) {
		
		String text = "";
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(
					filepath));
			String line = reader.readLine();
			while (line != null) {
				//System.out.println(line);
				// read next line
				text += line;
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return text;
	}

}
