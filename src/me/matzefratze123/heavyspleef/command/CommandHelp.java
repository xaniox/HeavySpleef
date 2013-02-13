package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHelp extends HSCommand {

	private String[] firstPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 1/4  -----   ",
											   ChatColor.GOLD + "/spleef create <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Creates a new spleefarena with the given selection",
			  								   ChatColor.GOLD + "/spleef delete <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Deletes a spleefarena with the given name",
			  								   ChatColor.GOLD + "/spleef addfloor <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Adds a floor to the arena with the given selection",
			  								   ChatColor.GOLD + "/spleef removefloor <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes the floor you are currently looking"};
	
	private String[] secondPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 2/4  -----   ",
											    ChatColor.GOLD + "/spleef addlose <name> - " + ChatColor.YELLOW + "Adds a losezone for the specified arena",
												ChatColor.GOLD + "/spleef removelose" + ChatColor.RED + " - " + ChatColor.YELLOW + "Removes the losezone you are currently looking",
												ChatColor.GOLD + "/spleef setlose <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the losepoint where a player should be teleported if he lose",
												ChatColor.GOLD + "/spleef setwin <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Sets the winpoint where the winner will be teleported at win"};
	
	private String[] thirdPage = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 3/4  -----   ",
											   ChatColor.GOLD + "/spleef setlobby <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Set's the pregamepoint when a player join's a game",
											   ChatColor.GOLD + "/spleef start <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Starts the game with the given name",
											   ChatColor.GOLD + "/spleef join <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Joins a spleef game with the given name",
											   ChatColor.GOLD + "/spleef leave <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Leaves a spleef game"};
	
	private String[] fourthPage = new String[]{ChatColor.DARK_BLUE + "   -----   HeavySpleef Help - Page 4/4  -----   ",
			   								   ChatColor.GOLD + "/spleef kick <Player>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Kicks a player out of a game",
			   								   ChatColor.GOLD + "/spleef disable <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Disabled a spleef game",
			   								   ChatColor.GOLD + "/spleef enable <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Enables a disabled spleef game",
			   								   ChatColor.GOLD + "/spleef setminplayers <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Set's the minimum size of players that are needed to start the game",
			   								   ChatColor.GOLD + "/spleef setmoney <name> <amount>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Set's the money that every player has to pay for the game",
			   								   ChatColor.GOLD + "/spleef save" + ChatColor.RED + " - " + ChatColor.GOLD + "Saves all games to the database"};
	
	private String[] userHelp = new String[] {ChatColor.DARK_BLUE + "   -----   HeavySpleef Help   -----   ",
											  ChatColor.GOLD + "/spleef start <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Starts the game with the given name",
											  ChatColor.GOLD + "/spleef join <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Joins a spleef game with the given name",
											  ChatColor.GOLD + "/spleef leave <name>" + ChatColor.RED + " - " + ChatColor.YELLOW + "Leaves a spleef game"};
	
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
			default:
				sender.sendMessage(_("pageDoesntExists"));
			}
		} else if (sender.hasPermission(Permissions.HELP_USER.getPerm())) {
			sender.sendMessage(userHelp);
		} else sender.sendMessage(_("noPermission"));
	}

}
