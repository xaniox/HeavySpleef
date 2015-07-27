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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.Inject;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.defaults.FlagScoreboard.GetScoreboardDisplayNameEvent;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam.PlayerSelectedTeamEvent;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam.TeamColor;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam.TeamScoreboardInitializeEvent;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "scoreboard", parent = FlagTeam.class)
public class FlagTeamScoreboard extends BaseFlag {

	private static final String DEFAULT_OBJECTIVE_NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "Team Spleef";
	
	@Inject
	private Game game;
	private Objective objective;
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the sidebar scoreboard for team games");
	}
	
	@Subscribe
	public void onPlayerSelectedTeam(PlayerSelectedTeamEvent event) {
		updateScoreboard();
	}
	
	@Subscribe
	public void onTeamScoreboardInitialize(TeamScoreboardInitializeEvent event) {
		FlagTeam flag = (FlagTeam) getParent();
		Scoreboard board = flag.getScoreboard();
		
		if (board == null) {
			return;
		}
		
		GetScoreboardDisplayNameEvent getDisplayNameEvent = new GetScoreboardDisplayNameEvent();
		game.getEventBus().callEvent(getDisplayNameEvent);
		String displayName = getDisplayNameEvent.getDisplayName();
		
		objective = board.getObjective(FlagTeam.OBJECTIVE_NAME);
		objective.setDisplayName(displayName);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	@Subscribe(priority = Priority.HIGH)
	public void onGameCountdown(GameCountdownEvent event) {
		updateScoreboard();
	}
	
	@Subscribe(priority = Priority.HIGH)
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		updateScoreboard();
	}
	
	@Subscribe(priority = Priority.LOW)
	public void onGetScoreboardDisplayName(GetScoreboardDisplayNameEvent event) {
		event.setDisplayName(DEFAULT_OBJECTIVE_NAME);
	}
	
	@Subscribe
	public void onSetScoreboardDisplayName(GetScoreboardDisplayNameEvent event) {
		objective.setDisplayName(event.getDisplayName());
	}
	
	private void updateScoreboard() {
		FlagTeam flag = (FlagTeam) getParent();
		Scoreboard board = flag.getScoreboard();
		
		if (board == null) {
			return;
		}
		
		Objective objective = board.getObjective(FlagTeam.OBJECTIVE_NAME);
		for (TeamColor color : flag.getValue()) {
			String localizedName = flag.getLocalizedColorName(color);
			board.resetScores(color.getChatColor() + localizedName);
		}
		
		Map<SpleefPlayer, TeamColor> teams = flag.getPlayers();
		Map<TeamColor, Integer> teamSizes = Maps.newHashMap();
		
		for (Entry<SpleefPlayer, TeamColor> entry : teams.entrySet()) {
			TeamColor color = entry.getValue();
			if (color == null) {
				continue;
			}
			
			if (!teamSizes.containsKey(color)) {
				teamSizes.put(color, 0);
			}
			
			int previous = teamSizes.get(color);
			teamSizes.put(color, ++previous);
		}
		
		for (Entry<TeamColor, Integer> entry : teamSizes.entrySet()) {
			String localizedName = flag.getLocalizedColorName(entry.getKey());
			objective.getScore(entry.getKey().getChatColor() + localizedName).setScore(entry.getValue());
		}
	}

}
