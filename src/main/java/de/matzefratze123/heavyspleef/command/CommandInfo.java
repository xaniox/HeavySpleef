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

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.core.region.LoseZone;
import de.matzefratze123.heavyspleef.util.Permissions;
import de.matzefratze123.heavyspleef.util.Util;

@UserType(Type.ADMIN)
public class CommandInfo extends HSCommand {

	public CommandInfo() {
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.INFO);
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
		if (game.getFlags().size() > 0) {
			player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Flags: " + ChatColor.BLUE + getFriendlyFlagInfo(game));
		}
		
		player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Floors:");
		for (IFloor floor : game.getComponents().getFloors()) {
			player.sendMessage(ChatColor.LIGHT_PURPLE + "# " + floor.asPlayerInfo());
		}
		
		player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Losezones:");
		for (LoseZone zone : game.getComponents().getLoseZones()) {
			player.sendMessage(ChatColor.YELLOW + "# " + zone.asInfo());
		}
	}
	
	private String getFriendlyFlagInfo(Game game) {
		Map<Flag<?>, Object> flags = game.getFlags();
		String[] info = new String[flags.size()];
		
		int i = 0;
		for (Flag<?> flag : flags.keySet()) {
			info[i] = flag.toInfo(flags.get(flag));
			i++;
		}
		
		return Util.toFriendlyString(info, ", ");
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef info <name>");
		help.addHelp("Prints out information about this game");
		
		return help;
	}

}
