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
package de.matzefratze123.heavyspleef.selection;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;

/**
 * Contains selections for players
 * and methods to modify them
 * 
 * @author matzefratze123
 */
public class SelectionManager {
	
	/** The ArrayList containing all locations and selections of players **/
	private List<Selection> selections = new ArrayList<Selection>();
	private WandType type;
	
	public SelectionManager() {
		setup();
	}
	
	public void setup() {
		type = HeavySpleef.getSystemConfig().getGeneralSection().getWandType();
	}
	
	public Selection getSelection(Player player) {
		Selection s = getRawSelection(player);
		
		if (s == null && HookManager.getInstance().getService(WorldEditHook.class).hasHook() && getWandType() == WandType.WORLDEDIT)
			s = new SelectionWorldEdit(player.getName());
		
		if (s == null) {
			addHSSelection(player);
			s = getRawSelection(player);
		}
		
		return s;
	}
	
	private Selection getRawSelection(Player player) {
		for (Selection s : selections) {
			if (s.getOwner().equalsIgnoreCase(player.getName()))
				return s;
		}
		
		return null;
	}
	
	protected void addHSSelection(Player player) {
		if (selections.contains(player.getName()))
			return;
		selections.add(new SelectionHeavySpleef(player.getName()));
	}
	
	public WandType getWandType() {
		return this.type;
	}
	
	public static enum WandType {
		
		HEAVYSPLEEF,
		WORLDEDIT;
		
	}

}