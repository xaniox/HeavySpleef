/*
 * This file is part of addons.
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
package de.matzefratze123.joingui;

import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.JoinRequester;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class JoinInventory extends GameInventory {
	
	public JoinInventory(BasicAddOn addOn, InventoryEntryConfig config) {
		super(addOn, config);
	}

    @Override
    protected void onGameClicked(Game game, SpleefPlayer player) {
        try {
            long timer = game.getJoinRequester().request(player, JoinRequester.QUEUE_PLAYER_CALLBACK);
            if (timer > 0) {
                player.sendMessage(getI18n().getVarString(de.matzefratze123.heavyspleef.core.i18n.Messages.Command.JOIN_TIMER_STARTED)
                        .setVariable("timer", String.valueOf(timer))
                        .toString());
            }
        } catch (JoinRequester.JoinValidationException e) {
            player.sendMessage(e.getMessage());
        }
    }

}
