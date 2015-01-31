package de.matzefratze123.heavyspleef.core.hook;

import java.util.Map;

import com.google.common.collect.Maps;

public class HookManager {
	
	private Map<HookReference, Hook> hooks;
	
	public HookManager() {
		this.hooks = Maps.newHashMap();
	}
	
	public void registerHook(HookReference ref) {
		hooks.put(ref, ref.newHookInstance());
	}
	
	public Hook getHook(HookReference ref) {
		return hooks.get(ref);
	}
	
}
