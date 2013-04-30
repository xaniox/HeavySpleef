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
package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.region.HUBPortal;
import me.matzefratze123.heavyspleef.util.Permissions;
import me.matzefratze123.heavyspleef.util.Util;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRemovePortal extends HSCommand {
	
	public CommandRemovePortal() {
		setPermission(Permissions.REMOVE_PORTAL);
		setUsage("/spleef removeportal");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		Block targetBlock = player.getTargetBlock(Util.getTransparentMaterials(), 100);
		
		HUBPortal port = null;
		for (HUBPortal portal : GameManager.getPortals()) {
			if (portal.contains(targetBlock)) {
				port = portal;
			}
		}
		
		if (port == null) {
			player.sendMessage(_("notLookingAtPortal"));
			return;
		}
		
		GameManager.removePortal(port);
		player.sendMessage(_("portalRemoved"));
	}

}
