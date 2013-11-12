package de.matzefratze123.heavyspleef.stats;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.Logger;

/**
 * A class that provides a cache for statistics
 */
public class CachedStatistics {

	private static CachedStatistics instance;
	
	/**
	 * A key for an invalid statistic module. Cache does not allow
	 * null values which means that we have to mark an empty module
	 * with an invalid holder.
	 */
	public static final String INVALID_MODULE = "__INVALID__";
	
	/**
	 * The cache which stores current statistics
	 */
	private Cache<String, StatisticModule> cache;
	/**
	 * The cacheloader
	 */
	private StatisticCacheLoader loader;
	
	/**
	 * Gets the instance of the statistic cacher
	 */
	public static CachedStatistics getInstance() {
		if (instance == null) {
			instance = new CachedStatistics();
		}
		
		return instance;
	}
	
	private CachedStatistics() {
		loader = new StatisticCacheLoader();
		
		cache = CacheBuilder.newBuilder().
				maximumSize(100).
				expireAfterAccess(2, TimeUnit.MINUTES).
				build(loader);
	}
	
	/**
	 * Caches and returns the statistic
	 * 
	 * @param holder
	 * @return
	 */
	public StatisticModule cacheStatistic(String holder) {
		try {
			return cache.get(holder);
		} catch (ExecutionException e) {
			Logger.warning("Could not cache statistic for user " + holder + ": " + e.getMessage());
		}
		
		return null;
	}
	
	static class StatisticCacheLoader extends CacheLoader<String, StatisticModule> {

		@Override
		public StatisticModule load(String owner) throws Exception {
			StatisticModule module = HeavySpleef.getInstance().getStatisticDatabase().loadAccount(owner);
			
			if (module == null) {
				module = new StatisticModule(INVALID_MODULE);
			}
			
			return module;
		}
		
	}

}
