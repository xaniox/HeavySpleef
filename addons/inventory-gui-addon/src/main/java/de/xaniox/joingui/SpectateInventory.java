/*
 * This file is part of addons.
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
package de.xaniox.joingui;

import de.xaniox.heavyspleef.addon.java.BasicAddOn;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.defaults.FlagQueueLobby;
import de.xaniox.heavyspleef.flag.defaults.FlagSpectate;

public class SpectateInventory extends GameInventory {

    public SpectateInventory(BasicAddOn addOn, InventoryEntryConfig config) {
        super(addOn, config);
    }

    @Override
    protected void onGameClicked(Game game, SpleefPlayer player) {
        if (!game.isFlagPresent(FlagSpectate.class)) {
            player.sendMessage(getI18n().getString(Messages.Player.NO_SPECTATE_FLAG));
            return;
        }

        FlagSpectate spectateFlag = game.getFlag(FlagSpectate.class);
        for (Game otherGame : getHeavySpleef().getGameManager().getGames()) {
            if (!otherGame.isFlagPresent(FlagSpectate.class)) {
                continue;
            }

            FlagSpectate flag = otherGame.getFlag(FlagSpectate.class);
            if (!flag.isSpectating(player)) {
                continue;
            }

            flag.leave(player);
            player.sendMessage(getI18n().getVarString(Messages.Player.PLAYER_LEAVE_SPECTATE)
                    .setVariable("game", game.getName())
                    .toString());
            break;
        }

        if (game.isFlagPresent(FlagQueueLobby.class) && game.isQueued(player)) {
            getI18n().getString(Messages.Command.CANNOT_SPECTATE_IN_QUEUE_LOBBY);
            return;
        }

        boolean success = spectateFlag.spectate(player, game);
        if (success) {
            player.sendMessage(getI18n().getVarString(Messages.Player.PLAYER_SPECTATE)
                    .setVariable("game", game.getName())
                    .toString());
        }
    }

    @Override
    protected boolean canPlaceGame(Game game) {
        //Only place games which are currently flagged as spectatable
        return game.isFlagPresent(FlagSpectate.class);
    }
}