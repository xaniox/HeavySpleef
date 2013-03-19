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

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.ScoreBoard;
import me.matzefratze123.heavyspleef.utility.Permissions;

public class CommandRemoveScoreBoard extends HSCommand {

	public CommandRemoveScoreBoard() {
		setMaxArgs(0);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.REMOVE_SCOREBOARD.getPerm());
		setUsage("/spleef removescoreboard");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		Block targetBlock = p.getTargetBlock(null, 100);
		
		int id = -1;
		
		for (Game game : GameManager.getGames()) {
			for (ScoreBoard board : game.getScoreBoards()) {
				if (board.contains(targetBlock.getLocation())) {
					id = board.getId();
					
					board.remove();
					game.removeScoreBoard(id);
					p.sendMessage(_("scoreBoardRemoved"));
					return;
				}
			}
		}
		
	} 
	
}
