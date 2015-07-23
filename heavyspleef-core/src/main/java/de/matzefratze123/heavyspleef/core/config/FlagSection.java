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
package de.matzefratze123.heavyspleef.core.config;

import lombok.Getter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

import de.matzefratze123.heavyspleef.core.MaterialDataMatcher;

@Getter
public class FlagSection {
	
	private int autostartVote;
	private int anticampingWarn;
	private boolean anticampingDoWarn;
	private int anticampingTeleport;
	private MaterialData readyBlock;
	private MaterialData leaveItem;
	
	public FlagSection(ConfigurationSection section) {
		this.autostartVote = section.getInt("autostart-vote", 75);
		this.anticampingWarn = section.getInt("anticamping-warn", 3);
		this.anticampingDoWarn = section.getBoolean("anticamping-do-warn", true);
		this.anticampingTeleport = section.getInt("anticamping-teleport", 6);
		
		String readyBlockStr = section.getString("ready-block", "IRON_BLOCK");
		MaterialDataMatcher matcher = MaterialDataMatcher.newMatcher(readyBlockStr);
		matcher.match();
		
		readyBlock = matcher.result();
		
		String leaveItemStr = section.getString("leave-item", "MAGMA_CREAM");
		matcher = MaterialDataMatcher.newMatcher(leaveItemStr);
		matcher.match();
		
		leaveItem = matcher.result();
	}

}
