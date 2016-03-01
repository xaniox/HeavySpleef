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
package de.xaniox.heavyspleef.core.flag;

import de.xaniox.heavyspleef.core.MinecraftVersion;
import de.xaniox.heavyspleef.core.hook.HookReference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {

	String name();
	
	Class<? extends AbstractFlag<?>> parent() default NullFlag.class;
	
	boolean hasCommands() default false;
	
	boolean hasGameProperties() default false;
	
	boolean ignoreParseException() default false;
	
	HookReference[] depend() default {};
	
	String[] pluginDepend() default {};
	
	Class<? extends AbstractFlag<?>>[] conflictsWith() default {};
	
	int requiresVersion() default MinecraftVersion.UNKNOWN_VERSION;
	
}