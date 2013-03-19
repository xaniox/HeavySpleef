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
package me.matzefratze123.heavyspleef.utility.statistic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatisticManager {

	private static Map<String, StatisticModule> statistics = new HashMap<String, StatisticModule>();
	
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
	
}
