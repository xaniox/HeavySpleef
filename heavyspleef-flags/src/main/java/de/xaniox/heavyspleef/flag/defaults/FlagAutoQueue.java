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

import de.xaniox.heavyspleef.core.event.PlayerLeftGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.QuitCause;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;

import java.util.List;

@Flag(name = "auto-queue")
public class FlagAutoQueue extends BaseFlag {

    @Override
    public void getDescription(List<String> description) {
        description.add("Automatically queues the player for the next game until they leave the queue explicitly");
    }

    @Subscribe(priority = Subscribe.Priority.LOW)
    public void onPlayerLeftGame(PlayerLeftGameEvent event) {
        if (event.getCause() == QuitCause.SELF) {
            return;
        }

        SpleefPlayer player = event.getPlayer();
        Game game = event.getGame();

        //Queue the player
        boolean success = game.queue(player);

        if (success) {
            player.sendMessage(getI18N().getVarString(Messages.Command.ADDED_TO_QUEUE)
                    .setVariable("game", game.getName())
                    .toString());
        } else {
            player.sendMessage(getI18N().getString(Messages.Command.COULD_NOT_ADD_TO_QUEUE));
        }
    }

}
