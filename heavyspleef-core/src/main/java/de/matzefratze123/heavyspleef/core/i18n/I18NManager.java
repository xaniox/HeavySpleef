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
package de.matzefratze123.heavyspleef.core.i18n;

import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;

public class I18NManager {
	
	/* Simple object used for synchronization when initializing
	 * the global instance of I18N */
	private static final Object initLock = new Object();
	private static I18NBuilder globalBuilder;
	private static I18N global;
	
	private Set<I18N> registered;
	
	/**
	 * Returns the global I18N instance used for
	 * retrieving internal messages of HeavySpleef
	 * 
	 * @return The global instance of I18N
	 */
	public static I18N getGlobal() {
		synchronized (initLock) {
			if (global == null) {
				if (globalBuilder == null) {
					throw new IllegalStateException("No global builder has been set for initializing");
				}
				
				global = globalBuilder.build();
			}
		}
		
		return global;
	}
	
	/**
	 * Sets the {@link I18NBuilder} for initializing the
	 * global I18N.<br><br>
	 * 
	 * This may throw an exception when there is already a builder set
	 * 
	 * @param builder The builder for initializing
	 * @see #getGlobal()
	 */
	public static void setGlobalBuilder(I18NBuilder builder) {
		if (globalBuilder != null) {
			throw new IllegalStateException("Global I18NBuilder has already been set");
		}
		
		I18NManager.globalBuilder = builder;
	}
	
	public I18NManager() {
		this.registered = Sets.newHashSet();
	}
	
	public void registerI18N(I18N i18n) {
		Validate.isTrue(!registered.contains(i18n), "I18N instance already registered");
		
		registered.add(i18n);
	}
	
	public I18N registerI18N(I18NBuilder builder) {
		I18N i18n = builder.build();
		registered.add(i18n);
		
		return i18n;
	}
	
	public void unregisterI18N(I18N i18n) {
		Validate.isTrue(registered.contains(i18n), "I18N instance is not registered");
		
		registered.remove(i18n);
	}
	
	public void reloadAll(Locale locale) {
		if (global != null) {
			global.setLocale(locale);
			global.load();
		}
		
		for (I18N i18n : registered) {
			i18n.setLocale(locale);
			i18n.load();
		}
	}
	
}
