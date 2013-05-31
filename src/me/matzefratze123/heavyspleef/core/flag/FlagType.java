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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import me.matzefratze123.heavyspleef.core.flag.enums.Difficulty;

import org.bukkit.inventory.ItemStack;

public enum FlagType {

	INTEGER_FLAG(0),
	LOCATION_FLAG(1),
	BOOLEAN_FLAG(2),
	ITEMSTACK_FLAG(3),
	ENUM_FLAG(4);
	
	private int internalId;
	
	private FlagType(int internalId) {
		this.internalId = internalId;
	}
	
	public int getInternalId() {
		return this.internalId;
	}
	
	public static final LocationFlag WIN = new LocationFlag("win");
	public static final LocationFlag LOSE = new LocationFlag("lose");
	public static final LocationFlag LOBBY = new LocationFlag("lobby");
	public static final LocationFlag QUEUELOBBY = new LocationFlag("queuelobby");
	public static final LocationFlag SPAWNPOINT1 = new LocationFlag("spawnpoint1");
	public static final LocationFlag SPAWNPOINT2 = new LocationFlag("spawnpoint2");
	public static final LocationFlag SPECTATE = new LocationFlag("spectate");
	
	public static final IntegerFlag MINPLAYERS = new IntegerFlag("minplayers", 2);
	public static final IntegerFlag MAXPLAYERS = new IntegerFlag("maxplayers", 0);
	public static final IntegerFlag AUTOSTART = new IntegerFlag("autostart", 0);
	public static final IntegerFlag COUNTDOWN = new IntegerFlag("countdown", 10);
	public static final IntegerFlag JACKPOTAMOUNT = new IntegerFlag("jackpotamount", 0);
	public static final IntegerFlag REWARD = new IntegerFlag("reward", 0);
	public static final IntegerFlag CHANCES = new IntegerFlag("chances", 0);
	public static final IntegerFlag TIMEOUT = new IntegerFlag("timeout", 0);
	public static final IntegerFlag ROUNDS = new IntegerFlag("rounds", 3);
	
	public static final BooleanFlag ONEVSONE = new BooleanFlag("1vs1", false);
	public static final BooleanFlag SHOVELS = new BooleanFlag("shovels", false);
	public static final BooleanFlag SHEARS = new BooleanFlag("shears", false);
	public static final BooleanFlag TEAM = new BooleanFlag("team", false);
	
	public static final ItemStackFlag ITEMREWARD = new ItemStackFlag("itemreward", new ItemStack[]{});
	public static final ItemStackFlag LOSEREWARD = new ItemStackFlag("losereward", new ItemStack[]{});
	
	public static final EnumFlag<Difficulty> DIFFICULTY = new EnumFlag<Difficulty> ("difficulty", Difficulty.class, Difficulty.MEDIUM);
	
	public static List<Flag<?>> getFlagList() {
		List<Flag<?>> flags = new ArrayList<Flag<?>>();
		
		try {
			//Using java reflection here
			Field[] fields = FlagType.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				if (!Modifier.isStatic(field.getModifiers()))
					continue;
				Object value = field.get(null);
				if (!(value instanceof Flag<?>))
					continue;
				Flag<?> flag = (Flag<?>)value;
				flags.add(flag);
			}
		
		} catch (IllegalAccessException e) {}
		
		return flags;
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
