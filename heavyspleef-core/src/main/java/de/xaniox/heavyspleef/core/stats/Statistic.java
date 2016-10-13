/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core.stats;

import com.google.common.util.concurrent.FutureCallback;
import de.xaniox.heavyspleef.core.game.Rateable;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.persistence.AsyncReadWriteHandler;
import de.xaniox.heavyspleef.core.script.Variable;
import de.xaniox.heavyspleef.core.script.VariableSuppliable;
import org.bukkit.command.CommandSender;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Statistic implements Comparable<Statistic>, Rateable, VariableSuppliable {

	private static final double START_RATING = 1000D;
    private static final DecimalFormat FORMAT = new DecimalFormat("0.##");
    private static final int ROWS_PER_PAGE = 10;

    private UUID uniqueIdentifier;
	private String lastName;
	private int wins;
	private int losses;
	private int knockouts;
	private int gamesPlayed;
	private long timePlayed;
	private int blocksBroken;
	private double rating = START_RATING;
	
	public Statistic() {}
	
	public Statistic(UUID uuid) {
		this.uniqueIdentifier = uuid;
	}
	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getWins() {
		return wins;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public int getLosses() {
		return losses;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public int getKnockouts() {
		return knockouts;
	}

	public void setKnockouts(int knockouts) {
		this.knockouts = knockouts;
	}

	public int getGamesPlayed() {
		return gamesPlayed;
	}

	public void setGamesPlayed(int gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}

	public long getTimePlayed() {
		return timePlayed;
	}

	public void setTimePlayed(long timePlayed) {
		this.timePlayed = timePlayed;
	}

	public int getBlocksBroken() {
		return blocksBroken;
	}

	public void setBlocksBroken(int blocksBroken) {
		this.blocksBroken = blocksBroken;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public UUID getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	@Override
	public int compareTo(Statistic o) {
		return Double.valueOf(rating).compareTo(o.rating);
	}

	public boolean isEmpty() {
		return wins == 0 && losses == 0 && knockouts == 0 && gamesPlayed == 0 && timePlayed == 0 && blocksBroken == 0 && rating == START_RATING;
	}

	@Override
	public void supply(Set<Variable> vars, Set<String> requested) {
		vars.add(new Variable("last-name", lastName));
		vars.add(new Variable("wins", wins));
		vars.add(new Variable("losses", losses));
		vars.add(new Variable("knockouts", knockouts));
		vars.add(new Variable("games-played", gamesPlayed));
		vars.add(new Variable("time-played", timePlayed));
		vars.add(new Variable("blocks-broken", blocksBroken));
		vars.add(new Variable("rating", (int)rating));
		vars.add(new Variable("rating-exact", rating));
	}

    private static void printStatistic(CommandSender sender, Statistic statistic, String owner, String statisticMessageKey, int place, I18N i18n) {
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

    public interface StatisticPrinter {

        public void print();

    }

    public static class FullStatisticPrinter implements StatisticPrinter, FutureCallback<Statistic> {

        private AsyncReadWriteHandler handler;
        private CommandSender sender;
        private String player;
        private Logger logger;
        private I18N i18n = I18NManager.getGlobal();

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
                    printStatistic(sender, statistic, player, Messages.Command.STATISTIC_FORMAT, rank, i18n);
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

    public static class TopStatisticPrinter implements StatisticPrinter, FutureCallback<Map<String, Statistic>> {

        private Logger logger;
        private CommandSender sender;
        private int page;
        private AsyncReadWriteHandler handler;
        private I18N i18n = I18NManager.getGlobal();

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
            for (Map.Entry<String, Statistic> entry : result.entrySet()) {
                String name = entry.getKey();
                Statistic statistic = entry.getValue();

                int place = (page - 1) * ROWS_PER_PAGE + i++ + 1;

                printStatistic(sender, statistic, name, Messages.Command.TOP_STATISTIC_FORMAT, place, i18n);
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