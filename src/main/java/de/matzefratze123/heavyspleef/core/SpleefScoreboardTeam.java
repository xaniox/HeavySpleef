/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.ImmutableList;

import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class SpleefScoreboardTeam implements SpleefScoreboard, SpleefPlayerGameListener {

	private Game		game;
	private Scoreboard	scoreboard;
	private Objective	objective;
	
	private List<Team> lastIngameTeams;

	public SpleefScoreboardTeam(Game game) {
		this.game = game;
		this.lastIngameTeams = ImmutableList.copyOf(game.getComponents().getActiveTeams());
		
		game.registerGameListener(this);
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		objective = scoreboard.registerNewObjective("heavyspleefScore", SCOREBOARD_CRITERIA);

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Spleef Knockouts");
	}

	@Override
	public void updateScoreboard() {
		for (Team team : game.getComponents().getTeams()) {
			OfflinePlayer fakePlayer = getFakeOfflinePlayer(team, team.hasPlayersLeft());
			
			if (team.hasPlayersLeft()) {
				objective.getScore(fakePlayer).setScore(team.getCurrentKnockouts());
			} else {
				if (!lastIngameTeams.contains(team)) {
					return;
				}
				
				scoreboard.resetScores(getFakeOfflinePlayer(team, true));
				objective.getScore(fakePlayer).setScore(team.getCurrentKnockouts());
			}
		}
		
		lastIngameTeams = ImmutableList.copyOf(game.getComponents().getActiveTeams());
	}
	
	private OfflinePlayer getFakeOfflinePlayer(Team team, boolean hasPlayersLeft) {
		String fakeName = (hasPlayersLeft ? team.getColor().toChatColor() : ChatColor.GRAY) + team.getColor().toString();
		if (fakeName.length() > 16) {
			fakeName = fakeName.substring(0, 16);
		}
		
		return Bukkit.getOfflinePlayer(fakeName);
	}

	@Override
	public void removeScoreboard() {
		for (SpleefPlayer player : game.getIngamePlayers()) {
			Scoreboard last = player.getState().getBoard();

			if (last == null) {
				last = Bukkit.getScoreboardManager().getMainScoreboard();
			}

			player.getBukkitPlayer().setScoreboard(last);
		}
		
		game.unregisterGameListener(this);
	}

	@Override
	public void show() {
		for (SpleefPlayer player : game.getIngamePlayers()) {
			player.getBukkitPlayer().setScoreboard(scoreboard);
		}
		
		lastIngameTeams = ImmutableList.copyOf(game.getComponents().getActiveTeams());
	}

	@Override
	public void playerJoin(SpleefPlayer player) {
		updateScoreboard();
	}

	@Override
	public void playerLeave(SpleefPlayer player) {
		updateScoreboard();

		Scoreboard last = player.getState().getBoard();

		if (last == null) {
			last = Bukkit.getScoreboardManager().getMainScoreboard();
		}

		player.getBukkitPlayer().setScoreboard(last);
	}

	@Override
	public void playerKnockout(SpleefPlayer player) {}

	@Override
	public void playerWin(SpleefPlayer player) {}

}
