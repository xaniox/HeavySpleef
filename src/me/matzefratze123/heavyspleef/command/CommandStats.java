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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import me.matzefratze123.heavyspleef.utility.Permissions;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticModule;
import me.matzefratze123.heavyspleef.utility.statistic.StatisticManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.*;

public class CommandStats extends HSCommand {

	public CommandStats() {
		setMaxArgs(2);
		setMinArgs(0);
		setOnlyIngame(true);
		setUsage("/spleef stats [Name|top] [page]");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		
		if (args.length == 0) {
			if (!p.hasPermission(Permissions.STATS.getPerm())) {
				p.sendMessage(_("noPermission"));
				return;
			}
			if (!StatisticManager.hasStatistic(p.getName())) {
				p.sendMessage(ChatColor.RED + "You don't have statistics!");
				return;
			}
			
			StatisticModule stat = StatisticManager.getStatistic(p.getName(), false);
			printStatistics(stat, p);
		} else if (args.length >= 1 && args[0].equalsIgnoreCase("top")) {
			
			SortedMap<Integer, StatisticModule> map = new TreeMap<Integer, StatisticModule>();
			List<Integer> bestScores = new ArrayList<Integer>();
			Collection<StatisticModule> stats = StatisticManager.getStatistics();
			
			for (StatisticModule stat : stats) {
				int decreaser = 0;
				while (map.containsKey(stat.getScore() - decreaser))
					decreaser++;
				map.put(stat.getScore() - decreaser, stat);
				bestScores.add(stat.getScore() - decreaser);
			}
			
			Collections.sort(bestScores);
			int page = 0;
			
			if (args.length > 1) {
				try {
					page = Integer.parseInt(args[1]) - 1;
				} catch (NumberFormatException nfe) {
					p.sendMessage(_("notANumber", args[1]));
					return;
				}
			}
			
			page = page * 10;
			for (int i = page; i <= page + 10; i++) {
				if (bestScores.size() - 1 - i < 0) {
					p.sendMessage(RED + "Error: No more listings!");
					return;
				}
				StatisticModule stat = map.get(bestScores.get(bestScores.size() - 1 - i));
				p.sendMessage(GOLD + "" + (i + 1) + ". " + GREEN + stat.getName() + GOLD + ": Wins: " + stat.getWins() + " Loses: " + stat.getLoses() + " Knockouts: " + stat.getKnockouts() + " Games: " + stat.getGamesPlayed() + " Score: " + stat.getScore());
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
				p.sendMessage(ChatColor.RED + "This player doesn't have statistics!");
				return;
			}
			
			StatisticModule stat = StatisticManager.getStatistic(target.getName(), false);
			printStatistics(stat, p);
		}
	}
	
	private void printStatistics(StatisticModule stat, Player p) {
		double winsPerGame = 0.0;
		if (stat.getWins() > 0 && stat.getGamesPlayed() > 0)
			winsPerGame = stat.getWins() / stat.getGamesPlayed();
		
		p.sendMessage("§0=§1-§0=§1-§0=§1-§0=§1-§0=§1-§0=§1-§0  §6§lSpleef Statistics  §r§0=§1-§0=§1-§0=§1-§0=§1-§0=§1-§0=§1-§0");
		if (!stat.getName().equalsIgnoreCase(p.getName()))
			p.sendMessage(GOLD + "Statistics of " + stat.getName());
		else
			p.sendMessage(GOLD + "Your statistics:");
		p.sendMessage(GREEN + "Wins: " + GOLD + stat.getWins());
		p.sendMessage(RED + "Loses: " + GOLD + stat.getLoses());
		p.sendMessage(DARK_AQUA + "Knockouts: " + GOLD + stat.getKnockouts());
		p.sendMessage(GREEN + "Games played: " + GOLD + stat.getGamesPlayed());
		p.sendMessage(GREEN + "Wins / Game: " + GOLD + winsPerGame);
		p.sendMessage(GREEN + "Score: " + GOLD + stat.getScore());
	}

}
