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

import de.matzefratze123.heavyspleef.core.event.PlayerLeftGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameState;
import de.matzefratze123.heavyspleef.core.game.QuitCause;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "autospectate", parent = FlagSpectate.class)
public class FlagAutoSpectate extends BaseFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Automatically makes players spectate upon losing in Spleef");
	}
	
	@Subscribe(priority = Priority.HIGH)
	public void onPlayerLeftGame(PlayerLeftGameEvent event) {
		FlagSpectate flag = (FlagSpectate) getParent();
		SpleefPlayer player = event.getPlayer();
		Game game = event.getGame();
		
		if ((game.getGameState() != GameState.INGAME && game.getGameState() != GameState.STARTING) || event.getCause() != QuitCause.LOSE) {
			//Only auto-spectate when ingame
			return;
		}
		
		if (flag.spectate(player, game)) {
			player.sendMessage(getI18N().getVarString(Messages.Player.PLAYER_SPECTATE)
					.setVariable("game", game.getName())
					.toString());
		}
	}

}
