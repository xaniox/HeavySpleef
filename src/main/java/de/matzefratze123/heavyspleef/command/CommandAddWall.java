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

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandAddWall implements CommandListener {

	@Command(value = "addwall", minArgs = 1, onlyIngame = true)
	@CommandPermissions(value = {Permissions.ADD_WALL})
	@CommandHelp(usage = "/spleef addwall <game>", description = "Adds a self updating wall to a game")
	public void execute(Player player, Game game) {
		if (game == null) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Selection selection = HeavySpleef.getInstance().getSelectionManager().getSelection(player);
		if (!selection.hasSelection()) {
			player.sendMessage(_("needSelection"));
			return;
		}
		
		if (selection.isTroughWorlds()) {
			player.sendMessage(_("selectionCantTroughWorlds"));
			return;
		}
		
		if (!validateSelection(selection)) {
			player.sendMessage(ChatColor.RED + "Invalid selection. Please select one row of signs.");
			return;
		}
		
		int id = 0;
		while (game.getComponents().hasSignWall(id)) {
			id++;
		}
		
		SignWall wall = new SignWall(id, selection.getFirst(), selection.getSecond());
		game.getComponents().addSignWall(wall);
		game.getComponents().updateWalls();
		player.sendMessage(_("signWallAdded"));
	}
	
	private boolean validateSelection(Selection selection) {
		if (selection.getFirst().getBlockY() != selection.getSecond().getBlockY()) {
			return false;
		}
		
		if (selection.getFirst().getBlockX() != selection.getSecond().getBlockX() &&
			selection.getFirst().getBlockZ() != selection.getSecond().getBlockZ()) {
			return false;
		}
		
		int minX = Math.min(selection.getFirst().getBlockX(), selection.getSecond().getBlockX());
		int maxX = Math.max(selection.getFirst().getBlockX(), selection.getSecond().getBlockX());
		int minZ = Math.min(selection.getFirst().getBlockZ(), selection.getSecond().getBlockZ());
		int maxZ = Math.max(selection.getFirst().getBlockZ(), selection.getSecond().getBlockZ());
		
		boolean validMaterial = true;
		
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				Block block = selection.getFirst().getWorld().getBlockAt(x, selection.getFirst().getBlockY(), z);
				
				if (!(block.getState() instanceof Sign)) {
					validMaterial = false;
				}
			}
		}
		
		return validMaterial;
	}
	
}
