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
package me.matzefratze123.heavyspleef.hooks;

import java.util.ArrayList;
import java.util.List;

public class HookManager {

	//Hook instances start
	public static VaultHook vaultHook = new VaultHook();
	public static WorldEditHook weHook = new WorldEditHook();
	public static TagAPIHook tagAPIHook = new TagAPIHook();
	//Hook instances end
	
	public static Hook<?>[] allHooks = new Hook<?>[] {vaultHook, weHook, tagAPIHook};
	private List<Hook<?>> hooks = new ArrayList<Hook<?>>();
	
	public HookManager() {
		initHooks();
	}
	
	public static HookManager getInstance() {
		return new HookManager();
	}
	
	private void initHooks() {
		for (Hook<?> hook : allHooks) {
			if (hooks.contains(hook))
				hooks.remove(hook);
			hooks.add(hook);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <V> Hook<V> getService(Class<? extends Hook<V>> clazz) {
		for (Hook<?> hook : hooks) {
			if (!hook.getClass().equals(clazz))
				continue;
			
			Hook<V> h = (Hook<V>)hook;
			return h;
		}
		
		return null;
	}
	
}

	