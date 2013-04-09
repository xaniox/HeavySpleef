package me.matzefratze123.heavyspleef.core;

import java.util.ArrayList;
import java.util.List;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.hooks.TagAPIHook;
import me.matzefratze123.heavyspleef.utility.MaterialHelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.kitteh.tag.TagAPI;

public class Team {

	public static ChatColor[] allowedColors = new ChatColor[] {ChatColor.RED, ChatColor.BLUE, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GRAY};
	
	private Game game;
	private List<String> players = new ArrayList<String>();
	private ChatColor color;
	
	public Team(ChatColor color, Game game) {
		this.color = color;
		this.game = game;
	}
	
	public ChatColor getColor() {
		return this.color;
	}
	
	public void join(Player player) {
		if (players.contains(player.getName())) {
			player.sendMessage(Game._("alreadyInTeam", color + MaterialHelper.getName(color.name())));
			return;
		}
		
		players.add(player.getName());
		player.sendMessage(Game._("addedToTeam", color + MaterialHelper.getName(color.name())));
		
		if (HeavySpleef.hooks.getService(TagAPIHook.class).hasHook())
			TagAPI.refreshPlayer(player);
	}
	
	public void leave(Player player) {
		if (!players.contains(player.getName())) {
			player.sendMessage(Game._("notInThisTeam", color + MaterialHelper.getName(color.name())));
			return;
		}
		
		players.remove(player.getName());
		player.sendMessage(Game._("removedFromTeam", color + MaterialHelper.getName(color.name())));
		
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
			return 0x3;
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
		case 0x3:
			return ChatColor.BLUE;
		case 0x4:
			return ChatColor.YELLOW;
		case 0x8:
			return ChatColor.GRAY;
		default:
			return ChatColor.WHITE;
		}
	}
	
}
