package me.matzefratze123.heavyspleef.utility;

public class MaterialNameHelper {

	public static String getName(String str) {
		str = str.toLowerCase();
		
		String[] parts = str.split("_");
		String realName = "";
		
		for (String part : parts) {
			char[] chars = part.toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			
			part = String.copyValueOf(chars);
			realName += part + " ";
		}
		
		return realName;
	}

}
