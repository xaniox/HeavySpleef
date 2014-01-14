/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
