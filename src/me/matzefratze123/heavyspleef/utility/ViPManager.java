package me.matzefratze123.heavyspleef.utility;

import java.util.List;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.ChatColor;

public class ViPManager {

	//Just a list of unchangeable ViP's :P
	public static String[] vips = new String[] {"matzefratze123", "x3Fawkesx3", "HeavyRain900", "pascalvdl", "timscoLP"};
	
	public static void initVips() {
		List<String> serverVips = HeavySpleef.instance.getConfig().getStringList("general.vip");
		
		List<String> allVips = ArrayHelper.mergeArrays(serverVips.toArray(new String[serverVips.size()]), vips);
		vips = allVips.toArray(new String[allVips.size()]);
	}
	
	public static String colorName(String str) {
		for (String vip : vips) {
			if (vip.equalsIgnoreCase(str)) {
				return ChatColor.DARK_RED + str;
			}
		}
		
		return str;
	}
	
}
