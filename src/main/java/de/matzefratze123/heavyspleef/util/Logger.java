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

import java.util.logging.Level;

import de.matzefratze123.heavyspleef.HeavySpleef;

/**
 * Provides an easy class for quick logging
 * 
 * @author matzefratze123
 */
public class Logger {

	public static void info(String msg) {
		HeavySpleef.getInstance().getLogger().log(Level.INFO, msg);
	}

	public static void warning(String msg) {
		HeavySpleef.getInstance().getLogger().log(Level.WARNING, msg);
	}

	public static void severe(String msg) {
		HeavySpleef.getInstance().getLogger().log(Level.SEVERE, msg);
	}

}
