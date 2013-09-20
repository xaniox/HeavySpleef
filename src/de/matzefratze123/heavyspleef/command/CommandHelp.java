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


import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.util.Permissions;

public class CommandHelp extends HSCommand {

	private String[] firstPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 1/6  -----   ",
											   ChatColor.GOLD + "/spleef create <arena> <cuboid|cylinder <radius> <height>>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Creates a new spleefarena with the given selection or the given radius and height",
			  								   ChatColor.GOLD + "/spleef delete <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Deletes a spleefarena with the given name",
			  								   ChatColor.GOLD + "/spleef addfloor <randomwool|given|block[:subdata]>]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Adds a floor to the arena where you are standing / your selection is",
			  								   ChatColor.GOLD + "/spleef removefloor" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes the floor you are currently looking"};
	
	private String[] secondPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 2/6  -----   ",
											    ChatColor.GOLD + "/spleef addlose <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Adds a losezone for the specified arena with the given selection",
												ChatColor.GOLD + "/spleef removelose <arena> <id>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes a losezone with the given id in the arena",
												ChatColor.GOLD + "/spleef flag <arena> <flag> <value>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets a flag for a game",
												ChatColor.GOLD + "/spleef info <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows information about a game (name, type, flags etc.)"};
	
	private String[] thirdPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 3/6  -----   ",
											   ChatColor.GOLD + "/spleef start <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Starts the game with the given name",
											   ChatColor.GOLD + "/spleef join <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Joins a spleef game with the given name",
											   ChatColor.GOLD + "/spleef leave" + ChatColor.RED + " - " + ChatColor.YELLOW + "Leaves a spleef game if you are active or leaves the queue.",
											   ChatColor.GOLD + "/spleef reload" + ChatColor.RED + " - " + ChatColor.YELLOW + "Reloads the plugin."};
	
	private String[] fourthPage = new String[]{ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 4/6  -----   ",
			   								   ChatColor.GOLD + "/spleef kick <Player> [reason]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Kicks a player out of a game",
			   								   ChatColor.GOLD + "/spleef disable <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Disabled a spleef game",
			   								   ChatColor.GOLD + "/spleef enable <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Enables a disabled spleef game",
			   								   ChatColor.GOLD + "/spleef save" + ChatColor.RED + " - " + ChatColor.GOLD + "Saves all games and statistics to the database"};
	
	private String[] fifthPage = new String[]{ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 5/6  -----   ",
											  ChatColor.GOLD + "/spleef stats [player]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the current spleef stats of themself or another player",
											  ChatColor.GOLD + "/spleef stats top [page]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the top spleef players!",
											  ChatColor.GOLD + "/spleef addwall <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Adds a signwall to the game",
											  ChatColor.GOLD + "/spleef removewall <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes the wall where you currently looking."};
	
	private String[] sixthPage = new String[]{ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 6/6  -----   ",
											  ChatColor.GOLD + "/spleef addscoreboard <arena> <direction>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Adds a scoreboard with the size of 19x7 blocks left-under you",
											  ChatColor.GOLD + "/spleef removescoreboard" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes the scoreboard where you currently look",
											  ChatColor.GOLD + "/spleef update" + ChatColor.RED + " - " + ChatColor.YELLOW + "Auto-updates the plugin if there is a new version avaible"};
		
	private String[] userHelp = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help   -----   ",
											  ChatColor.GOLD + "/spleef start <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Starts the game with the given name",
											  ChatColor.GOLD + "/spleef vote" + ChatColor.RED + " - " + ChatColor.YELLOW + "Votes to start a game",
											  ChatColor.GOLD + "/spleef join [arena]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Joins a spleef game with the given name",
											  ChatColor.GOLD + "/spleef leave <arena>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Leaves a spleef game",
											  ChatColor.GOLD + "/spleef list" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows ingame players or the game list.",
											  ChatColor.GOLD + "/spleef stats [player]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the current spleef stats of themself or another player",
											  ChatColor.GOLD + "/spleef stats top [page]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the top spleef players!"};
	
	public CommandHelp() {
		setMaxArgs(1);
		setMinArgs(0);
		setUsage("/spleef help [page]");
		setTabHelp(new String[]{"[page]"});
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender.hasPermission(Permissions.HELP_ADMIN.getPerm())) {
			if (args.length != 1) {
				sender.sendMessage(firstPage);
				return;
			}
			int page;
			try {
				page = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				sender.sendMessage(firstPage);
				return;
			}
			switch(page) {
			case 1:
				sender.sendMessage(firstPage);
				break;
			case 2:
				sender.sendMessage(secondPage);
				break;
			case 3:
				sender.sendMessage(thirdPage);
				break;
			case 4:
				sender.sendMessage(fourthPage);
				break;
			case 5:
				sender.sendMessage(fifthPage);
				break;
			case 6:
				sender.sendMessage(sixthPage);
				break;
			default:
				sender.sendMessage(_("pageDoesntExists"));
			}
		} else if (sender.hasPermission(Permissions.HELP_USER.getPerm())) {
			sender.sendMessage(userHelp);
		} else sender.sendMessage(_("noPermission"));
	}

}
