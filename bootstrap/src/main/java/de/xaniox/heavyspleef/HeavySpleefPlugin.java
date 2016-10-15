/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef;

import de.xaniox.heavyspleef.addon.AddOnModule;
import de.xaniox.heavyspleef.commands.CommandModule;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.HeavySpleef.GamesLoadCallback;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.module.LoadPolicy;
import de.xaniox.heavyspleef.core.module.Module;
import de.xaniox.heavyspleef.flag.FlagModule;
import de.xaniox.heavyspleef.migration.MigrationModule;
import de.xaniox.heavyspleef.persistence.PersistenceModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class HeavySpleefPlugin extends JavaPlugin {

	private HeavySpleef heavySpleef;
	
	@Override
	public void onEnable() {
		heavySpleef = new HeavySpleef(this);
		
		Module flagModule = new FlagModule(heavySpleef);
		Module commandModule = new CommandModule(heavySpleef);
		Module persistenceModule = new PersistenceModule(heavySpleef);
		Module addOnModule = new AddOnModule(heavySpleef);
		Module legacyModule = new MigrationModule(heavySpleef);
		
		heavySpleef.registerModule(legacyModule);
		heavySpleef.registerModule(commandModule);
		heavySpleef.registerModule(flagModule);
		heavySpleef.registerModule(persistenceModule);
		heavySpleef.registerModule(addOnModule);
		heavySpleef.enableModules(LoadPolicy.Lifecycle.PRE_LOAD);
		
		heavySpleef.load();
		
		heavySpleef.enableModules(LoadPolicy.Lifecycle.POST_LOAD);
		heavySpleef.addGamesLoadCallback(new GamesLoadCallback() {
			
			@Override
			public void onGamesLoaded(List<Game> games) {
				heavySpleef.enableModules(LoadPolicy.Lifecycle.POST_GAMES_LOAD);
			}
		});
		
		heavySpleef.enable();
		heavySpleef.enableModules(LoadPolicy.Lifecycle.POST_ENABLE);
	}
	
	@Override
	public void onDisable() {
		if (heavySpleef != null) {
			heavySpleef.disable(); 
		}
	}

	public HeavySpleef getCoreHeavySpleef() {
        return heavySpleef;
    }
	
}