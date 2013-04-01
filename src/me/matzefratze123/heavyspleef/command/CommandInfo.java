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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.flag.Flag;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInfo extends HSCommand {

	public CommandInfo() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.INFO);
		setUsage("/spleef info <name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		
		player.sendMessage(ChatColor.YELLOW + "Name: " + game.getName() + ChatColor.GRAY + ", type: " + game.getType().name());
		if (game.getFlags().size() > 0)
			player.sendMessage(ChatColor.BLUE + "Flags: " + parseFlags(game));
		
	}
	
	private Set<String> parseFlags(Game game) {
		Map<Flag<?>, Object> flags = game.getFlags();
		Set<String> set = new HashSet<String>();
		
		for (Flag<?> flag : flags.keySet()) {
			set.add(flag.toInfo(flags.get(flag)));
		}
		
		return set;
	}

}
