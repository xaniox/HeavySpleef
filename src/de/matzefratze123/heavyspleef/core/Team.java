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

import java.util.ArrayList;
import java.util.List;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.kitteh.tag.TagAPI;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.hooks.TagAPIHook;
import de.matzefratze123.heavyspleef.util.Util;

public class Team {

	public static ChatColor[] allowedColors = new ChatColor[] {ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GRAY};
	
	private Game game;
	private List<String> players = new ArrayList<String>();
	private ChatColor color;
	
	private int maxplayers = -1;
	private int minplayers = -1;
	
	private int currentKnockouts = 0;
	
	public Team(ChatColor color, Game game) {
		this.color = color;
		this.game = game;
	}
	
	public ChatColor getColor() {
		return this.color;
	}
	
	public void join(Player player) {
		if (players.contains(player.getName())) {
			player.sendMessage(Game._("alreadyInTeam", color + Util.toFriendlyString(color.name())));
			return;
		}
		
		players.add(player.getName());
		player.sendMessage(Game._("addedToTeam", color + Util.toFriendlyString(color.name())));
		
		if (HeavySpleef.hooks.getService(TagAPIHook.class).hasHook())
			TagAPI.refreshPlayer(player);
	}
	
	public void leave(Player player) {
		if (!players.contains(player.getName())) {
			player.sendMessage(Game._("notInThisTeam", color + Util.toFriendlyString(color.name())));
			return;
		}
		
		players.remove(player.getName());
		player.sendMessage(Game._("removedFromTeam", color + Util.toFriendlyString(color.name())));
		
		if (HeavySpleef.hooks.getService(TagAPIHook.class).hasHook())
			TagAPI.refreshPlayer(player);
	}
	
	public boolean hasPlayer(Player player) {
		return players.contains(player.getName());
	}
	
	public boolean hasPlayersLeft() {
		return players.size() > 0;
	}
	
	public Player[] getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for (String str : this.players) {
			Player player = Bukkit.getPlayer(str);
			if (player == null)
				continue;
			
			players.add(player);
		}
		
		return players.toArray(new Player[players.size()]);
	}
	
	public Game getGame() {
		return game;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof Team))
			return false;
		Team team = (Team)o;
		if (team.getColor() != getColor())
			return false;
		
		return true;
	}
	
	public static byte chatColorToWoolDye(ChatColor color) {
		switch (color) {
		case RED:
			return 0xE;
		case GREEN:
			return 0x5;
		case BLUE:
			return 0xB;
		case YELLOW:
			return 0x4;
		case GRAY:
			return 0x8;
		default:
			return 0x0;
		}	
	}
	
	public static ChatColor woolDyeToChatColor(byte woolDye) {
		switch(woolDye) {
		case 0xE:
			return ChatColor.RED;
		case 0x5:
			return ChatColor.GREEN;
		case 0xB:
			return ChatColor.BLUE;
		case 0x4:
			return ChatColor.YELLOW;
		case 0x8:
			return ChatColor.GRAY;
		default:
			return ChatColor.WHITE;
		}
	}
	
	public void setMaxPlayers(int maxplayers) {
		this.maxplayers = maxplayers;
	}
	
	public void setMinPlayers(int minplayers) {
		this.minplayers = minplayers;
	}
	
	public int getMinPlayers() {
		return this.minplayers;
	}
	
	public int getMaxPlayers() {
		return this.maxplayers;
	}
	
	public int getCurrentKnockouts() {
		return this.currentKnockouts;
	}
	
	public void resetKnockouts() {
		this.currentKnockouts = 0;
	}
	
	public void addKnockout() {
		this.currentKnockouts += 1;
	}
	
}
