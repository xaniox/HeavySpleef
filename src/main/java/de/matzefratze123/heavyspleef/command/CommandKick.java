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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandKick extends HSCommand {

	public CommandKick() {
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.KICK);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		SpleefPlayer target = HeavySpleef.getInstance().getSpleefPlayer(args[0]);
		
		if (target == null) {
			player.sendMessage(_("playerNotOnline"));
			return;
		}
		
		if (!target.isActive()) {
			player.sendMessage(_("playerIsntInAnyGame"));
			return;
		}
		
		String reasonMessage = args.length > 1 ? " for " : "";
		StringBuilder reasonBuilder = new StringBuilder();
		for (int i = 1; i < args.length; i++)
			reasonBuilder.append(args[i]).append(" ");
		reasonMessage += reasonBuilder.toString();
		
		Game game = target.getGame();
		game.leave(target, LoseCause.KICK);
		target.sendMessage(_("kickedOfToPlayer", player.getName(), reasonMessage));
		player.sendMessage(_("kickedOfToKicker", target.getName(), game.getName(), reasonMessage));
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef kick <Player> [Reason]");
		help.addHelp("Kicks a player from a game");
		
		return help;
	}

}
