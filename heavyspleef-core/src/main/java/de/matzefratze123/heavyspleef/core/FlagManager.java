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
package de.matzefratze123.heavyspleef.core;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.GamePropertyPriority;
import de.matzefratze123.heavyspleef.core.flag.GamePropertyPriority.Priority;

@Entity
@Table(name = "flags")
@XmlAccessorType(XmlAccessType.FIELD)
public class FlagManager {
	
	private final JavaPlugin plugin;
	private Map<String, AbstractFlag<?>> flags;
	private Set<GamePropertyBundle> propertyBundles;
	private DefaultGamePropertyBundle requestedProperties;
	
	public FlagManager(JavaPlugin plugin) {
		this.plugin = plugin;
		this.flags = Maps.newLinkedHashMap();
		this.propertyBundles = Sets.newTreeSet();
		this.requestedProperties = new DefaultGamePropertyBundle(Maps.newEnumMap(GameProperty.class));
	}
	
	public void addFlag(AbstractFlag<?> flag) {
		Class<?> clazz = flag.getClass();
		
		Validate.isTrue(clazz.isAnnotationPresent(Flag.class), "Flag class " + clazz.getCanonicalName() + " must annotate " + Flag.class.getCanonicalName());
		Flag flagAnnotation = clazz.getAnnotation(Flag.class);
		
		String name = flagAnnotation.name();
		
		if (flags.containsKey(name)) {
			return;
		}
		
		flags.put(name, flag);
		
		if (clazz.isAnnotationPresent(BukkitListener.class)) {
			Bukkit.getPluginManager().registerEvents(flag, plugin);
		}
		
		if (flagAnnotation.hasGameProperties()) {
			Map<GameProperty, Object> flagGamePropertiesMap = new EnumMap<GameProperty, Object>(GameProperty.class);
			flag.defineGameProperties(flagGamePropertiesMap);
			
			if (!flagGamePropertiesMap.isEmpty()) {
				GamePropertyBundle properties = new GamePropertyBundle(flag, flagGamePropertiesMap);
				propertyBundles.add(properties);
			}
		}
	}
	
	public void removeFlag(String name) {
		if (!flags.containsKey(name)) {
			return;
		}
		
		AbstractFlag<?> flag = flags.remove(name);
		if (flag.getClass().isAnnotationPresent(BukkitListener.class)) {
			HandlerList.unregisterAll(flag);
		}
		
		Iterator<GamePropertyBundle> iterator = propertyBundles.iterator();
		while (iterator.hasNext()) {
			GamePropertyBundle bundle = iterator.next();
			if (bundle.getRelatingFlag() == null || bundle.getRelatingFlag() != flag) {
				continue;
			}
			
			iterator.remove();
		}
	}
	
	public boolean isFlagPresent(String name) {
		return flags.containsKey(name);
	}
	
	public Map<String, AbstractFlag<?>> getPresentFlags() {
		return Collections.unmodifiableMap(flags);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractFlag<?>> T getFlag(Class<T> clazz) {
		for (AbstractFlag<?> flag : flags.values()) {
			if (flag.getClass() == clazz) {
				return (T) flag;
			}
		}
		
		return null;
	}
	
	public AbstractFlag<?> getFlag(String name) {
		return flags.get(name);
	}
	
	public Object getProperty(GameProperty property) {
		Object value = null;
		
		for (GamePropertyBundle bundle : propertyBundles) {
			Object candidateValue = bundle.get(property);
			if (candidateValue != null) {
				value = candidateValue;
			}
		}
		
		if (value == null) {
			// Requested properties have the lowest priority
			value = requestedProperties.get(property);
		}
		
		return value;
	}
	
	public void requestProperty(GameProperty property, Object value) {
		requestedProperties.put(property, value);
	}
	
	public GamePropertyBundle getDefaultPropertyBundle() {
		return requestedProperties;
	}
	
	public static class GamePropertyBundle extends ForwardingMap<GameProperty, Object> implements Comparable<GamePropertyBundle> {
		
		@XmlTransient
		@Transient
		private AbstractFlag<?> relatingFlag;
		private Map<GameProperty, Object> delegate;
		private GamePropertyPriority.Priority priority;

		public GamePropertyBundle(AbstractFlag<?> flag, Map<GameProperty, Object> propertyMap) {
			this.delegate = propertyMap;
			this.relatingFlag = flag;
			
			try {
				// Doing it the ugly way...
				Method method = flag.getClass().getMethod("defineGameProperties", Map.class);
				if (!method.isAnnotationPresent(GamePropertyPriority.class)) {
					priority = Priority.NORMAL;
				} else {
					GamePropertyPriority priorityAnnotation = method.getAnnotation(GamePropertyPriority.class);
					priority = priorityAnnotation.value();
				}
			} catch (Exception e) {
				//Could not get priority
				priority = Priority.NORMAL;
			}
		}
		
		public GamePropertyBundle(Priority priority, Map<GameProperty, Object> propertyMap) {
			this.priority = priority;
			this.delegate = propertyMap;
		}
		
		@Override
		protected Map<GameProperty, Object> delegate() {
			return delegate;
		}
		
		public GamePropertyPriority.Priority getPriority() {
			return priority;
		}
		
		public AbstractFlag<?> getRelatingFlag() {
			return relatingFlag;
		}

		@Override
		public int compareTo(GamePropertyBundle other) {
			return Integer.valueOf(priority.getSortInt()).compareTo(other.getPriority().getSortInt());
		}
		
	}
	
	@Entity
	@Table(name = "property_bundles")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class DefaultGamePropertyBundle extends GamePropertyBundle {

		public DefaultGamePropertyBundle(Map<GameProperty, Object> propertyMap) {
			super(Priority.REQUESTED, propertyMap);
		}
		
	}
	
}
