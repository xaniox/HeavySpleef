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
package de.matzefratze123.heavyspleef.core.flag;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.BiMap;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.core.flag.GamePropertyPriority.Priority;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.game.GameProperty;

public class FlagManager {
	
	private final JavaPlugin plugin;
	private BiMap<String, AbstractFlag<?>> flags;
	private List<UnloadedFlag> unloadedFlags;
	private Set<String> disabledFlags;
	private Set<GamePropertyBundle> propertyBundles;
	private DefaultGamePropertyBundle requestedProperties;
	private GamePropertyBundle defaults;
	
	public FlagManager(JavaPlugin plugin, GamePropertyBundle defaults) {
		this.plugin = plugin;
		this.defaults = defaults;
		this.flags = HashBiMap.create();
		this.disabledFlags = Sets.newHashSet();
		this.unloadedFlags = Lists.newArrayList();
		this.propertyBundles = Sets.newTreeSet();
		this.requestedProperties = new DefaultGamePropertyBundle(Maps.newEnumMap(GameProperty.class));
	}
	
	public void addFlag(AbstractFlag<?> flag) {
		addFlag(flag, false);
	}
	
	public void addFlag(AbstractFlag<?> flag, boolean disable) {
		Class<?> clazz = flag.getClass();
		String path;
		
		if (flag instanceof UnloadedFlag) {
			UnloadedFlag unloadedFlag = (UnloadedFlag) flag;
			path = unloadedFlag.getFlagName();
			
			for (UnloadedFlag unloaded : unloadedFlags) {
				if (unloaded.getFlagName().equals(path)) {
					throw new IllegalStateException("Unloaded flag with name " + path + " already registered");
				}
			}
			
			if (flags.containsKey(path) || disabledFlags.contains(path)) {
				return;
			}
			
			unloadedFlags.add(unloadedFlag);
		} else {
			Validate.isTrue(clazz.isAnnotationPresent(Flag.class), "Flag class " + clazz.getCanonicalName() + " must annotate " + Flag.class.getCanonicalName());
			Flag flagAnnotation = clazz.getAnnotation(Flag.class);
			path = generatePath(flagAnnotation);
			
			if (flags.containsKey(path) || disabledFlags.contains(path)) {
				return;
			}
			
			if (disable) {
				disabledFlags.add(path);
			} else {
				flags.put(path, flag);
				
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
				
				if (flagAnnotation.parent() != NullFlag.class) {
					AbstractFlag<?> parent = getFlag(flagAnnotation.parent());
					flag.setParent(parent);
				}
			}
		}
	}
	
	public void revalidateParents() {
		for (AbstractFlag<?> flag : flags.values()) {
			Flag annotation = flag.getClass().getAnnotation(Flag.class);
			if (annotation.parent() == NullFlag.class || flag.getParent() != null) {
				continue;
			}
			
			AbstractFlag<?> parent = getFlag(annotation.parent());
			if (parent == null) {
				throw new IllegalStateException("Parent of flag " + flag.getClass().getSimpleName() + " is not available!");
			}
			
			flag.setParent(parent);
		}
	}
	
	private String generatePath(Flag flagAnnotation) {
		//Generate the full path
		StringBuilder pathBuilder = new StringBuilder();
		
		Flag lastParentFlagData = flagAnnotation;
		while (lastParentFlagData != null) {
			pathBuilder.insert(0, lastParentFlagData.name());
			
			Class<? extends AbstractFlag<?>> parentClass = lastParentFlagData.parent();
			lastParentFlagData = parentClass.getAnnotation(Flag.class);
			
			if (lastParentFlagData != null) {
				pathBuilder.insert(0, ":");
			}
		}
		
		String path = pathBuilder.toString();
		return path;
	}
	
	public AbstractFlag<?> removeFlag(String path) {
		AbstractFlag<?> flag = flags.remove(path);
		if (flag == null) {
			disabledFlags.remove(path);
			
			UnloadedFlag removed = null;
			Iterator<UnloadedFlag> iterator = unloadedFlags.iterator();
			while (iterator.hasNext()) {
				UnloadedFlag unloaded = iterator.next();
				if (!unloaded.getFlagName().equals(path)) {
					continue;
				}
				
				iterator.remove();
				removed = unloaded;
			}
			
			return removed;
		}
		
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
		
		return flag;
	}
	
	public AbstractFlag<?> removeFlag(Class<? extends AbstractFlag<?>> flagClass) {
		AbstractFlag<?> recent = null;
		
		if (UnloadedFlag.class.isAssignableFrom(flagClass)) {
			for (UnloadedFlag other : Lists.newArrayList(unloadedFlags)) {
				if (other.getClass() != flagClass) {
					continue;
				}
				
				recent = removeFlag(other.getFlagName());
			}
		} else {
			for (Entry<String, AbstractFlag<?>> entry : Sets.newHashSet(flags.entrySet())) {
				AbstractFlag<?> flag = entry.getValue();
				if (flag.getClass() != flagClass) {
					continue;
				}
				
				recent = removeFlag(entry.getKey());
			}
		}
		
		return recent;
	}
	
	public boolean isFlagPresent(String path) {
		return isFlagPresent(path, false);
	}
	
