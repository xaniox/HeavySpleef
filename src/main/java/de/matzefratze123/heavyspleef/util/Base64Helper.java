package de.matzefratze123.heavyspleef.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class Base64Helper {
	
	public static Object fromBase64(String s) {
		byte[] data = Base64Coder.decode(s);
		
		ObjectInputStream ois;
		Object o = null;
		
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			o = ois.readObject();
			ois.close();
		} catch (final IOException e) {
			Logger.severe("Could not read from base64 string: " + e.getMessage());
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			Logger.severe("Could not recognize base64 class: " + e.getMessage());
			e.printStackTrace();
		}
		
		return o;
	}

	public static String toBase64(Serializable o) {
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			return new String(Base64Coder.encode(baos.toByteArray()));
		} catch (IOException e) {
			Logger.severe("Could not write base64 string: " + e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}

}
