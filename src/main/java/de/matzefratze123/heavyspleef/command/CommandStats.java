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

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.WHITE;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.stats.CachedStatistics;
import de.matzefratze123.heavyspleef.stats.StatisticModule;
import de.matzefratze123.heavyspleef.stats.sql.AbstractDatabase;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandStats extends HSCommand {

	public CommandStats() {
		setMaxArgs(2);
		setMinArgs(0);
		setOnlyIngame(true);
		setUsage("/spleef stats [player|top] [page]");
		setHelp("Shows spleef statistics");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		final Player player = (Player) sender;
		SpleefPlayer spleefPlayer = HeavySpleef.getInstance().getSpleefPlayer(player);
		
		if (!AbstractDatabase.isEnabled()) {
			player.sendMessage(ChatColor.RED + "Statistics are disabled!");
			return;
		}
		
		if (args.length == 0) {
			if (!player.hasPermission(Permissions.STATS.getPerm())) {
				player.sendMessage(_("noPermission"));
				return;
			}
			if (spleefPlayer.getStatistic() == null) {
				player.sendMessage(ChatColor.RED + "You don't have statistics!");
				return;
			}

			StatisticModule stat = spleefPlayer.getStatistic();
			printStatistics(stat, player);
		} else if (args.length >= 1 && args[0].equalsIgnoreCase("top")) {
			int page = 0;
			if (args.length == 2) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					player.sendMessage(_("notANumber", args[1]));
				}
			}

			showLeaderboard(player, page);
		} else if (args.length == 1) {
			if (!player.hasPermission(Permissions.STATS_OTHERS.getPerm())) {
				player.sendMessage(_("noPermission"));
				return;
			}

			Player target = Bukkit.getPlayerExact(args[0]);
			final OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(args[0]);;
			
			SpleefPlayer targetSpleefPlayer = HeavySpleef.getInstance().getSpleefPlayer(target);
			
			if (targetSpleefPlayer != null) {
				printStatistics(targetSpleefPlayer.getStatistic(), player);
			} else if (offlineTarget != null) {
				Bukkit.getScheduler().runTaskAsynchronously(HeavySpleef.getInstance(), new Runnable() {
					
					@Override
					public void run() {
						//Get statistic via cache
						CachedStatistics cache = CachedStatistics.getInstance();
						
						StatisticModule module = cache.cacheStatistic(offlineTarget.getName());
						
						if (module.getName().equalsIgnoreCase(CachedStatistics.INVALID_MODULE)) {
							player.sendMessage(ChatColor.RED + "This player doesn't have statistics!");
						} else {
							printStatistics(module, player);
						}
					}
				});
			} else {
				player.sendMessage(ChatColor.RED + "This player doesn't have statistics!");
			}
		}
	}

	private void printStatistics(StatisticModule module, Player player) {
		if (!module.getName().equalsIgnoreCase(player.getName())) {
			player.sendMessage("--- " + GREEN + "Statistics of " + module.getName() + WHITE + " ---");
		} else {
			player.sendMessage("--- " + GREEN + "Your statistics" + WHITE + " ---");
		}
		
		player.sendMessage(GREEN + "Wins: "         + WHITE + module.getWins());
		player.sendMessage(RED   + "Loses: "        + WHITE + module.getLoses());
		player.sendMessage(GREEN + "Knockouts: "    + WHITE + module.getKnockouts());
		player.sendMessage(GREEN + "Games played: " + WHITE + module.getGamesPlayed());
		player.sendMessage(GREEN + "Wins / Game: "  + WHITE + module.getKD());
		player.sendMessage(GREEN + "Score: "        + WHITE + module.getScore());
	}
	
	public static void showLeaderboard(final Player player, final int page) {
        Bukkit.getScheduler().runTaskAsynchronously(HeavySpleef.getInstance(), new LeaderboardShower(player, page));
	}

	
	private static class LeaderboardShower implements Runnable {
        
        private Player player;
        private int page;
        
        public LeaderboardShower(Player player, int page) {
                this.player = player;
                this.page = page;
        }
        
        @Override
        public void run() {
                List<StatisticModule> list = HeavySpleef.getInstance().getStatisticDatabase().loadAccounts();
                
                if (list == null) {
                        player.sendMessage(ChatColor.RED + "Failed to load statistics!");
                        return;
                }
                
                Collections.sort(list);
                
                page = Math.abs(page);
                
                if (page < 1)
                        page = 1;
                
                int destination = (page - 1) * 10;
                
                player.sendMessage("--- " + ChatColor.GREEN + "Top Players" + ChatColor.WHITE + " ---");
                
                for (int i = 0; i < 10; i++) {
                        int place = destination + i;
                        if (place >= list.size())
                                break;
                        
                        StatisticModule module = list.get(place);
                        
                        //Thread safe method
                        player.sendMessage((place + 1) + ". " + GREEN + module.getName() + " - " + WHITE + "Wins: " + module.getWins() + " | Loses: " + module.getLoses() + " | Win Ratio: " + module.getKD());
                }
                
                
        }
        
}


}
