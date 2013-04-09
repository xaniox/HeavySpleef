package me.matzefratze123.heavyspleef.core;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import static me.matzefratze123.heavyspleef.core.GameManager.*;

/**
 * Provides a global queue manager for all games
 * 
 * @author matzefratze123
 */
public class QueuesManager {

	public static boolean hasQueue(Player player) {
		boolean has = false;
		
		for (Game game : getGames()) {
			has = game.hasQueue(player);
			if (has) break;
		}
		
		return has;
	}
	
	public static void removeFromQueue(Player player) {
		for (Game game : getGames())
			game.removeFromQueue(player);
	}
	
	public static Game getQueue(Player player) {
		for (Game game : getGames()) {
			if (game.getQueue(player) != null)
				return game;
		}
		
		return null;
	}
	
	public static void addToQueue(Player player, Game game, ChatColor color) {
		if (game == null)
			return;
		
		game.addToQueue(player, color);
	}
	
}
