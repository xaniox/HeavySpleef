package me.matzefratze123.heavyspleef.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class WorldEditHook implements Hook<WorldEditPlugin>{

	private WorldEditPlugin hook = null;
	
	@Override
	public void hook() {
		Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
		
		if (we == null)
			return;
		
		if (!we.isEnabled())
			return;
		
		if (!(we instanceof WorldEditPlugin))
			return;
		
		hook = (WorldEditPlugin) we;
	}

	@Override
	public WorldEditPlugin getHook() {
		return this.hook;
	}

	@Override
	public boolean hasHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
		
		if (plugin == null)
			return false;
		if (!plugin.isEnabled())
			return false;
		if (!(plugin instanceof WorldEditPlugin))
			return false;
		
		if (hook == null)
			hook();
		return true;
	}

}
