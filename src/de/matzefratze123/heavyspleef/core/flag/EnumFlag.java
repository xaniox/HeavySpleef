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

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class EnumFlag<E extends Enum<E>> extends Flag<E> {

	private Class<E> enumClass;
	
	public EnumFlag(String name, Class<E> enumClass, E defaulte) {
		super(name, defaulte);
		
		this.enumClass = enumClass;
	}

	@Override
	@SuppressWarnings("unchecked")
	public String serialize(Object object) {
		E constant = (E)object;
		return getName() + ":" + constant.name();
	}

	@Override
	public E deserialize(String str) {
		if (str == null)
			return null;
		
		String[] parts = str.split(":");
		if (parts.length < 2)
			return null;
		
		this.name = parts[0];
		try {
			return Enum.valueOf(enumClass, parts[1].toUpperCase());
		} catch (Exception e) {}//Nothing here
		
		return null;
	}

	@Override
	public E parse(Player player, String input) {
		if (input != null) {
			input = input.toUpperCase();
			input = input.trim();
		} else return null;
		
		return (E) Enum.valueOf(enumClass, input);
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public String toInfo(Object value) {
		try {
			E constant = (E)value;
			return getName() + ":" + constant.name();
		} catch (Exception e) {
			return new String();
		}
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + " /spleef flag <name> " + getName() + " [enum-constant]";
	}

	@Override
	public FlagType getType() {
		return FlagType.ENUM_FLAG;
	}
	
}