	public boolean isFlagPresent(String path, boolean checkDisabled) {		
		boolean present = flags.containsKey(path);
		if (checkDisabled && !present) {
			present = disabledFlags.contains(path);
		}
		
		return present;
	}
	
	public boolean isFlagPresent(Class<? extends AbstractFlag<?>> clazz) {
		for (AbstractFlag<?> val : flags.values()) {
			if (clazz.isInstance(val)) {
				return true;
			}
		}
		
		return false;
	}
	
	public Set<AbstractFlag<?>> getFlags() {
		Set<AbstractFlag<?>> set = Sets.newHashSet();
		set.addAll(flags.values());
		set.addAll(unloadedFlags);
		
		return set;
	}
	
	@SuppressWarnings("rawtypes")
	public List<Conflict> computeConflicts(Class<? extends AbstractFlag<?>> flagClass, Flag flagAnnotation) {
		List<Conflict> conflicts = Lists.newArrayList();
		
		for (AbstractFlag<?> otherFlag : flags.values()) {
			Class<? extends AbstractFlag> otherFlagClass = otherFlag.getClass();
			Flag otherFlagAnnotation = otherFlagClass.getAnnotation(Flag.class);
			
			for (Class<? extends AbstractFlag<?>> flagConflictClass : flagAnnotation.conflictsWith()) {
				if (flagConflictClass != otherFlagClass) {
					continue;
				}
				
				conflicts.add(new Conflict(flagClass, flagAnnotation, otherFlagClass, otherFlagAnnotation));
			}
			
			for (Class<? extends AbstractFlag<?>> flagConflictClass : otherFlagAnnotation.conflictsWith()) {
				if (flagConflictClass != flagClass) {
					continue;
				}
				
				conflicts.add(new Conflict(otherFlagClass, otherFlagAnnotation, flagClass, flagAnnotation));
			}
		}
		
		return conflicts;
	}
	
	@SuppressWarnings("unchecked")
	public void disableFlag(String path) {
		Validate.isTrue(flags.containsKey(path), "Flag is not registered or already disabled");
		
		AbstractFlag<?> flag = flags.get(path);
		disableFlag((Class<? extends AbstractFlag<?>>)flag.getClass());
	}
	
	public void disableFlag(Class<? extends AbstractFlag<?>> clazz) {		
		AbstractFlag<?> flag = getFlag(clazz);
		Validate.notNull(flag, "Flag is not registered or already disabled");
		
		String path = flags.inverse().remove(flag);
		
		if (clazz.isAnnotationPresent(BukkitListener.class)) {
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
		
		disabledFlags.add(path);
	}
	
	@SuppressWarnings("unchecked")
	public void enableFlag(String path, FlagRegistry registry, Game game) {
		Validate.isTrue(disabledFlags.contains(path), "Flag is not disabled");
		
		AbstractFlag<?> flag = flags.get(path);
		enableFlag((Class<? extends AbstractFlag<?>>)flag.getClass(), registry, game);
	}
	
	public void enableFlag(Class<? extends AbstractFlag<?>> clazz, FlagRegistry registry, Game game) {
		Validate.isTrue(!isFlagPresent(clazz));
		Validate.isTrue(clazz.isAnnotationPresent(Flag.class));
		
		Flag flagAnnotation = clazz.getAnnotation(Flag.class);
		String path = generatePath(flagAnnotation);
		
		Validate.isTrue(disabledFlags.contains(path), "Flag is not disabled");
		AbstractFlag<?> flag = registry.newFlagInstance(path, AbstractFlag.class, game);
		
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
		
		if (flagAnnotation.parent() != NullFlag.class) {
			AbstractFlag<?> parent = getFlag(flagAnnotation.parent());
			flag.setParent(parent);
		}
		
		flags.put(path, flag);
		disabledFlags.remove(path);
	}
	
	public Map<String, AbstractFlag<?>> getPresentFlags() {
		Map<String, AbstractFlag<?>> unloadedFlags = Maps.newHashMap();
		for (UnloadedFlag unloaded : this.unloadedFlags) {
			unloadedFlags.put(unloaded.getFlagName(), unloaded);
		}
		
		ImmutableMap.Builder<String, AbstractFlag<?>> builder = ImmutableMap.builder();
		
		return builder.putAll(flags)
				.putAll(unloadedFlags)
				.build();
	}
	
	public Set<String> getDisabledFlags() {
		return ImmutableSet.copyOf(disabledFlags);
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
	
	public AbstractFlag<?> getFlag(String path) {
		AbstractFlag<?> flag = flags.get(path);
		return flag;
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
		
		if (value == null) {
			// There is no requested property, just use the config default
			value = defaults.get(property);
		}
		
		if (value == null) {
			value = property.getDefaultValue();
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
	
	public static class DefaultGamePropertyBundle extends GamePropertyBundle {

		public DefaultGamePropertyBundle(Map<GameProperty, Object> propertyMap) {
			super(Priority.REQUESTED, propertyMap);
		}
		
	}
	
	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Conflict {
		
		private Class<?> conflictSource;
		private Flag conflictSourceAnnotation;
		private Class<?> conflictWith;
		private Flag conflictWithAnnotation;
		
	}
	
}
