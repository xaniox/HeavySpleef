package de.matzefratze123.heavyspleef.api;

import java.util.ArrayList;
import java.util.List;

import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;

public class GameManagerAPI implements IGameManager {

	private static GameManagerAPI instance;
	
	static {
		if (instance == null) {
			instance = new GameManagerAPI();
		}
	}
	
	private GameManagerAPI() {}
	
	public static GameManagerAPI getInstance() {
		return instance;
	}

	@Override
	public void addGame(IGame game) {
		GameManager.addGame((Game)game);
	}

	@Override
	public void deleteGame(String name) {
		GameManager.deleteGame(name);
	}

	@Override
	public boolean hasGame(String name) {
		return GameManager.hasGame(name);
	}

	@Override
	public IGame getGame(String name) {
		return GameManager.getGame(name);
	}

	@Override
	public List<IGame> getGames() {
		List<IGame> games = new ArrayList<IGame>();
		
		for (Game game : GameManager.getGames()) {
			games.add(game);
		}
		
		return games;
	}
	
	

}
