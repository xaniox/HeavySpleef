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
package de.matzefratze123.heavyspleef.stats;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.WHITE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.bukkit.Bukkit;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class StatisticManager {

	private static volatile Map<String, StatisticModule> statistics = new HashMap<String, StatisticModule>();
	static boolean pushOnChange;
	
	static {
		pushOnChange = HeavySpleef.getSystemConfig().getBoolean("statistic.push-on-change");
	}
	
	public static StatisticModule getStatistic(String owner, boolean add) {
		if (add && !hasStatistic(owner))
			addNewStatistic(owner);
		return statistics.get(owner);
	}
	
	public static boolean addNewStatistic(String owner) {
		if (statistics.containsKey(owner))
			return false;
		statistics.put(owner, new StatisticModule(owner));
		return true;
	}
	
	public static boolean addExistingStatistic(StatisticModule stat) {
		if (statistics.containsKey(stat.getName()))
			return false;
		statistics.put(stat.getName(), stat);
		return true;
	}
	
	public static boolean hasStatistic(String owner) {
		return statistics.containsKey(owner);
	}
	
	public static Collection<StatisticModule> getStatistics() {
		return statistics.values();
	}
	
	public static Map<String, StatisticModule> getMap() {
		return statistics;
	}
	
	public static String[] retrieveLeaderboard(int page) {
		String[] array = new String[10];
		List<StatisticModule> list = new ArrayList<StatisticModule>(statistics.values());
		Collections.sort(list);
		
		page = Math.abs(page);
		
		if (page < 1)
			page = 1;
		
		int destination = (page - 1) * 10;
		
		for (int i = 0; i < 10; i++) {
			int place = destination + i;
			if (place >= list.size())
				break;
			
			StatisticModule module = list.get(place);
			array[i] = (place + 1) + ". " + GREEN + module.getName() + " - " + WHITE + "Wins: " + module.getWins() + " | Loses: " + module.getLoses() + " | Win Ratio: " + module.getKD();
		}
		
		return array;
	}
	
	/**
	 * Pushes all statistics to the database
	 * </br></br>
	 * Note that the paramater async only works if
	 * the database type is set to mysql</br>
	 * (Cannot use Bukkits SnakeYAML async as it is not thread-safe)
	 */
	public static void push(boolean async) {
		if (HeavySpleef.getSystemConfig().getString("statistic.dbType").equalsIgnoreCase("mysql") && async) {
			Bukkit.getScheduler().runTaskAsynchronously(HeavySpleef.instance, new Runnable() {
				
				@Override
				public void run() {
					new MySQLStatisticDatabase().save();
				}
			});
		} else {
			HeavySpleef.instance.statisticDatabase.save();
		}
	}
	
}
