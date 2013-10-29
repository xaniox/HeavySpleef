/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
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

import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.LoseCause;
import de.matzefratze123.heavyspleef.core.QueuesManager;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandLeave extends HSCommand {

	public CommandLeave() {
		setMaxArgs(0);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.LEAVE_GAME);
		setUsage("/spleef leave");
		setHelp("Leaves the game/queue/spectate mode");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		leave(player);
	}
	
	public static void leave(Player player) {
		if (GameManager.isSpectating(player)) {
			GameManager.fromPlayer(player).leaveSpectate(player);
			return;
		}
		
		if (!GameManager.isActive(player)) {
			if (!QueuesManager.hasQueue(player)) {
				player.sendMessage(_("notInQueue"));
				return;
			}
			
			QueuesManager.removeFromQueue(player);
			return;
		}
		Game game = GameManager.fromPlayer(player);
		
		game.leave(player, LoseCause.LEAVE);
		player.sendMessage(_("left"));
	}

}
