package me.matzefratze123.heavyspleef.api;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;

public class GameAPI {
	
	public static GameAPI getInstance() {
		return new GameAPI();
	}
	
	public boolean createCuboidGame(String name, Location... locations) {
		if (name == null)
			return false;
		if (locations.length < 2)
			throw new IllegalArgumentException("To few locations");
		if (hasGame(name))
			return false;
		
		GameManager.createCuboidGame(name, locations[0], locations[1]);
		return true;
	}
	
	public boolean createCylinderGame(String name, Location center, int radius, int minY, int maxY) {
		Validate.notNull(name);
		Validate.notNull(center);
		
		if (radius < 0 || minY < 0 || maxY > 256)
			return false;
		if (hasGame(name))
			return false;
		
		GameManager.createCylinderGame(name, center, radius, minY, maxY);
		return true;
	}
	
	public boolean delete(String name) {
		Validate.notNull(name);
		
		if (!hasGame(name))
			return false;
		
		GameManager.deleteGame(name);
		return true;
	}
	
	public boolean delete(GameData data) {
		Validate.notNull(data);
		
		if (!hasGame(data.getGame().getName()))
			return false;
		
		GameManager.deleteGame(data.getGame().getName());
		return true;
	}
	
	public GameData getGameData(String name) {
		Validate.notNull(name);
		
		if (!hasGame(name))
			return null;
		
		return new GameData(name);
	}
	
	public GameData getGameData(Player player) {
		Validate.notNull(player);
		
		Game game = GameManager.fromPlayer(player);
		return new GameData(game);
	}
	
	public GameData[] getGameDatas() {
		Game[] games = GameManager.getGames();
		GameData[] datas = new GameData[games.length];
		
		for (int i = 0; i < games.length; i++)
			datas[i] = new GameData(games[i]);
		
		return datas;
	}
	
	public boolean hasGame(String name) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null");
		
		return GameManager.hasGame(name);
	}
	
	public QueueAPI getQueueAPI() {
		return QueueAPI.getInstance();
	}
}
