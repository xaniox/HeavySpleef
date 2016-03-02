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

import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.ValidationException;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;

import java.util.List;

@Flag(name = "autostart")
public class FlagAutostart extends IntegerFlag {

	@Override
	public void getDescription(List<String> description) {
		description.add("Defines the count of players needed to automatically start the game");
	}
	
	@Override
	public void validateInput(Integer input) throws ValidationException {
		if (input <= 1) {
			throw new ValidationException(getI18N().getString(Messages.Command.INVALID_AUTOSTART));
		}
	}
	
	@Subscribe
	public void onPlayerJoin(PlayerJoinGameEvent event) {
		Game game = event.getGame();
		
		int playersNow = game.getPlayers().size();
		if (playersNow >= getValue() && !game.getGameState().isGameActive()) {
			event.setStartGame(true);
		}
	}

    @Subscribe(priority = Subscribe.Priority.HIGH)
    public void onBossbarUpdate(FlagBossbar.BossbarUpdateEvent event) {
        Event trigger = event.getTrigger();
        if (!(trigger instanceof GameEvent)) {
            return;
        }

        GameEvent gameEvent = (GameEvent) trigger;
        Game game = gameEvent.getGame();
        if (game.getGameState() != GameState.LOBBY) {
            return;
        }

        if (trigger instanceof PlayerJoinGameEvent || trigger instanceof PlayerLeaveGameEvent) {
            if (trigger instanceof PlayerJoinGameEvent && ((PlayerJoinGameEvent)trigger).getStartGame()) {
                return;
            }

            int stillNeeded = getValue() - game.getPlayers().size() - (trigger instanceof PlayerLeaveGameEvent ? 1 : 0);
            event.setPermMessage(getI18N().getVarString(Messages.Broadcast.BOSSBAR_PLAYERS_NEEDED)
                    .setVariable("needed", String.valueOf(stillNeeded))
                    .toString());
        }
    }
	
}