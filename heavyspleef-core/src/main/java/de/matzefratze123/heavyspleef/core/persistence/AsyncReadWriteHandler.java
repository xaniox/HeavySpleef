package de.matzefratze123.heavyspleef.core.persistence;

import java.util.List;
import java.util.TreeSet;

import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Statistic;

public interface AsyncReadWriteHandler {
	
	public void saveGames(Iterable<Game> iterable, FutureCallback<Void> callback);
	
	public void saveGame(Game game, FutureCallback<Void> callback);
	
	public void getGame(String name, FutureCallback<Game> callback);
	
	public void getGames(FutureCallback<List<Game>> callback);
	
	public void saveStatistics(Iterable<Statistic> statistics, FutureCallback<Void> callback);
	
	public void saveStatistic(Statistic statistic, FutureCallback<Void> callback);
	
	public void getStatistic(String player, FutureCallback<Statistic> callback);
	
	public void getTopStatistics(int limit, FutureCallback<TreeSet<Statistic>> callback);
	
}
