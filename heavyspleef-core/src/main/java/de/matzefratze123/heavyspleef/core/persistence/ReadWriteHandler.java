package de.matzefratze123.heavyspleef.core.persistence;

import java.util.List;
import java.util.TreeSet;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Statistic;

public interface ReadWriteHandler {
	
	public void saveGames(Iterable<Game> iterable);
	
	public void saveGame(Game game);
	
	public Game getGame(String name);
	
	public List<Game> getGames();
	
	public void saveStatistics(Iterable<Statistic> iterable);
	
	public void saveStatistic(Statistic statistic);
	
	public Statistic getStatistic(String player);

	public TreeSet<Statistic> getTopStatistics(int limit);
	
}
