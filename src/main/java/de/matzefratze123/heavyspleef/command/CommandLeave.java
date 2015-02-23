/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.command;

import static de.matzefratze123.heavyspleef.util.I18N._;

import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandAliases;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.core.QueuesManager;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandLeave implements CommandListener {

	@Command(value = "leave", onlyIngame = true)
	@CommandPermissions(value = { Permissions.LEAVE_GAME })
	@CommandHelp(usage = "/spleef leave", description = "Leaves the game/queue/spectate mode")
	@CommandAliases({ "quit" })
	public void execute(Player player) {
		leave(HeavySpleef.getInstance().getSpleefPlayer(player));
	}

	public static void leave(SpleefPlayer player) {
		if (player.isSpectating()) {
			player.getGame().leaveSpectate(player);
			return;
		}

		if (!player.isActive()) {
			if (!QueuesManager.hasQueue(player)) {
				player.sendMessage(_("notInQueue"));
				return;
			}

			QueuesManager.removeFromQueue(player);
			return;
		}

		Game game = player.getGame();

		game.leave(player, LoseCause.LEAVE);
		player.sendMessage(_("left"));
	}

}
