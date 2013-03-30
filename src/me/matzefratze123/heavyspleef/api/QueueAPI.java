package me.matzefratze123.heavyspleef.api;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

public class QueueAPI {

	public static QueueAPI getInstance() {
		return new QueueAPI();
	}
	
	public GameData getQueue(Player player) {
		Validate.notNull(player);
		
		Game game = GameManager.getQueue(player);
		return new GameData(game);
	}
	
	public boolean hasQueue(Player player) {
		Validate.notNull(player, "Player cannot be null");
		return GameManager.isInQueue(player);
	}
	
	public boolean addQueue(Player player, GameData data) {
		Validate.notNull(player, "Player cannot be null");
		Validate.notNull(data, "GameData cannot be null");
		
		if (hasQueue(player))
			GameManager.removeFromQueue(player);
		
		GameManager.addQueue(player, data.getGame().getName());
		return true;
	}
	
	public boolean removeQueue(Player player) {
		Validate.notNull(player, "Player cannot be null");
		
		if (!hasQueue(player))
			return false;
		
		GameManager.removeFromQueue(player);
		return true;
	}
	
	public void removeAllQueues(GameData data) {
		Validate.notNull(data, "GameData cannot be null");
		GameManager.removeAllPlayersFromGameQueue(data.getGame().getName());
	}
	
}
