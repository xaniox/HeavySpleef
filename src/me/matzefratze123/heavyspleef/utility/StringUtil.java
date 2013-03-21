package me.matzefratze123.heavyspleef.utility;

import org.bukkit.ChatColor;

public class StringUtil {

	//Just a list of spleef ViP's
	public static String[] vips = new String[] {"matzefratze123", "x3Fawkesx3", "HeavyRain900", "pascalvdl"};
	
	public static String colorName(String str) {
		for (String vip : vips) {
			if (vip.equalsIgnoreCase(str)) {
				return ChatColor.DARK_RED + str;
			}
		}
		
		return str;
	}
	
}
