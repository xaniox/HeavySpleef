package de.matzefratze123.heavyspleef.api;

import java.util.List;

public interface IGameManager {
	
	public void addGame(IGame game);
	
	public void deleteGame(String name);
	
	public boolean hasGame(String name);
	
	public IGame getGame(String name);
	
	public List<IGame> getGames();

}
