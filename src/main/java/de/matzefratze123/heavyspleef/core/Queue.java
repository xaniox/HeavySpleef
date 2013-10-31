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
package de.matzefratze123.heavyspleef.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Queue {

	private Game game = null;
	private Team team = null;
	private String owner = null;
	
	public Queue(Player owner, Game game) {
		this.game = game;
		this.owner = owner.getName();
	}
	
	public Queue(Player owner, Game game, Team team) {
		this.owner = owner.getName();
		this.team = team;
		this.game = game;
	}
	
	public Game getGame() {
		return this.game;
	}
	
	public Team getTeam() {
		return this.team;
	}
	
	public Player getOwner() {
		return Bukkit.getPlayer(this.owner);
	}
	
}
