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

import de.matzefratze123.heavyspleef.core.event.GameCountdownEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.event.PlayerJoinGameEvent;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.ValidationException;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "min-players")
public class FlagMinPlayers extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the minimum count of players required to start a game");
	}
	
	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 1) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_MIN_PLAYERS));
		}
	}
	
	@Subscribe(priority = Priority.HIGH)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		if (!isStartAllowed(game)) {
			event.setStartGame(false);
		}
	}
	
	@Subscribe
	public void onGameCountdown(GameCountdownEvent event) {
		Game game = event.getGame();
		if (!isStartAllowed(game)) {
			game.broadcast(getI18N().getVarString(Messages.Broadcast.NEED_MIN_PLAYERS)
					.setVariable("min-players", String.valueOf(getValue()))
					.toString());
			event.setCancelled(true);
		}
	}
	
	private boolean isStartAllowed(Game game) {
		int playersNow = game.getPlayers().size();
		return playersNow >= getValue();
	}

}
