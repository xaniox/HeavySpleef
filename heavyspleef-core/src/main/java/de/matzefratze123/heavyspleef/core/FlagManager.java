package de.matzefratze123.heavyspleef.core;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.core.flag.AbstractFlag;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.GamePropertyPriority;
import de.matzefratze123.heavyspleef.core.flag.GamePropertyPriority.Priority;

public class FlagManager {
	
	private final JavaPlugin plugin;
	private Map<String, AbstractFlag<?>> flags;
	private Set<GamePropertyBundle> propertyBundles;
	
	public FlagManager(JavaPlugin plugin) {
		this.plugin = plugin;
		this.flags = Maps.newLinkedHashMap();
		this.propertyBundles = Sets.newTreeSet();
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
		
		if (flag.hasBukkitListenerMethods()) {
			Bukkit.getPluginManager().registerEvents(flag, plugin);
		}
		
		Map<GameProperty, Object> flagGamePropertiesMap = new EnumMap<GameProperty, Object>(GameProperty.class);
		flag.defineGameProperties(flagGamePropertiesMap);
		
		if (!flagGamePropertiesMap.isEmpty()) {
			GamePropertyBundle properties = new GamePropertyBundle(flag, flagGamePropertiesMap);
			propertyBundles.add(properties);
		}
	}
	
	public void removeFlag(String name) {
		if (!flags.containsKey(name)) {
			return;
		}
		
		AbstractFlag<?> flag = flags.remove(name);
		if (flag.hasBukkitListenerMethods()) {
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
	
	public Object getProperty(GameProperty property) {
		Object value = null;
		
		for (GamePropertyBundle bundle : propertyBundles) {
			Object candidateValue = bundle.get(property);
			if (candidateValue != null) {
				value = candidateValue;
			}
		}
		
		return value;
	}
	
	public void requestProperty(GameProperty property, Object value) {
		GamePropertyBundle defaultBundle = getDefaultBundle();
		
		defaultBundle.put(property, value);
	}
	
	private DefaultGamePropertyBundle getDefaultBundle() {
		DefaultGamePropertyBundle defaultBundle = null;
		for (GamePropertyBundle bundle : propertyBundles) {
			if (bundle instanceof DefaultGamePropertyBundle) {
				defaultBundle = (DefaultGamePropertyBundle) bundle;
				break;
			}
		}
		
		if (defaultBundle == null) {
			Map<GameProperty, Object> requestedProperties = new EnumMap<GameProperty, Object>(GameProperty.class);
			defaultBundle = new DefaultGamePropertyBundle(requestedProperties);
		}
		
		return defaultBundle;
	}
	
	private static class GamePropertyBundle extends ForwardingMap<GameProperty, Object> implements Comparable<GamePropertyBundle> {
		
		private AbstractFlag<?> relatingFlag;
		private Map<GameProperty, Object> delegate;
		private GamePropertyPriority.Priority priority;

		public GamePropertyBundle(AbstractFlag<?> flag, Map<GameProperty, Object> propertyMap) {
			this.delegate = propertyMap;
			this.relatingFlag = flag;
			
			try {
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
	
	private static class DefaultGamePropertyBundle extends GamePropertyBundle {

		public DefaultGamePropertyBundle(Map<GameProperty, Object> propertyMap) {
			super(Priority.REQUESTED, propertyMap);
		}
		
	}
	
}
