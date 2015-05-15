/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef;

import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import de.matzefratze123.heavyspleef.addon.AddOnModule;
import de.matzefratze123.heavyspleef.commands.CommandModule;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.HeavySpleef.GamesLoadCallback;
import de.matzefratze123.heavyspleef.core.module.Module;
import de.matzefratze123.heavyspleef.core.module.LoadPolicy.Lifecycle;
import de.matzefratze123.heavyspleef.flag.FlagModule;
import de.matzefratze123.heavyspleef.persistence.PersistenceModule;

public class HeavySpleefPlugin extends JavaPlugin {

	private HeavySpleef heavySpleef;
	
	@Override
	public void onEnable() {
		heavySpleef = new HeavySpleef(this);
		heavySpleef.load();
		
		Module flagModule = new FlagModule(heavySpleef);
		Module commandModule = new CommandModule(heavySpleef);
		Module persistenceModule = new PersistenceModule(heavySpleef);
		Module addOnModule = new AddOnModule(heavySpleef);
		
		heavySpleef.registerModule(flagModule);
		heavySpleef.registerModule(commandModule);
		heavySpleef.registerModule(persistenceModule);
		heavySpleef.registerModule(addOnModule);
		
		heavySpleef.enableModules(Lifecycle.POST_LOAD);
		heavySpleef.addGamesLoadCallback(new GamesLoadCallback() {
			
			@Override
			public void onGamesLoaded(List<Game> games) {
				heavySpleef.enableModules(Lifecycle.POST_GAMES_LOAD);
			}
		});
		
		heavySpleef.enable();
		heavySpleef.enableModules(Lifecycle.POST_ENABLE);
	}
	
	@Override
	public void onDisable() {
		if (heavySpleef != null) {
			heavySpleef.disable(); 
		}
	}
	
}
