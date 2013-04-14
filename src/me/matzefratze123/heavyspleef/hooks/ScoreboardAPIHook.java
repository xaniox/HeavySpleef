package me.matzefratze123.heavyspleef.hooks;

import me.matzefratze123.heavyspleef.HeavySpleef;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import yt.codebukkit.scoreboardapi.ScoreboardAPI;

public class ScoreboardAPIHook implements Hook<ScoreboardAPI> {

	private ScoreboardAPI hook = null;
	
	@Override
	public void hook() {
		Plugin api = Bukkit.getPluginManager().getPlugin("ScoreboardAPI");
		
		if (api == null)
			return;
		if (!api.isEnabled())
			return;
		if (getJavaVersion() < 1.7) {
			HeavySpleef.instance.getLogger().warning("Seems that you use Java 6 with ScoreboardAPI...\nScoreboardAPI can only be run with Java 7!");
			return;
		}
		
		hook = ScoreboardAPI.getInstance();
	}

	@Override
	public ScoreboardAPI getHook() {
		return this.hook;
	}

	@Override
	public boolean hasHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("ScoreboardAPI");
		
		if (plugin == null)
			return false;
		if (!plugin.isEnabled())
			return false;
		if (!(plugin instanceof ScoreboardAPI))
			return false;
		if (getJavaVersion() < 1.7) //ScoreboardAPI is compiled with java 7 (Environment 1.7)...
			return false;
		
		if (hook == null)
			hook();
		return true;
	}
	
	private double getJavaVersion() {
		String property = System.getProperty("java.version");
		property = property.substring(0, 3);
		
		double version = -999.0;
		
		try {
			version = Double.parseDouble(property);
		} catch (NumberFormatException e) {
			HeavySpleef.instance.getLogger().severe("Unknown Java Version!!! If you use sidebar scoreboards, expect errors!!!");
		}
		
		return version;
	}
	
}
