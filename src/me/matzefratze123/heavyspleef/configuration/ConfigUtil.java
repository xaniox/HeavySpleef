package me.matzefratze123.heavyspleef.configuration;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.BroadcastType;

public class ConfigUtil {
	
	/**
	 * Gets an broadcast type from the config.
	 * Internal method
	 * 
	 * @param name The config message name
	 */
	public static BroadcastType getBroadcast(String name) {
		String str = HeavySpleef.getSystemConfig().getString("messages." + name);
		if (str == null)
			return BroadcastType.RADIUS;
		
		BroadcastType type = BroadcastType.getBroadcastType(str);
		if (type == null)
			return BroadcastType.RADIUS;
		
		return type;
	}
	
}
