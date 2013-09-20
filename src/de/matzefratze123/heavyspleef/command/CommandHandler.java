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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.LanguageHandler;

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
		
		Vector<String> cutArgs = new Vector<String>(Arrays.asList(args));
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
		if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission().getPerm()) && !sender.hasPermission("heavyspleef.*")) {
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
		//Add all commands
		addSubCommand("flag", new CommandFlag());
		addSubCommand("f", new CommandFlag());
		addSubCommand("create", new CommandCreate());
		addSubCommand("delete", new CommandDelete());
		addSubCommand("join", new CommandJoin());
		addSubCommand("leave", new CommandLeave());
		addSubCommand("start", new CommandStart());
		addSubCommand("addfloor", new CommandAddFloor());
		addSubCommand("addlayer", new CommandAddFloor());
		addSubCommand("removefloor", new CommandRemoveFloor());
		addSubCommand("removelayer", new CommandRemoveFloor());
		addSubCommand("addlose", new CommandAddLose());
		addSubCommand("removelose", new CommandRemoveLose());
		addSubCommand("help", new CommandHelp());
		addSubCommand("kick", new CommandKick());
		addSubCommand("disable", new CommandDisable());
		addSubCommand("enable", new CommandEnable());
		addSubCommand("save", new CommandSave());
		addSubCommand("stop", new CommandStop());
		addSubCommand("stats", new CommandStats());
		addSubCommand("update", new CommandUpdate());
		addSubCommand("addscoreboard", new CommandAddScoreBoard());
		addSubCommand("removescoreboard", new CommandRemoveScoreBoard());
		addSubCommand("addwall", new CommandAddWall());
		addSubCommand("removewall", new CommandRemoveWall());
		addSubCommand("reload", new CommandReload());
		addSubCommand("info", new CommandInfo());
		addSubCommand("list", new CommandList());
		addSubCommand("vote", new CommandVote());
		addSubCommand("ready", new CommandVote());
		addSubCommand("rename", new CommandRename());
		addSubCommand("addteam", new CommandAddTeam());
		addSubCommand("removeteam", new CommandRemoveTeam());
		addSubCommand("sethub", new CommandSetHub());
		addSubCommand("addportal", new CommandAddPortal());
		addSubCommand("removeportal", new CommandRemovePortal());
		addSubCommand("hub", new CommandHub());
		addSubCommand("teamflag", new CommandTeamFlag());
	}
	
	public static void setPluginInstance(HeavySpleef instance) {
		HSCommand.setPluginInstance(instance);
	}

	public static void setConfigInstance(HeavySpleef heavySpleef) {
		HSCommand.setFileConfiguration(heavySpleef.getConfig());
	}
}
