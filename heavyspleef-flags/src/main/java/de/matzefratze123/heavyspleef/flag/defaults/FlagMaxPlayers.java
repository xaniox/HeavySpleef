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

import de.matzefratze123.heavyspleef.core.event.GameEventHandler;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent.JoinResult;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.flag.presets.IntegerFlag;

@Flag(name = "max-players")
public class FlagMaxPlayers extends IntegerFlag {
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the maximum count of players for a Spleef game");
	}
	
	@GameEventHandler
	public void onPlayerJoinGame(PlayerPreJoinGameEvent event) {
		int playersCount = event.getGame().getPlayers().size();
		if (playersCount >= getValue()) {
			event.setJoinResult(JoinResult.DENY);
			event.setMessage(getI18N().getVarString(Messages.Player.MAX_PLAYER_COUNT_REACHED)
					.setVariable("max", String.valueOf(getValue()))
					.toString());
		}
	}
	
}
