package de.matzefratze123.heavyspleef.core;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

@XmlRootElement(name = "games")
public class GameManager {

	@XmlTransient
	private AsyncReadWriteHandler databaseHandler;
	private Map<String, Game> games;
	
	public GameManager(AsyncReadWriteHandler databaseHandler) {
		this.databaseHandler = databaseHandler;
		this.games = Maps.newHashMap();
	}
	
	public void addGame(Game game) {
		String name = game.getName();
		Validate.isTrue(!games.containsKey(name));
		
		games.put(game.getName(), game);
	}
	
	public Game deleteGame(String name) {
		Game game = games.remove(name);
		
		databaseHandler.deleteGame(game, null);
		return game;
	}
	
	public boolean hasGame(String name) {
		return games.containsKey(name);
	}
	
	public Game getGame(String name) {
		return games.get(name);
	}
	
	public Game getGame(SpleefPlayer player) {
		for (Game game : games.values()) {
			if (game.getPlayers().contains(player)) {
				return game;
			}
		}
		
		return null;
	}
	
	public Collection<Game> getGames() {
		return games.values();
	}
	
}
