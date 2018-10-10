package fileIO;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;

import model.DTree;

public class FileOutputHandler {
	public static void saveModels(ArrayList<DTree> players) {
		try {
			// Use the ObjectOutputStream class for writing binary data.
			FileOutputStream fos = new FileOutputStream("data/progress/players.dat");
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			oos.writeObject(players);
			oos.flush();
			oos.close();
			fos.close();
			System.out.println("Players saved - " + new Date().toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeDebugCodeToFile(String debugCode, String filePath) {
		File file = new File(filePath);
		PrintWriter out;
		try {
			out = new PrintWriter(file);
			out.print(debugCode);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
