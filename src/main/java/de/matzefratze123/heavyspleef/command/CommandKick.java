/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.command;

import static de.matzefratze123.heavyspleef.util.I18N._;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.api.command.Command;
import de.matzefratze123.api.command.CommandHelp;
import de.matzefratze123.api.command.CommandListener;
import de.matzefratze123.api.command.CommandPermissions;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandKick implements CommandListener {
	
	@Command(value = "kick", minArgs = 1, onlyIngame = true)
	@CommandPermissions(value = {Permissions.KICK})
	@CommandHelp(usage = "/spleef kick <Player> [Reason]", description = "Kicks a player from a game")
	public void execute(CommandSender sender, Player player, String[] reason) {
		SpleefPlayer target = HeavySpleef.getInstance().getSpleefPlayer(player);
		
		if (target == null) {
			sender.sendMessage(_("playerNotOnline"));
			return;
		}
		
		if (!target.isActive()) {
			sender.sendMessage(_("playerIsntInAnyGame"));
			return;
		}
		
		String reasonMessage = "";
		
		if (reason != null) {
			reasonMessage = " for ";
			StringBuilder reasonBuilder = new StringBuilder();
			for (int i = 0; i < reason.length; i++) {
				reasonBuilder.append(reason[i]);
				
				if (i + 1 < reason.length) {
					reasonBuilder.append(" ");
				}
			}
			
			reasonMessage += reasonBuilder.toString();
		}
		
		Game game = target.getGame();
		game.leave(target, LoseCause.KICK);
		target.sendMessage(_("kickedOfToPlayer", sender.getName(), reasonMessage));
		sender.sendMessage(_("kickedOfToKicker", target.getName(), game.getName(), reasonMessage));
	}

}
