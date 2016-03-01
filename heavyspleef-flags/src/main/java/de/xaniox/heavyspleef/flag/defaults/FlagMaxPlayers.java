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

import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.DefaultConfig;
import de.xaniox.heavyspleef.core.config.GeneralSection;
import de.xaniox.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.xaniox.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.Game.JoinResult;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;

import java.util.List;

@Flag(name = "max-players")
public class FlagMaxPlayers extends IntegerFlag {
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the maximum count of players for a Spleef game");
	}
	
	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 1) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_MAX_PLAYERS));
		}
	}
	
	@Subscribe
	public void onPlayerJoinGame(PlayerPreJoinGameEvent event) {
		DefaultConfig config = getHeavySpleef().getConfiguration(ConfigType.DEFAULT_CONFIG);
		GeneralSection section = config.getGeneralSection();
		
		SpleefPlayer player = event.getPlayer();
		
		int playersCount = event.getGame().getPlayers().size();
		if (playersCount < getValue() || (section.isVipJoinFull() && player.isVip())) {
			return;
		}
		
		event.setJoinResult(JoinResult.TEMPORARY_DENY);
		event.setMessage(getI18N().getVarString(Messages.Player.MAX_PLAYER_COUNT_REACHED)
				.setVariable("max", String.valueOf(getValue()))
				.toString());
	}
	
	@Subscribe
	public void onPlayerLeave(PlayerLeaveGameEvent event) {
		Game game = event.getGame();
		
		if (game.getGameState() != GameState.LOBBY) {
			return;
		}
		
		if (game.getPlayers().size() >= getValue()) {
			return;
		}
		
		//Flush queue
		game.flushQueue();
	}
	
}