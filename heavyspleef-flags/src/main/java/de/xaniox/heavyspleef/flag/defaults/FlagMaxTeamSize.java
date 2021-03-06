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

import de.xaniox.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.xaniox.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.Game.JoinResult;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.flag.defaults.FlagTeam.GetMaxPlayersEvent;
import de.xaniox.heavyspleef.flag.defaults.FlagTeam.PlayerSelectTeamEvent;
import de.xaniox.heavyspleef.flag.defaults.FlagTeam.TeamColor;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;

import java.util.List;

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
	
	@Subscribe
	public void onGetMaxPlayersEvent(GetMaxPlayersEvent event) {
		event.setMaxPlayers(getValue());
	}

}