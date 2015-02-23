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

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class SpleefScoreboardFFA implements SpleefScoreboard, SpleefPlayerGameListener {

	private Game game;
	private Scoreboard scoreboard;
	private Objective objective;
	
	private List<SpleefPlayer> lastIngamePlayers;
	
	public SpleefScoreboardFFA(Game game) {
		this.game = game;
		this.lastIngamePlayers = ImmutableList.copyOf(game.getIngamePlayers());
		game.registerGameListener(this);
		
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("heavyspleefScore", SCOREBOARD_CRITERIA);
		
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Spleef Knockouts");
	}
	
	@Override
	public void updateScoreboard() {
		List<SpleefPlayer> ingame = game.getIngamePlayers();
		List<OfflinePlayer> out = game.getOutPlayers();
		
		for (SpleefPlayer player : ingame) {
			objective.getScore(getFakeOfflinePlayer(player.getBukkitPlayer(), true)).setScore(player.getKnockouts());
		}
		
		for (OfflinePlayer player : out) {
			if (!player.isOnline()) {
				scoreboard.resetScores(player);
				continue;
			}
			
			SpleefPlayer spleefPlayer = HeavySpleef.getInstance().getSpleefPlayer(player.getName());
			
			if (!lastIngamePlayers.contains(spleefPlayer)) {
				continue;
			}
			
			scoreboard.resetScores(getFakeOfflinePlayer(player, true));
			objective.getScore(getFakeOfflinePlayer(player, false)).setScore(spleefPlayer.getKnockouts());
		}
		
		lastIngamePlayers = ImmutableList.copyOf(game.getIngamePlayers());
	}
	
	@Override
	public void show() {
		lastIngamePlayers = ImmutableList.copyOf(game.getIngamePlayers());
		
		for (SpleefPlayer player : game.getIngamePlayers()) {
			player.getBukkitPlayer().setScoreboard(scoreboard);
		}
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
	
	private String getColoredPlayer(OfflinePlayer player, boolean ingame) {
		ChatColor prefix = null;
		
		if (!ingame) {
			prefix = ChatColor.GRAY;
		}
		
		return (prefix != null ? prefix : "") + player.getName();
	}
	
	private OfflinePlayer getFakeOfflinePlayer(OfflinePlayer player, boolean ingame) {
		String fakeName = (ingame ? "" : ChatColor.GRAY) + player.getName();
		if (fakeName.length() > 16) {
			fakeName = fakeName.substring(0, 16);
		}
		
		return Bukkit.getOfflinePlayer(fakeName);
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
