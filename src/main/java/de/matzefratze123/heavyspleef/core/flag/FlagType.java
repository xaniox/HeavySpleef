/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.core.flag;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import de.matzefratze123.heavyspleef.core.flag.ListFlagItemstack.SerializeableItemStack;
import de.matzefratze123.heavyspleef.core.flag.ListFlagLocation.SerializeableLocation;

public enum FlagType {

	INTEGER_FLAG,
	LOCATION_FLAG,
	BOOLEAN_FLAG,
	ARRAY_ITEMSTACK_FLAG,
	ENUM_FLAG,
	SINGLE_ITEMSTACK_FLAG,
	LISTFLAG;
	
	public static Set<Flag<?>> customFlags = new HashSet<Flag<?>>();
	
	/* Boolean flags */
	@FlagData
	public static final BooleanFlag ONEVSONE = new BooleanFlag("1vs1", false);
	@FlagData(aliases = {"shovel"})
	public static final BooleanFlag SHOVELS = new BooleanFlag("shovels", false);
	@FlagData
	public static final BooleanFlag SHEARS = new BooleanFlag("shears", false);
	@FlagData(aliases = {"teamgame"})
	public static final BooleanFlag TEAM = new BooleanFlag("team", false);
	@FlagData
	public static final BooleanFlag BOWSPLEEF = new BooleanFlag("bowspleef", false);
	@FlagData
	public static final BooleanFlag SPLEGG = new BooleanFlag("splegg", false);
	@FlagData
	public static final BooleanFlag BOXES = new BooleanFlag("boxes", true);
	@FlagData
	public static final BooleanFlag CAMP_DETECTION = new BooleanFlag("anticamping", true);
	@FlagData
	public static final BooleanFlag BLOCKBREAKEFFECT = new BooleanFlag("blockbreakeffect", false);
	
	/* Location flags */
	@FlagData(aliases = {"winpoint"})
	public static final LocationFlag WIN = new LocationFlag("win");
	@FlagData(aliases = {"losepoint"})
	public static final LocationFlag LOSE = new LocationFlag("lose");
	@FlagData(aliases = {"lobbypoint"})
	public static final LocationFlag LOBBY = new LocationFlag("lobby");
	@FlagData
	public static final LocationFlag QUEUELOBBY = new LocationFlag("queuelobby");
	@FlagData(aliases = {"spectatepoint"})
	public static final LocationFlag SPECTATE = new LocationFlag("spectate");
	@FlagData(aliases = {"spawn"})
	public static final LocationFlag SPAWNPOINT = new LocationFlag("spawnpoint");
	@FlagData
	public static final ListFlag<SerializeableLocation> NEXTSPAWNPOINT = new ListFlagLocation("nextspawnpoint", null);
	@FlagData
	public static final ListFlag<SerializeableItemStack> ITEMREWARD = new ListFlagItemstack("itemreward", null);
	
	/* Integer flags */
	@FlagData(aliases = {"min"})
	public static final IntegerFlag MINPLAYERS = new IntegerFlag("minplayers", 2);
	@FlagData(aliases = {"max"})
	public static final IntegerFlag MAXPLAYERS = new IntegerFlag("maxplayers", 0);
	@FlagData
	public static final IntegerFlag AUTOSTART = new IntegerFlag("autostart", 0);
	@FlagData
	public static final IntegerFlag COUNTDOWN = new IntegerFlag("countdown", 10);
	@FlagData
	public static final IntegerFlag ENTRY_FEE = new IntegerFlag("entryfee", 0);
	@FlagData
	public static final IntegerFlag REWARD = new IntegerFlag("reward", 0);
	@FlagData
	public static final IntegerFlag TIMEOUT = new IntegerFlag("timeout", 0);
	@FlagData
	public static final IntegerFlag ROUNDS = new IntegerFlag("rounds", 3);
	@FlagData(aliases = {"regeneration-intervall", "regeneration", "regen-intervall"})
	public static final IntegerFlag REGEN_INTERVALL = new IntegerFlag("regen", -1);
	
	@FlagData
	public static final ItemStackFlag ICON = new ItemStackFlag("icon", new ItemStack(Material.DIAMOND_SPADE));
	
	static {
		ONEVSONE.setConflictingFlags(TEAM);
		TEAM.setConflictingFlags(ONEVSONE);
		
		SHOVELS.setConflictingFlags(SPLEGG, BOWSPLEEF, SHEARS);
		SPLEGG.setConflictingFlags(SHOVELS, BOWSPLEEF, SHEARS);
		BOWSPLEEF.setConflictingFlags(SHOVELS, SPLEGG, SHEARS);
		SHEARS.setConflictingFlags(SHOVELS, SPLEGG, BOWSPLEEF);
		
		BOXES.setRequiredFlags(ONEVSONE);
		ROUNDS.setRequiredFlags(ONEVSONE);
	}
	
	public static List<Flag<?>> getFlagList() {
		List<Flag<?>> flags = new ArrayList<Flag<?>>();
		flags.addAll(customFlags);
		
		try {
			//Using java reflection here
			Field[] fields = FlagType.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				FlagData data = (FlagData) field.getAnnotation(FlagData.class);
				if (data == null)
					continue;
				
				Object value = field.get(null);
				if (!(value instanceof Flag<?>))
					continue;
				
				Flag<?> flag = (Flag<?>)value;
				flag.setAliases(data.aliases());
				flags.add(flag);
			}
		
		} catch (IllegalAccessException e) {}
		
		return flags;
	}
	
	/**
	 * Registeres a flag for spleef arenas
	 * 
	 * Your flag has to extend the Flag classes
	 * 
	 * @see IntegerFlag
	 * @see BooleanFlag
	 * @see EnumFlag
	 * @see ArrayItemStackFlag
	 * @see LocationFlag
	 */
	public static void registerFlag(Flag<?> flag) {
		customFlags.add(flag);
	}
	
	public static Flag<?> byName(String name) {
		for (Flag<?> flag : getFlagList()) {
			if (flag.getName().equalsIgnoreCase(name))
				return flag;
		}
		
		return null;
	}
	
}
