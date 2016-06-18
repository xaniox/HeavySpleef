/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core;

import org.bukkit.Bukkit;

import java.util.logging.Logger;

public class MinecraftVersion {

    public static final Updater.Version V1_6 = Updater.Version.parse("1.6");
    public static final Updater.Version V1_7 = Updater.Version.parse("1.7");
    public static final Updater.Version V1_8 = Updater.Version.parse("1.8");
    public static final Updater.Version V1_9 = Updater.Version.parse("1.9");
    public static final Updater.Version V1_10 = Updater.Version.parse("1.10");

    public static final int V1_6_ID = 0;
    public static final int V1_7_ID = 1;
    public static final int V1_8_ID = 2;
    public static final int V1_9_ID = 3;
    public static final int V1_10_ID = 4;

    public static final int UNKNOWN_VERSION = -1;

	private static Updater.Version implementationVersion;
	private static String implementationVersionString;
	private static boolean spigot;

	private MinecraftVersion() {}
	
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
        implementationVersion = Updater.Version.parse(majorVersion);
		implementationVersionString = majorVersion;
		
		String version = Bukkit.getVersion();
		if (version.toLowerCase().contains("spigot")) {
			spigot = true;
		}
	}
	
	public static Updater.Version getImplementationVersion() {
		return implementationVersion;
	}

    public static Updater.Version getVersionByInt(int versionIdentifier) {
        switch (versionIdentifier) {
            case V1_6_ID:
                return V1_6;
            case V1_7_ID:
                return V1_7;
            case V1_8_ID:
                return V1_8;
            case V1_9_ID:
                return V1_9;
            case V1_10_ID:
                return V1_10;
            default:
                return null;
        }
    }

	public static String getImplementationVersionString() {
		return implementationVersionString;
	}

	public static boolean isSpigot() {
		return spigot;
	}

}