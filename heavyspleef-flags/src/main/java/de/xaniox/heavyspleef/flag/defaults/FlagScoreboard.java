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
package de.xaniox.heavyspleef.flag.defaults;

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.event.Subscribe.Priority;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameProperty;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Map;

@Flag(name = "scoreboard")
public class FlagScoreboard extends BaseFlag {

	private static final String SCOREBOARD_NAME = "heavyspleef";
	private static final String SCOREBOARD_CRITERIA = "dummy";
	
	private static final String DEFAULT_OBJECTIVE_NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "Spleef";
	private static final int MAX_OBJECTIVE_ENTRIES = 16;
	
	private static final String IS_ALIVE_SYMBOL = ChatColor.GREEN + "✔ " + ChatColor.WHITE;
	private static final String IS_DEAD_SYMBOL = ChatColor.RED + "✘ " + ChatColor.GRAY;
	
	private final ScoreboardManager manager;
	private Scoreboard scoreboard;
	private Objective objective;
	private List<SpleefPlayer> playersTracked;
	
	public FlagScoreboard() {
		this.manager = Bukkit.getScoreboardManager();
		this.playersTracked = Lists.newArrayList();
	}

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    @Override
	public void defineGameProperties(Map<GameProperty, Object> properties) {}

	@Override
	public void getDescription(List<String> description) {
		description.add("Enables a sidebar scoreboard to show the status of the game");
	}
	
	@SuppressWarnings("deprecation")
	@Subscribe
	public void onGameStart(GameStartEvent event) {
		playersTracked.clear();
		scoreboard = manager.getNewScoreboard();
		objective = scoreboard.registerNewObjective(SCOREBOARD_NAME, SCOREBOARD_CRITERIA);
		
		GetScoreboardDisplayNameEvent getDisplayNameEvent = new GetScoreboardDisplayNameEvent();
		event.getGame().getEventBus().callEvent(getDisplayNameEvent);
		String displayName = getDisplayNameEvent.getDisplayName();
		
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(displayName);
		
		int index = 0;
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			Team team = scoreboard.registerNewTeam(player.getName());
			team.setPrefix(IS_ALIVE_SYMBOL + (player.isVip() ? getHeavySpleef().getVipPrefix() : ""));
			team.addPlayer(player.getBukkitPlayer());
			
			if (index < MAX_OBJECTIVE_ENTRIES) {
				Score score = objective.getScore(player.getName());
				score.setScore(0);
				playersTracked.add(player);
			}
			
			player.getBukkitPlayer().setScoreboard(scoreboard);
			index++;
		}
	}
	
	@Subscribe(priority = Priority.LOW)
	public void onGetScoreboardDisplayName(GetScoreboardDisplayNameEvent event) {
		event.setDisplayName(DEFAULT_OBJECTIVE_NAME);
	}
	
	@Subscribe
	public void onSetScoreboardDisplayName(SetScoreboardDisplayNameEvent event) {
		objective.setDisplayName(event.getDisplayName());
	}
	
	@Subscribe
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		Game game = event.getGame();
		
		if (scoreboard == null) {
			return;
		}
		
		Team team = scoreboard.getTeam(player.getName());
				
		if (game.getGameState() == GameState.INGAME) {
			team.setPrefix(IS_DEAD_SYMBOL);
		} else {
			team.unregister();
		}
		
		//Note: Scoreboard restoring is managed by the PlayerState
		SpleefPlayer killer = event.getKiller();
		if (killer != null && playersTracked.contains(killer)) {
			Score killerScore = objective.getScore(killer.getName());
			int previousScore = killerScore.getScore();
			
			killerScore.setScore(++previousScore);
		}
		
		Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
		player.getBukkitPlayer().setScoreboard(mainBoard);
	}
	
	@Subscribe
	public void onGameEnd(GameEndEvent event) {
		//Remove that reference
		scoreboard = null;
		playersTracked.clear();
	}
	
	public static class GetScoreboardDisplayNameEvent extends Event {
		
		private String displayName;
		
		public String getDisplayName() {
			return displayName;
		}
		
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		
	}
	
	public static class SetScoreboardDisplayNameEvent extends Event {
		
		private String displayName;
		
		public String getDisplayName() {
			return displayName;
		}
		
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		
	}

}