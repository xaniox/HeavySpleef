package de.matzefratze123.heavyspleef.core.hook;

import java.util.Set;

import com.google.common.collect.Sets;

public class HookManager {
	
	private Set<Hook> hooks;
	
	public HookManager() {
		this.hooks = Sets.newHashSet();
	}
	
	public void registerHook(String plugin) {
		Hook hook = new DefaultHook(plugin);
		registerHook(hook);
	}
	
	public void registerHook(Hook hook) {
		hooks.add(hook);
	}
	
	public Hook getHook(String plugin) {
		for (Hook hook : hooks) {
			if (hook.getName().equals(plugin)) {
				return hook;
			}
		}
		
		return null;
	}
	
}
