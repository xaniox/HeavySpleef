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
package de.matzefratze123.heavyspleef.addon;

import java.io.File;
import java.lang.reflect.Field;
import java.util.logging.Level;

import de.matzefratze123.heavyspleef.addon.java.BasicAddOn;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry;
import de.matzefratze123.heavyspleef.core.flag.FlagRegistry.FlagClassHolder;
import de.matzefratze123.heavyspleef.core.flag.Injector;
import de.matzefratze123.heavyspleef.core.module.SimpleModule;

public class AddOnModule extends SimpleModule {

	private static final String BASEDIR_FILE_NAME = "addons";
	
	private File baseDir;
	private AddOnManager manager;
	private final Injector<AbstractFlag<?>> addOnInjector = new Injector<AbstractFlag<?>>() {
		
		@Override
		public void inject(AbstractFlag<?> instance, Field[] injectableFields, Object holderCookie) throws IllegalArgumentException,
				IllegalAccessException {
			FlagClassHolder holder = (FlagClassHolder) holderCookie;
			
			//Extract the add-on out of the holder
			Object cookie = holder.getCookie();
			if (cookie == null) {
				//Ignore this flag injection request
				return;
			}
			
			BasicAddOn addOn = (BasicAddOn) holder.getCookie();
			
			for (Field field : injectableFields) {
				if (AddOn.class.isAssignableFrom(field.getType())) {
					field.set(instance, addOn);
				}
			}
		}
	};
	
	public AddOnModule(HeavySpleef heavySpleef) {
		super(heavySpleef);
	}

	@Override
	public void enable() {
		HeavySpleef heavySpleef = getHeavySpleef();
		FlagRegistry flagRegistry = heavySpleef.getFlagRegistry();
		flagRegistry.registerInjector(addOnInjector);
		
		File dataFolder = heavySpleef.getDataFolder();
		
		baseDir = new File(dataFolder, BASEDIR_FILE_NAME);
		if (!baseDir.exists()) {
			baseDir.mkdir();
		}
		
		manager = new AddOnManager(heavySpleef);
		manager.loadAddOns(baseDir);
		manager.enableAddOns();
		getLogger().log(Level.INFO, "Add-On load complete. Enabled " + manager.getAddOns().size() + " add-ons");
	}

	@Override
	public void reload() {
		for (AddOn addOn : manager.getEnabledAddOns()) {
			manager.disableAddOn(addOn.getName());
			manager.unloadAddOn(addOn.getName());
		}
		
		manager.loadAddOnsSafely(baseDir);
		manager.enableAddOns();
		getLogger().log(Level.INFO, "Add-On reload complete. Enabled " + manager.getAddOns().size() + " add-ons");
	}

	@Override
	public void disable() {
		for (AddOn addOn : manager.getEnabledAddOns()) {
			manager.disableAddOn(addOn.getName());
		}
		
		HeavySpleef heavySpleef = getHeavySpleef();
		FlagRegistry flagRegistry = heavySpleef.getFlagRegistry();
		flagRegistry.unregisterInjector(addOnInjector);
	}

}
