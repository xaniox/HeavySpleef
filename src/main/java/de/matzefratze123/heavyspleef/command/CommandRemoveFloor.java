/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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


import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.core.GameType;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandRemoveFloor extends HSCommand {

	public CommandRemoveFloor() {
		setOnlyIngame(true);
		setPermission(Permissions.REMOVE_FLOOR);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Block block = player.getTargetBlock(null, 50);
		if (block == null) {
			player.sendMessage(_("notLookingAtABlock"));
			return;
		}
		
		for (Game game : GameManager.getGames()) {
			if (game.getType() == GameType.CYLINDER && !HookManager.getInstance().getService(WorldEditHook.class).hasHook())
				continue;
			for (IFloor floor : game.getComponents().getFloors()) {
				if (floor.contains(block.getLocation())) {
					int id = floor.getId();
					game.getComponents().removeFloor(id);
					player.sendMessage(_("floorRemoved"));
					return;
				}
			}
		}
		
		player.sendMessage(_("notLookingAtFloor"));
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef removefloor");
		help.addHelp("Removes a floor from a game where you currently looking");
		
		return help;
	}

}
