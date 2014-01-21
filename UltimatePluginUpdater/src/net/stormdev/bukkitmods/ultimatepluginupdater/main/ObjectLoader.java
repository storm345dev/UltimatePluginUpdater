package net.stormdev.bukkitmods.ultimatepluginupdater.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectLoader {
	public static void save(Object object, File file){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(object);
			oos.flush();
			oos.close();
		}catch (Exception e) {
		}
		return;
	}
	public static void nullify(File file){
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(null);
			oos.flush();
			oos.close();
		}catch (Exception e) {
		}
		return;
	}
	public static Object load(File file){
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			Object result = ois.readObject();
			ois.close();
			return result;
		} catch (Exception e) {
		}
		return null;
	}
}
