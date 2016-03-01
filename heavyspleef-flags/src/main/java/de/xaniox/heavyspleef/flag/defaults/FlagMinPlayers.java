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

import de.xaniox.heavyspleef.core.event.GameCountdownEvent;
import de.xaniox.heavyspleef.core.event.PlayerJoinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;

import java.util.List;

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
	
	@Subscribe(priority = Subscribe.Priority.HIGH)
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		if (!isStartAllowed(game)) {
			event.setStartGame(false);
			
			int stillNeeded = getValue() - game.getPlayers().size();
			game.broadcast(getI18N().getVarString(Messages.Broadcast.NEED_MORE_PLAYERS)
					.setVariable("amount", String.valueOf(stillNeeded))
					.toString());
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