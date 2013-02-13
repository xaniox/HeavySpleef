package me.matzefratze123.heavyspleef.utility.statistic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StatisticManager {

	private static Map<String, Statistic> statistics = new HashMap<String, Statistic>();
	
	public static Statistic getStatistic(String owner) {
		return statistics.get(owner);
	}
	
	public static boolean addNewStatistic(String owner) {
		if (statistics.containsKey(owner))
			return false;
		statistics.put(owner, new Statistic(owner));
		return true;
	}
	
	public static boolean addExistingStatistic(Statistic stat) {
		if (statistics.containsKey(stat.getName()))
			return false;
		statistics.put(stat.getName(), stat);
		return true;
	}
	
	public static boolean hasStatistic(String owner) {
		return statistics.containsKey(owner);
	}
	
	public static Collection<Statistic> getStatistics() {
		return statistics.values();
	}
	
}
