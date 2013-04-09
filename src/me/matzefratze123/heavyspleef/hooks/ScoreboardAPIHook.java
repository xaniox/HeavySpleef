package me.matzefratze123.heavyspleef.hooks;

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
		
		if (hook == null)
			hook();
		return true;
	}

	
	
}
