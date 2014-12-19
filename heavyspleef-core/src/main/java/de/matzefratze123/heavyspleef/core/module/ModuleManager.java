package de.matzefratze123.heavyspleef.core.module;

import java.util.Set;

import com.google.common.collect.Sets;

public class ModuleManager {
	
	private Set<Module> modules;
	
	public ModuleManager() {
		this.modules = Sets.newHashSet();
	}
	
	public void registerModule(Module module) {
		modules.add(module);
		module.enable();
	}
	
	public void disableModules() {
		modules.forEach(Module::disable);
	}
	
}