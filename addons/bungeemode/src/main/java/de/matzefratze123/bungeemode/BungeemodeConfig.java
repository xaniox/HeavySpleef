/*
 * This file is part of addons.
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
package de.matzefratze123.bungeemode;

import org.bukkit.configuration.ConfigurationSection;

public class BungeemodeConfig {
	
	public static final int CURRENT_CONFIG_VERSION = 1;
	
	private boolean enabled;
	private String game;
	private boolean spectateWhenIngame;
	private SendBackCriteria sendBackOn;
	private String sendBackTo;
	private boolean restart;
	private int restartCountdown;
	private int configVersion;
	
	public BungeemodeConfig(ConfigurationSection config) {
		//Assign values
		reload(config);
	}
	
	public void reload(ConfigurationSection config) {
		this.enabled = config.getBoolean("enabled", false);
		this.game = config.getString("game");
		this.spectateWhenIngame = config.getBoolean("spectate-when-ingame", true);
		String sendBackOnStr = config.getString("send-back-on");
		this.sendBackOn = SendBackCriteria.getCriteria(sendBackOnStr, SendBackCriteria.FINISH);
		this.sendBackTo = config.getString("send-back-to", "lobby");
		this.restart = config.getBoolean("restart", true);
		this.restartCountdown = config.getInt("restart-countdown", 15);
		this.configVersion = config.getInt("config-version");
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public String getGame() {
		return game;
	}
	
	public boolean getSpectateWhenIngame() {
		return spectateWhenIngame;
	}

	public SendBackCriteria getSendBackOn() {
		return sendBackOn;
	}

	public String getSendBackTo() {
		return sendBackTo;
	}
	
	public boolean isRestart() {
		return restart;
	}
	
	public int getRestartCountdown() {
		return restartCountdown;
	}

	public int getConfigVersion() {
		return configVersion;
	}

	public enum SendBackCriteria {
		
		FINISH,
		LOSE;
		
		public static SendBackCriteria getCriteria(String name, SendBackCriteria defaulte) {
			for (SendBackCriteria val : values()) {
				if (val.name().equalsIgnoreCase(name)) {
					return val;
				}
			}
			
			return defaulte;
		}
		
	}
	
}
