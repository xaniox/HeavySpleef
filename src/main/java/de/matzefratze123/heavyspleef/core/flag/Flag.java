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
package de.matzefratze123.heavyspleef.core.flag;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.database.FlagSerializeable;

/**
 * This class represents a flag of a game</br>
 * As this class uses generics, you should
 * use the subclasses of this class.
 * 
 * @see IntegerFlag
 * @see BooleanFlag
 * @see EnumFlag
 * @see ArrayItemStackFlag
 * @see LocationFlag
 * 
 * @author matzefratze123
 *
 * @param <T> The value of this flag
 */
public abstract class Flag<T> implements FlagSerializeable<T> {

	T defaulte;
	String[] aliases;
	
	protected String name;
	protected List<Flag<?>> conflicts = new ArrayList<Flag<?>>();
	protected List<Flag<?>> required = new ArrayList<Flag<?>>();
	
	public Flag(String name, T defaulte) {
		this.name = name;
		this.defaulte = defaulte;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}
	
	public String[] getAliases() {
		return this.aliases;
	}
	
	public abstract T parse(Player player, String input, Object previousObject);
	
	public abstract String toInfo(Object value);
	
	public abstract String getHelp();
	
	public abstract FlagType getType();
	
	public void setConflictingFlags(Flag<?>... conflicts) {
		this.conflicts.clear();
		
		for (Flag<?> flag : conflicts) {
			this.conflicts.add(flag);
		}
	}
	
	public List<Flag<?>> getConflictingFlags() {
		return conflicts;
	}
	
	public void setRequiredFlags(Flag<?>... flags) {
		required.clear();
		
		for (Flag<?> flag : flags) {
			this.required.add(flag);
		}
	}
	
	public List<Flag<?>> getRequiredFlags() {
		return required;
	}
	
	public T getAbsoluteDefault() {
		return this.defaulte;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
