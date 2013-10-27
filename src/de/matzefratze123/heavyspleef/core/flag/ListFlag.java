package de.matzefratze123.heavyspleef.core.flag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.entity.Player;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class ListFlag<T> extends Flag<List<T>> {

	private static final String ELEMENT_SEPERATOR = ";";

	public ListFlag(String name, List<T> defaulte) {
		super(name, defaulte);
	}

	/**
	 * Serializes the current list </br></br> Please note that your list
	 * contents have to implement the serializeable interface
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String serialize(Object object) {
		List<T> list = (List<T>) object;

		StringBuilder builder = new StringBuilder();
		Iterator<T> iterator = list.iterator();
		
		while (iterator.hasNext()) {
			T element = iterator.next();
			
			Serializable s = (Serializable) element;
			String base64String = toBase64(s);
			
			builder.append(base64String);
			
			if (iterator.hasNext()) {
				builder.append(ELEMENT_SEPERATOR);
			}
		}
		
		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> deserialize(String str) {
		String[] elements = str.split(ELEMENT_SEPERATOR);
		
		List<T> list = new ArrayList<T>();
		
		for (String e : elements) {
			Object fromBase64 = fromBase64(e);
			
			T element = (T) fromBase64;
			list.add(element);
		}
		
		return list;
	}

	@Override
	public List<T> parse(Player player, String input) {
		// TODO Automatisch generierter Methodenstub
		return null;
	}

	@Override
	public String toInfo(Object value) {
		// TODO Automatisch generierter Methodenstub
		return null;
	}

	@Override
	public String getHelp() {
		// TODO Automatisch generierter Methodenstub
		return null;
	}

	@Override
	public FlagType getType() {
		return FlagType.LISTFLAG;
	}

	/** Read the object from Base64 string. */
	private Object fromBase64(String s) {
		byte[] data = Base64Coder.decode(s);
		
		ObjectInputStream ois;
		Object o = null;
		
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data));
			o = ois.readObject();
			ois.close();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return o;
	}

	/** Write the object to a Base64 string. */
	private String toBase64(Serializable o) {
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			return new String(Base64Coder.encode(baos.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
