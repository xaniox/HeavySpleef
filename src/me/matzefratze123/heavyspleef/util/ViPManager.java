/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
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
package me.matzefratze123.heavyspleef.util;

import java.util.List;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.ChatColor;

public class ViPManager {

	//Just a list of unchangeable ViP's :P
	public static String[] vips = new String[] {"matzefratze123", "x3Fawkesx3", "HeavyRain900", "pascalvdl", "timscoLP", "benfire1"};
	
	public static void initVips() {
		List<String> serverVips = HeavySpleef.getSystemConfig().getStringList("general.vip");
		
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
