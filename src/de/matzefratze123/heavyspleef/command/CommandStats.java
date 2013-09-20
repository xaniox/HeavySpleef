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

import static org.bukkit.ChatColor.*;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.stats.StatisticManager;
import de.matzefratze123.heavyspleef.stats.StatisticModule;
import de.matzefratze123.heavyspleef.util.Permissions;

public class CommandStats extends HSCommand {

	public CommandStats() {
		setMaxArgs(2);
		setMinArgs(0);
		setOnlyIngame(true);
		setUsage("/spleef stats [player|top] [page]");
		setTabHelp(new String[]{"[player]", "[top]", "[top] [page]"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;

		if (args.length == 0) {
			if (!p.hasPermission(Permissions.STATS.getPerm())) {
				p.sendMessage(_("noPermission"));
				return;
			}
			if (!StatisticManager.hasStatistic(p.getName())) {
				p.sendMessage(ChatColor.RED + "You don't have statistics!");
				return;
			}

			StatisticModule stat = StatisticManager.getStatistic(p.getName(),
					false);
			printStatistics(stat, p);
		} else if (args.length >= 1 && args[0].equalsIgnoreCase("top")) {
			int page = 0;
			if (args.length == 2) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					p.sendMessage(_("notANumber", args[1]));
				}
			}

			String[] leaderBoard = StatisticManager.retrieveLeaderboard(page);
			p.sendMessage("--- " + ChatColor.GREEN + "Top Players"
					+ ChatColor.WHITE + " ---");
			for (String str : leaderBoard) {
				if (str == null)
					continue;
				p.sendMessage(str);
			}	
		} else if (args.length == 1) {
			if (!p.hasPermission(Permissions.STATS_OTHERS.getPerm())) {
				p.sendMessage(_("noPermission"));
				return;
			}

			OfflinePlayer target = Bukkit.getPlayer(args[0]);
			if (target == null)
				target = Bukkit.getOfflinePlayer(args[0]);
			if (target == null) {
				p.sendMessage(_("playerNotOnline"));
				return;
			}
			if (!StatisticManager.hasStatistic(args[0])) {
				p.sendMessage(ChatColor.RED
						+ "This player doesn't have statistics!");
				return;
			}

			StatisticModule stat = StatisticManager.getStatistic(
					target.getName(), false);
			printStatistics(stat, p);
		}
	}

	private void printStatistics(StatisticModule stat, Player p) {
		if (!stat.getName().equalsIgnoreCase(p.getName()))
			p.sendMessage("--- " + GREEN + "Statistics of " + stat.getName() + WHITE + " ---");
		else
			p.sendMessage("--- " + GREEN + "Your statistics" + WHITE + " ---");
		p.sendMessage(GREEN + "Wins: " + WHITE + stat.getWins());
		p.sendMessage(RED + "Loses: " + WHITE + stat.getLoses());
		p.sendMessage(GREEN + "Knockouts: " + WHITE + stat.getKnockouts());
		p.sendMessage(GREEN + "Games played: " + WHITE + stat.getGamesPlayed());
		p.sendMessage(GREEN + "Wins / Game: " + WHITE + stat.getKD());
		p.sendMessage(GREEN + "Score: " + WHITE + stat.getScore());
	}

}
