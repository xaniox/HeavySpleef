/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MinecraftVersion {
	
	public static final int V1_6 = 1;
	public static final int V1_7 = 2;
	public static final int V1_8 = 3;
	public static final int UNKNOWN_VERSION = -1;
	
	private static @Getter int implementationVersion = UNKNOWN_VERSION;
	private static @Getter String implementationVersionString;
	
	static void initialize(Logger logger) {
		String bukkitVersion = Bukkit.getBukkitVersion();
		char[] chars = bukkitVersion.toCharArray();
		
		StringBuilder majorVersionBuilder = new StringBuilder();
		boolean hadDot = false;
		
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			
			if (!Character.isDigit(c) && c != '.') {
				break;
			}
			
			if (c == '.') {
				if (hadDot) {
					break;
				} else {
					hadDot = true;
				}
			}
			
			majorVersionBuilder.append(c);
		}
		
		String majorVersion = majorVersionBuilder.toString();
		switch (majorVersion) {
		case "1.6":
			implementationVersion = V1_6;
			break;
		case "1.7":
			implementationVersion = V1_7;
			break;
		case "1.8":
			implementationVersion = V1_8;
			break;
		default:
			logger.log(Level.WARNING, "Unable to determine version of Minecraft implementation! Some flags may cause compatibility problems!");
			break;
		}
		
		implementationVersionString = majorVersion;
	}
	
	public static String getImplementationVersionString(int version) {
		String versionString;
		
		switch (version) {
		case V1_6:
			versionString = "1.6";
			break;
		case V1_7:
			versionString = "1.7";
			break;
		case V1_8:
			versionString = "1.8";
			break;
		default:
			versionString = "Unknown";
			break;
		}
		
		return versionString;
	}

}
