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
package de.matzefratze123.heavyspleef.commands;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.Statistic;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.persistence.AsyncReadWriteHandler;

public class CommandStats {
	
	private static final int ROWS_PER_PAGE = 10;
	private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
	private final I18N i18n = I18NManager.getGlobal();
	
	@Command(name = "stats", usage = "/spleef stats [player|top [page]]",
			descref = Messages.Help.Description.STATS,
			permission = Permissions.PERMISSION_STATS)
	public void onStatsCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		AsyncReadWriteHandler databaseHandler = heavySpleef.getDatabaseHandler();
		
		StatisticPrinter printer;
		
		if (context.argsLength() > 0) {
			String arg = context.getString(0);
			
			if (arg.equalsIgnoreCase("top")) {
				int page = 1;
				
				if (context.argsLength() > 1) {
					//Try to parse a page number
					try {
						page = Integer.parseInt(context.getString(1));
					} catch (NumberFormatException nfe) {
						//Just use the default value
					}	
				}
				
				printer = new TopStatisticPrinter(sender, page, databaseHandler, heavySpleef.getLogger());
			} else {
				printer = new FullStatisticPrinter(databaseHandler, sender, arg, heavySpleef.getLogger());
			}
		} else {
			if (!(sender instanceof Player)) {
				sender.sendMessage(i18n.getString(Messages.Command.PLAYER_ONLY));
				return;
			}
			
			printer = new FullStatisticPrinter(databaseHandler, sender, sender.getName(), heavySpleef.getLogger());
		}
		
		printer.print();
	}
	
	private void printStatistic(CommandSender sender, Statistic statistic, String owner, String statisticMessageKey, int place) {
		int wins = statistic.getWins();
		int losses = statistic.getLosses();
		int knockouts = statistic.getKnockouts();
		int gamesPlayed = statistic.getGamesPlayed();
		int blocksBroken = statistic.getBlocksBroken();
		long timePlayed = statistic.getTimePlayed();
		double rating = statistic.getRating();
		
		double kd = losses > 0 ? (double) knockouts / losses : knockouts;
		double wl = losses > 0 ? (double) wins / losses : losses;
		double kg = gamesPlayed > 0 ? (double) knockouts / gamesPlayed : knockouts;
		
		long days = TimeUnit.MILLISECONDS.toDays(timePlayed);
		timePlayed -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(timePlayed);
		timePlayed -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(timePlayed);
		timePlayed -= TimeUnit.MINUTES.toMillis(timePlayed);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(timePlayed);
		
		String timeFormat = i18n.getVarString(Messages.Command.TIME_FORMAT)
				.setVariable("days", String.valueOf(days))
				.setVariable("hours", String.valueOf(hours))
				.setVariable("minutes", String.valueOf(minutes))
				.setVariable("seconds", String.valueOf(seconds))
				.toString();
		
		sender.sendMessage(i18n.getVarString(statisticMessageKey)
				.setVariable("player", owner)
				.setVariable("place", String.valueOf(place))
				.setVariable("wins", String.valueOf(wins))
				.setVariable("losses", String.valueOf(losses))
				.setVariable("knockouts", String.valueOf(knockouts))
				.setVariable("games-played", String.valueOf(gamesPlayed))
				.setVariable("blocks-broken", String.valueOf(blocksBroken))
				.setVariable("time-played", timeFormat)
				.setVariable("rating", String.valueOf((int)rating))
				.setVariable("knockouts-per-death", FORMAT.format(kd))
				.setVariable("wins-per-lose", FORMAT.format(wl))
				.setVariable("knockouts-per-game", FORMAT.format(kg))
				.toString());
	}
	
	private interface StatisticPrinter {
		
		public void print();
		
	}
	
	private class FullStatisticPrinter implements StatisticPrinter, FutureCallback<Statistic> {
		
		private AsyncReadWriteHandler handler;
		private CommandSender sender;
		private String player;
		private Logger logger;
		
		public FullStatisticPrinter(AsyncReadWriteHandler handler, CommandSender sender, String player, Logger logger) {
			this.handler = handler;
			this.sender = sender;
			this.player = player;
			this.logger = logger;
		}
		
		@Override
		public void print() {
			handler.getStatistic(player, this);
		}
		
		@Override
		public void onSuccess(final Statistic statistic) {
			handler.getStatisticRank(player, new FutureCallback<Integer>() {

				@Override
				public void onSuccess(Integer rank) {
					sender.sendMessage(i18n.getVarString(Messages.Command.STATISTIC_HEADER)
							.setVariable("player", player)
							.toString());
					printStatistic(sender, statistic, player, Messages.Command.STATISTIC_FORMAT, rank);
					sender.sendMessage(i18n.getVarString(Messages.Command.STATISTIC_FOOTER)
							.setVariable("player", player)
							.toString());
				}

				@Override
				public void onFailure(Throwable t) {
					reportException(t);
				}
			});
		}

		@Override
		public void onFailure(Throwable t) {
			reportException(t);
		}
		
		private void reportException(Throwable t) {
			sender.sendMessage(i18n.getString(Messages.Command.ERROR_ON_STATISTIC_LOAD));
			logger.log(Level.SEVERE, "Could not load statistic for player " + player + ": ", t);
		}
		
	}
	
	private class TopStatisticPrinter implements StatisticPrinter, FutureCallback<Map<String, Statistic>> {

		private Logger logger;
		private CommandSender sender;
		private int page;
		private AsyncReadWriteHandler handler;
		
		public TopStatisticPrinter(CommandSender sender, int page, AsyncReadWriteHandler handler, Logger logger) {
			this.sender = sender;
			this.page = page;
			this.handler = handler;
			this.logger = logger;
		}
		
		@Override
		public void print() {
			handler.getTopStatistics((page - 1) * ROWS_PER_PAGE, ROWS_PER_PAGE, this);
		}
		
		@Override
		public void onSuccess(Map<String, Statistic> result) {
			sender.sendMessage(i18n.getVarString(Messages.Command.TOP_STATISTICS_HEADER)
					.setVariable("page", String.valueOf(page))
					.toString());
			
			int i = 0;
			for (Entry<String, Statistic> entry : result.entrySet()) {
				String name = entry.getKey();
				Statistic statistic = entry.getValue();
				
				int place = (page - 1) * ROWS_PER_PAGE + i++ + 1;
				
				printStatistic(sender, statistic, name, Messages.Command.TOP_STATISTIC_FORMAT, place);
			}
			
			sender.sendMessage(i18n.getString(Messages.Command.TOP_STATISTICS_FOOTER));
		}

		@Override
		public void onFailure(Throwable t) {
			sender.sendMessage(i18n.getString(Messages.Command.ERROR_ON_STATISTIC_LOAD));
			logger.log(Level.SEVERE, "Could not load top statistic list: ", t);
		}
		
	}
	
}
