package de.matzefratze123.heavyspleef.core.i18n;

import java.util.Enumeration;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.bukkit.configuration.file.YamlConfiguration;

public class YMLResourceBundle extends ResourceBundle {
	
	private YamlConfiguration config;
	
	public YMLResourceBundle(YamlConfiguration config) {
		this.config = config;
	}
	
	@Override
	protected Object handleGetObject(String key) {
		Object value = config.get(key);
		
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			int size = list.size();
			
			String[] array = new String[size];
			
			for (int i = 0; i < size; i++) {
				array[i] = list.get(i).toString();
			}
			
			value = array;
		}
		
		return value;
	}

	@Override
	public Enumeration<String> getKeys() {
		return new YamlKeyEnumeration();
	}
	
	private class YamlKeyEnumeration implements Enumeration<String> {
		
		private Iterator<String> keyIterator;
		
		public YamlKeyEnumeration() {
			keyIterator = config.getKeys(false).iterator();
		}
		
		@Override
		public boolean hasMoreElements() {
			return keyIterator.hasNext();
		}

		@Override
		public String nextElement() {
			return keyIterator.next();
		}
		
	}

}
