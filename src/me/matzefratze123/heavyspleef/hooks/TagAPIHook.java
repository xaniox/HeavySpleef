package me.matzefratze123.heavyspleef.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.kitteh.tag.TagAPI;

public class TagAPIHook implements Hook<TagAPI> {

	private TagAPI hook;
	
	@Override
	public void hook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("TagAPI");
		
		if (plugin == null)
			return;
		if (!plugin.isEnabled())
			return;
		if (!(plugin instanceof TagAPI))
			return;
		
		TagAPI api = (TagAPI)plugin;
		hook = api;
	}

	@Override
	public TagAPI getHook() {
		return this.hook;
	}

	@Override
	public boolean hasHook() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("TagAPI");
		
		if (plugin == null)
			return false;
		if (!plugin.isEnabled())
			return false;
		if (!(plugin instanceof TagAPI))
			return false;
		
		if (hook == null)
			hook();
		
		return true;
	}
	
	
	
}
