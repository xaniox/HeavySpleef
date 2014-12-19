package de.matzefratze123.heavyspleef.core;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

@XmlRootElement(name = "games")
public class GameManager {

	private Map<String, Game> games;
	
	public GameManager() {
		games = Maps.newHashMap();
	}
	
	public void addGame(Game game) {
		String name = game.getName();
		Validate.isTrue(!games.containsKey(name));
		
		games.put(game.getName(), game);
	}
	
	public Game deleteGame(String name) {
		return games.remove(name);
	}
	
	public boolean hasGame(String name) {
		return games.containsKey(name);
	}
	
	public Game getGame(String name) {
		return games.get(name);
	}
	
	public Game getGame(SpleefPlayer player) {
		Optional<Game> optional = games.values().stream().filter(game -> game.getPlayers().contains(player)).findFirst();
		return optional.isPresent() ? optional.get() : null;
	}
	
	public Collection<Game> getGames() {
		return games.values();
	}
	
	public void forEach(Consumer<? super Game> action) {
		games.values().forEach(action);
	}
	
}
