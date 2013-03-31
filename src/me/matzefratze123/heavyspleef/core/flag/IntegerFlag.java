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

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.entity.Player;

public class IntegerFlag extends Flag<Integer> {

	public IntegerFlag(String name) {
		super(name);
	}

	@Override
	public Integer parse(Player player, String input) {
		try {
			int i = Integer.parseInt(input);
			i = Math.abs(i);
			return i;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + " /spleef flag <name> " + getName() + " [number]";
	}

}
