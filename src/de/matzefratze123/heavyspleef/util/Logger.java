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
		HeavySpleef.instance.getLogger().log(Level.INFO, msg);
	}
	
	public static void warn(String msg) {
		HeavySpleef.instance.getLogger().log(Level.WARNING, msg);
	}
	
	public static void severe(String msg) {
		HeavySpleef.instance.getLogger().log(Level.SEVERE, msg);
	}
	
}


