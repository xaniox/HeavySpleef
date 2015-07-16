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

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.Game.JoinResult;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.ValidationException;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam.PlayerSelectTeamEvent;
import de.matzefratze123.heavyspleef.flag.defaults.FlagTeam.TeamColor;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "max-players", parent = FlagTeam.class)
public class FlagMaxTeamSize extends IntegerFlag {

	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 1) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_TEAM_MAX_SIZE));
		}
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Sets the count of maximum players which are allowed to join a team");
	}
	
	@Subscribe
	public void onPreGameJoin(PlayerPreJoinGameEvent event) {
		FlagTeam flagTeam = (FlagTeam) getParent();
		int maxSize = getValue();
		int sizeTeams = flagTeam.size();
		int players = flagTeam.getPlayers().size();
		
		if (players >= maxSize * sizeTeams) {
			event.setJoinResult(JoinResult.TEMPORARY_DENY);
			event.setMessage(getI18N().getString(Messages.Player.TEAM_MAX_PLAYER_COUNT_REACHED));
			return;
		}
	}
	
	@Subscribe
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		Game game = event.getGame();
		
		if (game.getGameState() != GameState.LOBBY) {
			return;
		}
		
		FlagTeam flagTeam = (FlagTeam) getParent();
		int maxSize = getValue();
		int sizeTeams = flagTeam.size();
		int players = flagTeam.getPlayers().size();
		
		if (players >= maxSize * sizeTeams) {
			return;
		}
		
		//Flush queue
		game.flushQueue();
	}
	
	@Subscribe
	public void onPlayerSelectTeam(PlayerSelectTeamEvent event) {
		TeamColor color = event.getColorSelected();
		
		FlagTeam flagTeam = (FlagTeam) getParent();
		int maxSize = getValue();
		int teamSize = flagTeam.sizeOf(color);
		
		if (teamSize < maxSize) {
			return;
		}
		
		event.setCancelled(true);
		event.setFailMessage(getI18N().getVarString(Messages.Player.MAX_PLAYERS_IN_TEAM_REACHED)
				.setVariable("color", color.getChatColor() + flagTeam.getLocalizedColorName(color))
				.toString());
	}

}
