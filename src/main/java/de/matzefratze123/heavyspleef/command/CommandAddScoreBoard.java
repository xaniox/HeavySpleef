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

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.ScoreBoard;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandAddScoreBoard implements CommandListener {

	@Command(value = "addscoreboard", minArgs = 1, onlyIngame = true)
	@CommandPermissions(value = { Permissions.ADD_SCOREBOARD })
	@CommandHelp(usage = "/spleef addscoreboard <game>", description = "Creates a new block scoreboard (only 1vs1) in the direction you're currently looking")
	public void execute(Player player, Game game) {
		if (game == null) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}

		BlockFace face = getBlockFace(player.getLocation().getYaw());
		face = rotateBlockFaceLeft(face);

		int id = 0;
		while (game.getComponents().hasScoreBoard(id)) {
			id++;
		}

		ScoreBoard scoreboard = new ScoreBoard(id, player.getLocation(), face);
		scoreboard.generate('0', '0', '0', '0');

		game.getComponents().addScoreBoard(scoreboard);
		player.sendMessage(_("scoreBoardAdded"));
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
