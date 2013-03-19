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

import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHelp extends HSCommand {

	private String[] firstPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 1/5  -----   ",
											   ChatColor.GOLD + "/spleef create <name> <cuboid|cylinder <radius> <height>>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Creates a new spleefarena with the given selection or the given radius and height",
			  								   ChatColor.GOLD + "/spleef delete <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Deletes a spleefarena with the given name",
			  								   ChatColor.GOLD + "/spleef addfloor <randomwool|given|block[:subdata]>]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Adds a floor to the arena where you are standing / your selection is",
			  								   ChatColor.GOLD + "/spleef removefloor" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes the floor you are currently looking"};
	
	private String[] secondPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 2/5  -----   ",
											    ChatColor.GOLD + "/spleef addlose <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Adds a losezone for the specified arena with the given selection",
												ChatColor.GOLD + "/spleef removelose <name> <id>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes a losezone with the given id in the arena",
												ChatColor.GOLD + "/spleef setlose <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the losepoint where a player should be teleported if he lose",
												ChatColor.GOLD + "/spleef setwin <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the winpoint where the winner will be teleported at win"};
	
	private String[] thirdPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 3/5  -----   ",
											   ChatColor.GOLD + "/spleef setlobby <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Set's the spawnpoint when a player join's a game",
											   ChatColor.GOLD + "/spleef start <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Starts the game with the given name",
											   ChatColor.GOLD + "/spleef join <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Joins a spleef game with the given name",
											   ChatColor.GOLD + "/spleef leave" + ChatColor.RED + " - " + ChatColor.YELLOW + "Leaves a spleef game if you are active or leaves the queue."};
	
	private String[] fourthPage = new String[]{ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 4/5  -----   ",
			   								   ChatColor.GOLD + "/spleef kick <Player> [reason]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Kicks a player out of a game",
			   								   ChatColor.GOLD + "/spleef disable <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Disabled a spleef game",
			   								   ChatColor.GOLD + "/spleef enable <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Enables a disabled spleef game",
			   								   ChatColor.GOLD + "/spleef setminplayers <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Set's the minimum amount of players needed to start the game",
			   								   ChatColor.GOLD + "/spleef setmaxplayers <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Set's the maximum amount of players needed to start the game",
			   								   ChatColor.GOLD + "/spleef setjackpot <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Set's the money that every player has to pay for the game (0 to disable)",
			   								   ChatColor.GOLD + "/spleef save" + ChatColor.RED + " - " + ChatColor.GOLD + "Saves all games and statistics to the database"};
	
	private String[] fifthPage = new String[]{ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 5/5  -----   ",
											  ChatColor.GOLD + "/spleef setcountdown <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the countdown for a game",
											  ChatColor.GOLD + "/spleef setshovel <name> [true|false]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the game with shovels",
											  ChatColor.GOLD + "/spleef autostart <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the autostart of a game if the amount of given players is reached",
											  ChatColor.GOLD + "/spleef stats [player]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the current spleef stats of themself or another player",
											  ChatColor.GOLD + "/spleef stats top [page]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the top spleef players!",
											  ChatColor.GOLD + "/spleef setchances <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the value of chances that every player has.",
											  ChatColor.GOLD + "/spleef set1vs1 <name> [true|false]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the value if this game should be a 1vs1 arena.",
											  ChatColor.GOLD + "/spleef setrounds <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the round for a 1vs1 game. Only works if the game is 1vs1!"};
	
	private String[] userHelp = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help   -----   ",
											  ChatColor.GOLD + "/spleef start <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Starts the game with the given name",
											  ChatColor.GOLD + "/spleef join <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Joins a spleef game with the given name",
											  ChatColor.GOLD + "/spleef leave <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Leaves a spleef game",
											  ChatColor.GOLD + "/spleef stats [player]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the current spleef stats of themself or another player",
											  ChatColor.GOLD + "/spleef stats top [page]" + ChatColor.RED + " - " + ChatColor.YELLOW + "Shows the top spleef players!"};
	
	public CommandHelp() {
		setMaxArgs(1);
		setMinArgs(0);
		setUsage("/spleef help [page]");
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
			default:
				sender.sendMessage(_("pageDoesntExists"));
			}
		} else if (sender.hasPermission(Permissions.HELP_USER.getPerm())) {
			sender.sendMessage(userHelp);
		} else sender.sendMessage(_("noPermission"));
	}

}
