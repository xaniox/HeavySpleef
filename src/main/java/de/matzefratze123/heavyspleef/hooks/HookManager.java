/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.hooks;

public class HookManager {

	private static HookManager instance;
	
	private Hook<?>[] hooks = new Hook<?>[3];
	
	private HookManager() {
		hooks[0] = new VaultHook();
		hooks[1] = new WorldEditHook();
		hooks[2] = new TagAPIHook();
	}
	
	public static HookManager getInstance() {
		if (instance == null) {
			instance = new HookManager();
		}
		
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <V> Hook<V> getService(Class<? extends Hook<V>> clazz) {
		Hook<V> found = null;
		
		for (Hook<?> hook : hooks) {
			if (!hook.getClass().equals(clazz))
				continue;
			
			found = (Hook<V>)hook;
		}
	
		return found;
	}
	
}

	