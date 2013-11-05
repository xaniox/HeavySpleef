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

import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.ScoreBoard;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandAddScoreBoard extends HSCommand {

	public CommandAddScoreBoard() {
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_SCOREBOARD);
		setUsage("/spleef addscoreboard <name>");
		setHelp("Adds a scoreboard to the game (only 1vs1) with the direction you are currently looking");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		BlockFace face = getBlockFace(player.getLocation().getYaw());
		face = rotateBlockFaceLeft(face);
		
		int id = 0;
		while (game.getComponents().hasScoreBoard(id)) {
			id++;
		}
		
		ScoreBoard scoreboard = new ScoreBoard(id, player.getLocation(), face);
		scoreboard.generate('0', '0', '0', '0');
		
		game.getComponents().addScoreBoard(scoreboard);
		
	}
	
	private BlockFace getBlockFace(float yaw) {
		if (yaw < 45 && yaw > -45) {
			return BlockFace.SOUTH;
		}
		
		if (yaw < -45 && yaw > -135) {
			return BlockFace.EAST;
		}
		
		if (yaw < -135 || yaw > 135) {
			return BlockFace.NORTH;
		}
		
		if (yaw < 135 && yaw > 45) {
			return BlockFace.WEST;
		}
		
		return BlockFace.SOUTH;
	}
	
	private BlockFace rotateBlockFaceLeft(BlockFace face) {
		if (face == BlockFace.SOUTH) {
			return BlockFace.EAST;
		}
		
		if (face == BlockFace.EAST) {
			return BlockFace.NORTH;
		}
		
		if (face == BlockFace.NORTH) {
			return BlockFace.WEST;
		}
		
		if (face == BlockFace.WEST) {
			return BlockFace.SOUTH;
		}
		
		return face;
	}

}
