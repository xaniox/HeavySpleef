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
package de.matzefratze123.heavyspleef.config;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.BroadcastType;

public class ConfigUtil {
	
	public static final char   SEPERATOR             = '.';
	public static final String GENERAL_SECTION       = "general"       + SEPERATOR;
	public static final String LEADERBOARD_SECTION   = "leaderboard"   + SEPERATOR;
	public static final String MESSAGES_SECTION      = "messages"      + SEPERATOR;
	public static final String FLAG_DEFAULTS_SECTION = "flag-defaults" + SEPERATOR;
	public static final String QUEUES_SECTION        = "queues"        + SEPERATOR;
	public static final String SCOREBOARDS_SECTION   = "scoreboards"   + SEPERATOR;
	public static final String LANGUAGE_SECTION      = "language"      + SEPERATOR;
	public static final String ANTICAMPING_SECTION   = "anticamping"   + SEPERATOR;
	public static final String EFFECTS_SECTION       = "sounds"        + SEPERATOR;
	public static final String STATISTIC_SECTION     = "statistic"     + SEPERATOR;
	
	
	/**
	 * Gets an broadcast type from the config.
	 * Internal method
	 * 
	 * @param msgType The config message name
	 */
	public static BroadcastType getBroadcast(MessageType msgType) {
		return HeavySpleef.getSystemConfig().getMessagesSection().getBroadcastType(msgType);
	}
	
}
