/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.block.Block;

import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.core.RatingCompute.RatingResult;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.GameStartEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.event.SpleefListener;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class StatisticRecorder implements SpleefListener {

	private final HeavySpleef heavySpleef;
	private final Logger logger;
	private RatingCompute ratingCompute;
	private Map<String, Statistic> loadedStatistics;
	private long gameStartedAt;
	
	public StatisticRecorder(HeavySpleef heavySpleef, Logger logger) {
		this.heavySpleef = heavySpleef;
		this.logger = logger;
		this.ratingCompute = new DefaultRatingCompute();
	}
	
	public void setRatingCompute(RatingCompute ratingCompute) {
		if (ratingCompute == null) {
			ratingCompute = new DefaultRatingCompute();
		}
		
		this.ratingCompute = ratingCompute;
	}
	
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		gameStartedAt = System.currentTimeMillis();
		Game game = event.getGame();
		
		Set<SpleefPlayer> players = game.getPlayers();
		String[] playerNames = new String[players.size()];
		
		int i = 0;
		for (SpleefPlayer player : players) {
			playerNames[i++] = player.getName();
		}
		
		AsyncReadWriteHandler databaseHandler = heavySpleef.getDatabaseHandler();
		databaseHandler.getStatistics(playerNames, new FutureCallback<Map<String, Statistic>>() {

			@Override
			public void onSuccess(Map<String, Statistic> statistics) {
				loadedStatistics = statistics;
			}

			@Override
			public void onFailure(Throwable t) {
				logger.log(Level.SEVERE, "Could not load statistics for players when computing the rating: ", t);
			}
		});
	}
	
	@Subscribe
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		
		if (game.getGameState() != GameState.INGAME) {
			return;
		}
		
		final AsyncReadWriteHandler databaseHandler = heavySpleef.getDatabaseHandler();
		final SpleefPlayer player = event.getPlayer();
		databaseHandler.getStatistic(player.getName(), new FutureCallback<Statistic>() {

			@Override
			public void onSuccess(Statistic result) {
				loadedStatistics.put(player.getName(), result);
			}

			@Override
			public void onFailure(Throwable t) {
				logger.log(Level.SEVERE, "Could not load statistic for player " + player.getName() + ": ", t);
			}
		});
	}
	
	@Subscribe
	public void onPlayerWinGame(PlayerWinGameEvent event) {
		final Game game = event.getGame();
		final SpleefPlayer[] winners = event.getWinners();
		
		if (loadedStatistics == null) {
			logger.log(Level.WARNING, "Cannot compute new rating for players in game " + game.getName() + ": Statistics were not loaded on game start");
			return;
		}
		
		RatingResult result = ratingCompute.compute(loadedStatistics, game, winners);
		Map<String, Double> newRating = result.getNewRating();
		
		for (Entry<String, Statistic> entry : loadedStatistics.entrySet()) {
			String name = entry.getKey();
			Statistic statistic = entry.getValue();
			
			statistic.setRating(newRating.get(name));
		}
		
		AsyncReadWriteHandler databaseHandler = heavySpleef.getDatabaseHandler();
		databaseHandler.saveStatistics(loadedStatistics.values(), null);
	}
	
	@Subscribe
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		Game game = event.getGame();
		SpleefPlayer player = event.getPlayer();
		
		if (game.getGameState() != GameState.INGAME) {
			return;
		}

		QuitCause cause = event.getCause();
		
		if (cause != QuitCause.STOP && cause != QuitCause.KICK) {
			EnumStatisticAction actionType = event.getCause() == QuitCause.WIN ? EnumStatisticAction.WIN : EnumStatisticAction.LOSE;
			
			Statistic playerStatistic = loadedStatistics.get(player.getName());
			
			Set<Block> blocksBroken = game.getBlocksBroken().get(player);
			if (blocksBroken != null) {
				StatisticAction addBlocksBrokenAction = new IncrementingStatisticAction(EnumStatisticAction.BLOCKS_BROKEN, blocksBroken.size());
				addBlocksBrokenAction.executeAction(playerStatistic, false);
			}
			
			StatisticAction action = new IncrementingStatisticAction(actionType);
			StatisticAction addGameAction = new IncrementingStatisticAction(EnumStatisticAction.GAMES_PLAYED);
			StatisticAction recordTimeAction = new RecordTimeAction();
			
			action.executeAction(playerStatistic, false);
			addGameAction.executeAction(playerStatistic, false);
			recordTimeAction.executeAction(playerStatistic, true);			
			
			SpleefPlayer killer = event.getKiller();
			if (killer != null) {
				Statistic killerStatistic = loadedStatistics.get(killer.getName());
				StatisticAction knockoutAction = new IncrementingStatisticAction(EnumStatisticAction.KNOCKOUT);
				knockoutAction.executeAction(killerStatistic, true);
			}
		}
	}
	
	private interface StatisticAction {
		
		public void executeAction(Statistic statistic, boolean save);
		
	}
	
	private abstract class DefaultStatisticAction implements StatisticAction {

		@Override
		public void executeAction(Statistic statistic, boolean save) {
			executeAction(statistic);
			
			if (save) {
				AsyncReadWriteHandler databaseHandler = heavySpleef.getDatabaseHandler();
				databaseHandler.saveStatistic(statistic, null);
			}
		}
		
		protected abstract void executeAction(Statistic statistic);
		
	}
	
	private class IncrementingStatisticAction extends DefaultStatisticAction {
		
		private final EnumStatisticAction action;
		private int increment = 1;
		
		public IncrementingStatisticAction(EnumStatisticAction action) {
			this.action = action;
		}
		
		public IncrementingStatisticAction(EnumStatisticAction action, int increment) {
			this.action = action;
			this.increment = increment;
		}
		
		@Override
		public void executeAction(Statistic statistic) {
			switch (action) {
			case WIN:
				int wins = statistic.getWins();
				statistic.setWins(wins + increment);
				break;
			case LOSE:
				int losses = statistic.getLosses();
				statistic.setLosses(losses + increment);
				break;
			case KNOCKOUT:
				int knockouts = statistic.getKnockouts();
				statistic.setKnockouts(knockouts + increment);
				break;
			case GAMES_PLAYED:
				int gamesPlayed = statistic.getGamesPlayed();
				statistic.setGamesPlayed(gamesPlayed + increment);
				break;
			case BLOCKS_BROKEN:
				int blocksBroken = statistic.getBlocksBroken();
				statistic.setBlocksBroken(blocksBroken + increment);
				break;
			default:
				break;
			}
		}
		
	}
	
	private class RecordTimeAction extends DefaultStatisticAction {

		@Override
		protected void executeAction(Statistic statistic) {
			long timeNow = System.currentTimeMillis();
			long dif = timeNow - gameStartedAt;
			
			long timePlayed = statistic.getTimePlayed();
			statistic.setTimePlayed(timePlayed + dif);
		}
		
	}
	
	private enum EnumStatisticAction {
		
		WIN,
		LOSE,
		KNOCKOUT,
		GAMES_PLAYED,
		BLOCKS_BROKEN;
		
	}
	
}