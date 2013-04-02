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
package me.matzefratze123.heavyspleef.core.flag;

import java.util.Arrays;
import java.util.List;

public class FlagType {

	public static final LocationFlag WIN = new LocationFlag("win");
	public static final LocationFlag LOSE = new LocationFlag("lose");
	public static final LocationFlag LOBBY = new LocationFlag("lobby");
	public static final LocationFlag QUEUELOBBY = new LocationFlag("queuelobby");
	public static final LocationFlag SPAWNPOINT1 = new LocationFlag("spawnpoint1");
	public static final LocationFlag SPAWNPOINT2 = new LocationFlag("spawnpoint2");
	
	public static final IntegerFlag MINPLAYERS = new IntegerFlag("minplayers");
	public static final IntegerFlag MAXPLAYERS = new IntegerFlag("maxplayers");
	public static final IntegerFlag AUTOSTART = new IntegerFlag("autostart");
	public static final IntegerFlag COUNTDOWN = new IntegerFlag("countdown");
	public static final IntegerFlag JACKPOTAMOUNT = new IntegerFlag("jackpotamount");
	public static final IntegerFlag REWARD = new IntegerFlag("reward");
	public static final IntegerFlag CHANCES = new IntegerFlag("chances");
	public static final IntegerFlag TIMEOUT = new IntegerFlag("timeout");
	public static final IntegerFlag ROUNDS = new IntegerFlag("rounds");
	
	public static final BooleanFlag ONEVSONE = new BooleanFlag("1vs1");
	public static final BooleanFlag SHOVELS = new BooleanFlag("shovels");
	
	public static final ItemStackFlag ITEMREWARD = new ItemStackFlag("itemreward");
	public static final ItemStackFlag LOSEREWARD = new ItemStackFlag("losereward");
	
	public static final Flag<?>[] flagList = new Flag<?>[] {WIN, LOSE, LOBBY, QUEUELOBBY, SPAWNPOINT1, SPAWNPOINT2,
															MINPLAYERS, MAXPLAYERS, AUTOSTART, COUNTDOWN, JACKPOTAMOUNT,
															REWARD, CHANCES, TIMEOUT, ROUNDS, ONEVSONE, SHOVELS, ITEMREWARD,
															LOSEREWARD};
	
	public static List<Flag<?>> getFlagList() {
		return Arrays.asList(flagList);
	}
	
	public static Flag<?> byName(String name) {
		for (Flag<?> flag : getFlagList()) {
			if (flag.getName().equalsIgnoreCase(name))
				return flag;
		}
		
		return null;
	}
	
	public static Flag<?> byDatabaseName(String dbString) {
		String[] parts = dbString.split(":");
		return byName(parts[0]);
	}
}
