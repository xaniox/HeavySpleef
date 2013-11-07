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
package de.matzefratze123.heavyspleef.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import de.matzefratze123.heavyspleef.listener.TagListener;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Util;

public class Team {

	public static ChatColor[] allowedColors = new ChatColor[] {ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GRAY};
	
	private List<SpleefPlayer> players = new ArrayList<SpleefPlayer>();
	private ChatColor color;
	
	private int maxplayers = -1;
	private int minplayers = -1;
	
	public Team(ChatColor color) {
		this.color = color;
	}
	
	public ChatColor getColor() {
		return this.color;
	}
	
	public void join(SpleefPlayer player) {
		if (players.contains(player)) {
			player.sendMessage(LanguageHandler._("alreadyInTeam", color + Util.formatMaterialName(color.name())));
			return;
		}
		
		players.add(player);
		player.sendMessage(LanguageHandler._("addedToTeam", color + Util.formatMaterialName(color.name())));
		
		TagListener.setTag(player, color);
	}
	
	public void leave(SpleefPlayer player) {
		if (!players.contains(player)) {
			player.sendMessage(LanguageHandler._("notInThisTeam", color + Util.formatMaterialName(color.name())));
			return;
		}
		
		players.remove(player);
		player.sendMessage(LanguageHandler._("removedFromTeam", color + Util.formatMaterialName(color.name())));
		
		TagListener.setTag(player, null);
	}
	
	public boolean hasPlayer(SpleefPlayer player) {
		return players.contains(player);
	}
	
	public boolean hasPlayersLeft() {
		return players.size() > 0;
	}
	
	public List<SpleefPlayer> getPlayers() {
		return players;
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
		int knockouts = 0;
		
		for (SpleefPlayer player : players) {
			knockouts += player.getKnockouts();
		}
		
		return knockouts;
	}
	
}
