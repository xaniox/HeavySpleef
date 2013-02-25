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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
	
	public static Map<String, HSCommand> commands = new HashMap<String, HSCommand>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "HeavySpleef" + ChatColor.RESET + "" + ChatColor.GOLD + " - made by matzefratze123 [v" + ChatColor.RED + HeavySpleef.instance.getDescription().getVersion() + ChatColor.GRAY + "]");
			sender.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GOLD + "/spleef help for help");
			return true;
		}
		
		HSCommand command = getSubCommand(args[0]);
		if (command == null) {
			sender.sendMessage(ChatColor.RED + LanguageHandler._("unknownCommand"));
			return true;
		}
		
		Vector<String> cutArgs = new Vector<String>();
		cutArgs.addAll(Arrays.asList(args));
		cutArgs.remove(0);
		
		if (!isValidSubCommand(sender, command, cutArgs.toArray(new String[cutArgs.size()])))
			return true;
		command.execute(sender, cutArgs.toArray(new String[cutArgs.size()]));
		return true;
	}

	public static boolean isValidSubCommand(CommandSender sender, HSCommand cmd, String[] args) {
		if (cmd.onlyIngame() && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + LanguageHandler._("onlyIngame"));
			return false;
		}
		if (!cmd.getPermission().equalsIgnoreCase("") && !sender.hasPermission(cmd.getPermission())) {
			sender.sendMessage(ChatColor.RED + LanguageHandler._("noPermission"));
			return false;
		}
		if (args.length >= cmd.getMinArg() && (args.length <= cmd.getMaxArg() || cmd.getMaxArg() == -1))
			return true;
		else
			sender.sendMessage(ChatColor.RED + cmd.getUsage());
		return false;
	}
	
	public static void addSubCommand(String name, HSCommand cmd) {
		commands.put(name, cmd);
	}
	
	public static HSCommand getSubCommand(String name) {
		return commands.get(name.toLowerCase());
	}
	
	public static void initCommands() {
		addSubCommand("create", new CommandCreate());
		addSubCommand("delete", new CommandDelete());
		addSubCommand("join", new CommandJoin());
		addSubCommand("leave", new CommandLeave());
		addSubCommand("start", new CommandStart());
		addSubCommand("addfloor", new CommandAddFloor());
		addSubCommand("addlayer", new CommandAddFloor());
		addSubCommand("removefloor", new CommandRemoveFloor());
		addSubCommand("removelayer", new CommandRemoveFloor());
		addSubCommand("setlose", new CommandSetLose());
		addSubCommand("addlose", new CommandAddLose());
		addSubCommand("setwin", new CommandSetWin());
		addSubCommand("setlobby", new CommandSetLobby());
		addSubCommand("removelose", new CommandRemoveLose());
		addSubCommand("help", new CommandHelp());
		addSubCommand("setminplayers", new CommandMinPlayers());
		addSubCommand("kick", new CommandKick());
		addSubCommand("setmoney", new CommandSetMoney());
		addSubCommand("disable", new CommandDisable());
		addSubCommand("enable", new CommandEnable());
		addSubCommand("save", new CommandSave());
		addSubCommand("stop", new CommandStop());
		addSubCommand("setshovel", new CommandSetShovel());
		addSubCommand("setcountdown", new CommandSetCountdown());
		addSubCommand("startonminplayers", new CommandStartOnReachMinimum());
		addSubCommand("stats", new CommandStats());
	}
	
	public static void setPluginInstance(HeavySpleef instance) {
		HSCommand.setPluginInstance(instance);
	}

	public static void setConfigInstance(HeavySpleef heavySpleef) {
		HSCommand.setFileConfiguration(heavySpleef.getConfig());
	}
}
