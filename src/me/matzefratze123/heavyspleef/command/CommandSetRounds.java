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

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetRounds extends HSCommand {

	public CommandSetRounds() {
		setMaxArgs(2);
		setMinArgs(2);
		setOnlyIngame(true);
		setPermission(Permissions.SET_ROUNDS.getPerm());
		setUsage("/spleef setrounds <name> <amount>");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		if (!GameManager.hasGame(args[0])) {
			p.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		
		if (!game.is1vs1()) {
			p.sendMessage(_("not1vs1", game.getName())); //TODO Message
			return;
		}
		
		try {
			int rounds = Integer.parseInt(args[1]);
			
			game.setRounds(rounds);
			p.sendMessage(_("roundsSet")); //TODO Message
		} catch (NumberFormatException e) {
			p.sendMessage(_("notANumber", args[1]));
		}
	}

}
