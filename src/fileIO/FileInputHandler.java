package fileIO;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import model.DTree;

public class FileInputHandler {
	
	@SuppressWarnings("unchecked")
	public static ArrayList<DTree> loadModels() {
		// Use the ObjectInputStream class for reading binary data.
		ArrayList<DTree> models = null;
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("data/progress/players.dat");
			ois = new ObjectInputStream(new BufferedInputStream(fis));
			models = (ArrayList<DTree>) ois.readObject();
			System.out.println("Read successful.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return models;
	}
}
