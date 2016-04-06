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

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.xaniox.heavyspleef.commands.base.CommandManager;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Unregister;
import de.xaniox.heavyspleef.core.collection.DualKeyBiMap;
import de.xaniox.heavyspleef.core.collection.DualKeyHashBiMap;
import de.xaniox.heavyspleef.core.collection.DualKeyMap;
import de.xaniox.heavyspleef.core.collection.DualMaps;
import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.DefaultConfig;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.hook.HookManager;
import de.xaniox.heavyspleef.core.hook.HookReference;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.PluginManager;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.lang.reflect.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

public class FlagRegistry {
	
	public static final String FLAG_PATH_SEPERATOR = ":";
	
	private final HeavySpleef heavySpleef;
	private final I18NSupplier GLOBAL_SUPPLIER = new I18NSupplier() {
		
		@Override
		public I18N supply() {
			return I18NManager.getGlobal();
		}
	};
	private final DualMaps.Mapper<FlagClassHolder, Class<? extends AbstractFlag<?>>> valueMapper = new DualMaps.Mapper<FlagClassHolder, Class<? extends AbstractFlag<?>>>() {

		@Override
		public Class<? extends AbstractFlag<?>> map(FlagClassHolder from) {
			return from.flagClass;
		}
	};
	private DualKeyBiMap<String, Flag, FlagClassHolder> registeredFlagsMap;
	private Queue<Method> queuedInitMethods;
	private Set<Injector<AbstractFlag<?>>> staticInjectors;
	private Set<Injector<AbstractFlag<?>>> instanceInjectors;
	private InitializationPolicy initializationPolicy;
	
	public FlagRegistry(HeavySpleef heavySpleef) {
		this.heavySpleef = heavySpleef;
		this.registeredFlagsMap = new DualKeyHashBiMap<String, Flag, FlagClassHolder>(String.class, Flag.class);
		this.queuedInitMethods = Lists.newLinkedList();
		this.staticInjectors = Sets.newHashSet();
		this.instanceInjectors = Sets.newHashSet();
		this.initializationPolicy = InitializationPolicy.COMMIT;
	}
	
	public void registerFlag(Class<? extends AbstractFlag<?>> clazz) {
		registerFlag(clazz, null, null);
	}
	
	public void registerFlag(Class<? extends AbstractFlag<?>> clazz, I18NSupplier i18nSupplier, Object cookie) {
		Validate.notNull(clazz, "clazz cannot be null");
		Validate.isTrue(!isFlagPresent(clazz), "Cannot register flag twice: " + clazz.getName());
		
		if (i18nSupplier == null) {
			i18nSupplier = GLOBAL_SUPPLIER;
		}
		
		/* Check if the class provides the required Flag annotation */
		Validate.isTrue(clazz.isAnnotationPresent(Flag.class), "Flag-Class must be annotated with the @Flag annotation");
		
		Flag flagAnnotation = clazz.getAnnotation(Flag.class);
		String name = flagAnnotation.name();
		
		Validate.isTrue(!name.isEmpty(), "name() of annotation of flag for class " + clazz.getCanonicalName() + " cannot be empty");
		
		/* Generate a path */
		StringBuilder pathBuilder = new StringBuilder();
		Flag parentFlagData = flagAnnotation;
		
		do {
			pathBuilder.insert(0, parentFlagData.name());
			
			Class<? extends AbstractFlag<?>> parentFlagClass = parentFlagData.parent();
			parentFlagData = parentFlagClass.getAnnotation(Flag.class);
			
			if (parentFlagData != null && parentFlagClass != NullFlag.class) {
				pathBuilder.insert(0, FLAG_PATH_SEPERATOR);
			}
		} while (parentFlagData != null);
		
		String path = pathBuilder.toString();
		
		/* Check for name collides */
		for (String flagPath : registeredFlagsMap.primaryKeySet()) {
			if (flagPath.equalsIgnoreCase(path)) {
				throw new IllegalArgumentException("Flag " + clazz.getName() + " collides with " + registeredFlagsMap.get(flagPath).flagClass.getName());
			}
		}
		
		/* Check if the class can be instantiated */
		try {
			Constructor<? extends AbstractFlag<?>> constructor = clazz.getDeclaredConstructor();
			
			//Make the constructor accessible for future uses
			constructor.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Flag-Class must provide an empty constructor");
		}
		
		FlagClassHolder holder = new FlagClassHolder();
		holder.flagClass = clazz;
		holder.supplier = i18nSupplier;
		holder.cookie = cookie;
		
		Field[] instanceInjectableFields = null;
		
		if (checkHooks(flagAnnotation)) {
			inject(holder, null, null);
			instanceInjectableFields = getInjectableDeclaredFieldsByFilter(clazz, new FieldFilter(FieldFilter.INSTANCE_MODE));
			
			if (initializationPolicy == InitializationPolicy.COMMIT) {
				Method[] initMethods = getInitMethods(holder);
				for (Method method : initMethods) {
					queuedInitMethods.offer(method);
				}
			} else if (initializationPolicy == InitializationPolicy.REGISTER) {
				runInitMethods(holder);
			}
			
			if (flagAnnotation.hasCommands()) {
				CommandManager manager = heavySpleef.getCommandManager();
				manager.registerSpleefCommands(clazz);
			}
			
			
			
			holder.staticFieldsInjected = true;
			holder.staticMethodsInitialized = true;
		}
		
		holder.injectingFields = instanceInjectableFields;
		registeredFlagsMap.put(path, flagAnnotation, holder);
		
		if (heavySpleef.isGamesLoaded()) {
			for (Game game : heavySpleef.getGameManager().getGames()) {
				for (AbstractFlag<?> flag : game.getFlagManager().getFlags()) {
					if (!(flag instanceof UnloadedFlag)) {
						continue;
					}
					
					UnloadedFlag unloaded = (UnloadedFlag) flag;
					if (!unloaded.getFlagName().equals(path)) {
						continue;
					}
					
					game.removeFlag(path);
					
					AbstractFlag<?> newFlag = newFlagInstance(path, AbstractFlag.class, game);
					newFlag.unmarshal(unloaded.getXmlElement());
					
					game.addFlag(newFlag);
				}
			}
		}
	}
	
	public void unregister(Class<? extends AbstractFlag<?>> flagClass) {
		String path = null;
		
		for (Entry<DualKeyMap.DualKeyPair<String, Flag>, FlagClassHolder> entry : registeredFlagsMap.entrySet()) {
			FlagClassHolder holder = entry.getValue();
			
			if (holder.flagClass != flagClass) {
				continue;
			}
			
			Flag annotation = entry.getKey().getSecondaryKey();
			if (annotation.hasCommands()) {
				CommandManager manager = heavySpleef.getCommandManager();
				manager.unregisterSpleefCommand(flagClass);
			}
			
			Iterator<Method> methodIterator = queuedInitMethods.iterator();
			while (methodIterator.hasNext()) {
				Method method = methodIterator.next();
				if (method.getDeclaringClass() == flagClass) {
					methodIterator.remove();
				}
			}
			
			Unregister.Unregisterer.runUnregisterMethods(flagClass, heavySpleef, true, true);
			path = entry.getKey().getPrimaryKey();
			
			for (Game game : heavySpleef.getGameManager().getGames()) {
				if (!game.isFlagPresent(flagClass)) {
					continue;
				}
				
				AbstractFlag<?> flag = game.getFlag(flagClass);
				game.removeFlag(flagClass);
				
				Element element = DocumentHelper.createElement("flag");
				element.addAttribute("name", path);
				flag.marshal(element);
				
				UnloadedFlag unloaded = new UnloadedFlag();
				unloaded.setXmlElement(element);
				game.addFlag(unloaded, false);
			}
			break;
		}
		
		if (path != null) {
			registeredFlagsMap.remove(path);
		}
	}
	
	public Flag getFlagData(Class<? extends AbstractFlag<?>> clazz) {
		for (Entry<FlagClassHolder, DualKeyMap.DualKeyPair<String, Flag>> entry : registeredFlagsMap.inverse().entrySet()) {
			FlagClassHolder holder = entry.getKey();
			if (clazz != holder.flagClass) {
				continue;
			}
			
			return entry.getValue().getSecondaryKey();
		}
		
		throw new NoSuchFlagException(clazz.getName());
	}
	
	public boolean isFlagPresent(String flagPath) {
		return getFlagClass(flagPath) != null;
	}
	
	public boolean isFlagPresent(Class<? extends AbstractFlag<?>> flagClass) {
		for (FlagClassHolder holder : registeredFlagsMap.values()) {
			if (holder.flagClass == flagClass) {
				return true;
			}
		}
		
		return false;
	}
	
	/* Reverse path lookup */
	public Class<? extends AbstractFlag<?>> getFlagClass(String flagPath) {
		for (Entry<DualKeyMap.DualKeyPair<String, Flag>, FlagClassHolder> entry : registeredFlagsMap.entrySet()) {
			if (!entry.getKey().getPrimaryKey().equalsIgnoreCase(flagPath)) {
				continue;
			}
			
			return entry.getValue().flagClass;
		}
		
		return null;
	}
	
	public String getFlagPath(Class<? extends AbstractFlag<?>> flagClass) {
		for (Entry<DualKeyMap.DualKeyPair<String, Flag>, FlagClassHolder> entry : registeredFlagsMap.entrySet()) {
			if (entry.getValue().flagClass != flagClass) {
				continue;
			}
			
			return entry.getKey().getPrimaryKey();
		}
		
		return null;
	}
	
	public DualKeyBiMap<String, Flag, Class<? extends AbstractFlag<?>>> getAvailableFlags() {
		return DualMaps.valueMappedImmutableDualBiMap(registeredFlagsMap, valueMapper);
	}
	
	public <T extends AbstractFlag<?>> T newFlagInstance(String name, Class<T> expected) {
		return newFlagInstance(name, expected, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractFlag<?>> T newFlagInstance(String name, Class<T> expected, Game game) {
		FlagClassHolder holder = registeredFlagsMap.get(name);
		if (holder == null) {
			throw new NoSuchFlagException(name);
		}

        if (expected == null) {
            throw new IllegalArgumentException("expected cannot be null");
        }

		if (!expected.isAssignableFrom(holder.flagClass)) {
			throw new NoSuchFlagException("Expected class " + expected.getName() + " is not compatible with " + holder.flagClass.getName());
		}
		
		//Do a check on plugin dependencies
		Flag annotation = registeredFlagsMap.inverse().get(holder).getSecondaryKey();
		if (!checkHooks(annotation)) {
			throw new IllegalStateException("Cannot instantiate flag when its plugin dependencies are not available");
		}
		
		if (!holder.staticFieldsInjected) {
			inject(holder, null, null);
			holder.staticFieldsInjected = true;
		}
		
		if (!holder.staticMethodsInitialized) {
			runInitMethods(holder);
			holder.staticMethodsInitialized = true;
		}
		
		try {
			AbstractFlag<?> flag = holder.flagClass.newInstance();
			flag.setHeavySpleef(heavySpleef);
			flag.setI18N(holder.supplier.supply());
			
			inject(holder, flag, game);
			
			return (T) flag;
		} catch (InstantiationException | IllegalAccessException e) {
			//This should not happen as we made the constructor
			//accessible while the class was registered
			
			//But to be sure throw a RuntimeException
			throw new RuntimeException(e);
		}
	}
	
	private boolean checkHooks(Flag annotation) {
		HookReference[] refs = annotation.depend();
		String[] pluginDepends = annotation.pluginDepend();
		
		HookManager hookManager = heavySpleef.getHookManager();
		PluginManager pluginManager = heavySpleef.getPlugin().getServer().getPluginManager();
		boolean hooksPresent = true;
		
		for (HookReference ref : refs) {
			if (!hookManager.getHook(ref).isProvided()) {
				hooksPresent = false;
			}
		}
		
		for (String pluginDepend : pluginDepends) {
			if (!pluginManager.isPluginEnabled(pluginDepend)) {
				hooksPresent = false;
			}
		}
		
		return hooksPresent;
	}
	
	private void inject(FlagClassHolder holder, AbstractFlag<?> instance, final Game game) {
		Field[] injectingFields;
		Class<? extends AbstractFlag<?>> clazz = holder.flagClass;
		
		if (instance == null) {
			injectingFields = getInjectableDeclaredFieldsByFilter(clazz, new FieldFilter(FieldFilter.STATIC_MODE));
		} else {
			if (holder.injectingFields != null) {
				injectingFields = holder.injectingFields;
			} else {
				injectingFields = getInjectableDeclaredFieldsByFilter(clazz, new FieldFilter(FieldFilter.INSTANCE_MODE));
			}
		}
		
		if (instance != null) {
			Injector<AbstractFlag<?>> baseInjector = new Injector<AbstractFlag<?>>() {

				@Override
				public void inject(AbstractFlag<?> instance, Field[] injectableFields, Object cookie) throws IllegalArgumentException,
						IllegalAccessException {
					DefaultConfig config = heavySpleef.getConfiguration(ConfigType.DEFAULT_CONFIG);
					
					for (Field field : injectableFields) {
						if (field.getType() == Game.class) {
							field.set(instance, game);
						} else if (field.getType() == DefaultConfig.class) {
							field.set(instance, config);
						}
					}
				}
			};
			
			try {
				baseInjector.inject(instance, injectingFields, holder);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Failed to run basic injector", e);
			}
		}
		
		for (Injector<AbstractFlag<?>> injector : (instance == null ? staticInjectors : instanceInjectors)) {
			try {
				injector.inject(instance, injectingFields, holder);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("Could not inject fields when running injector '" + injector.getClass().getName() + "'", e);
			}
		}
	}
	
	private Field[] getInjectableDeclaredFieldsByFilter(Class<?> clazz, Predicate<Field> predicate) {
		Set<Field> fieldList = Sets.newHashSet();		
		Class<?> currentClass = clazz;
		
		do {
			Field[] fields = currentClass.getDeclaredFields();
			
			for (Field field : fields) {
				if (!field.isAnnotationPresent(Inject.class)) {
					continue;
				}
				
				if (!predicate.apply(field)) {
					continue;
				}
				
				field.setAccessible(true);
				fieldList.add(field);
			}
			
			currentClass = currentClass.getSuperclass();
		} while (AbstractFlag.class.isAssignableFrom(currentClass));
		
		return fieldList.toArray(new Field[fieldList.size()]);
	}
	
	public void flushAndExecuteInitMethods() {
		while (!queuedInitMethods.isEmpty()) {
			Method method = queuedInitMethods.poll();
			runInitMethod(method);
		}
	}
	
	private void runInitMethods(FlagClassHolder holder) {
		Method[] initMethods = getInitMethods(holder);
		for (Method method : initMethods) {
			runInitMethod(method);
		}
	}
	
	private void runInitMethod(Method method) {
		Class<?>[] parameters = method.getParameterTypes();
		Object[] args = new Object[parameters.length];
		
		for (int i = 0; i < parameters.length; i++) {
			Class<?> parameter = parameters[i];
			if (parameter == HeavySpleef.class) {
				args[i] = heavySpleef;
			}
		}
		
		try {
			method.invoke(null, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException("Could not invoke flag initialization method " + method.getName() + " of type "
					+ method.getDeclaringClass().getCanonicalName() + ": ", e);
		}
	}
	
	private Method[] getInitMethods(FlagClassHolder holder) {
		Class<? extends AbstractFlag<?>> clazz = holder.flagClass;
		List<Method> methods = Lists.newArrayList();
		
		for (Method method : clazz.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(FlagInit.class)) {
				continue;
			}
			
			if ((method.getModifiers() & Modifier.STATIC) == 0) {
				continue;
			}
			
			method.setAccessible(true);
			methods.add(method);
		}
		
		return methods.toArray(new Method[methods.size()]);
	}
	
	public List<Class<? extends AbstractFlag<?>>> getChildFlags(Class<? extends AbstractFlag<?>> flagClass) {
		List<Class<? extends AbstractFlag<?>>> childs = Lists.newArrayList();
		for (FlagClassHolder holder : registeredFlagsMap.values()) {
			if (!isChildFlag(flagClass, holder.flagClass)) {
				continue;
			}
			
			childs.add(holder.flagClass);
		}
		
		return childs;
	}
	
	public boolean isChildFlag(Class<? extends AbstractFlag<?>> parent, Class<? extends AbstractFlag<?>> childCandidate) {
		Validate.notNull(parent, "Parent cannot be null");
		Validate.notNull(childCandidate, "Child candidate cannot be null");
		
		BiMap<FlagClassHolder, DualKeyMap.DualKeyPair<String, Flag>> inverse = registeredFlagsMap.inverse();
		FlagClassHolder foundHolder = null;
		
		for (FlagClassHolder holder : inverse.keySet()) {
			if (holder.flagClass == childCandidate) {
				foundHolder = holder;
				break;
			}
		}
		
		Validate.isTrue(foundHolder != null, "Child candidate flag " + childCandidate.getName() + " has not been registered");
		
		Flag annotation = inverse.get(foundHolder).getSecondaryKey();
		
		Validate.isTrue(annotation != null, "ChildCandidate has not been registered");
		boolean directChild = annotation != null && annotation.parent() != NullFlag.class && annotation.parent() == parent;
		if (directChild) {
			return true;
		}
		
		//Do a search on the path
		Class<? extends AbstractFlag<?>> recentParent = annotation.parent();
		
		loop: do {
			if (recentParent == childCandidate) {
				return true;
			}
			
			for (FlagClassHolder holder : inverse.keySet()) {
				if (holder.flagClass != recentParent) {
					continue;
				}
				
				recentParent = holder.flagClass;
				continue loop;
			}
			
			recentParent = null;
		} while (recentParent != null && recentParent != NullFlag.class);
		//NullFlag is the root parent as annotations require non-null values
		
		return false;
	}
	
	public void registerInjector(Injector<AbstractFlag<?>> injector) {
		staticInjectors.add(injector);
		instanceInjectors.add(injector);
	}
	
	public void registerInjector(Injector<AbstractFlag<?>> injector, boolean isStatic) {
		Set<Injector<AbstractFlag<?>>> injectorSet = isStatic ? staticInjectors : instanceInjectors;
		
		Validate.isTrue(!injectorSet.contains(injector), "Injector already registered");
		Validate.notNull(injector, "Injector cannot be null");
		
		injectorSet.add(injector);
	}
	
	public void unregisterInjector(Injector<AbstractFlag<?>> injector) {
		Validate.isTrue(instanceInjectors.contains(injector) || staticInjectors.contains(injector), "Injector has not been registered");
		
		instanceInjectors.remove(injector);
		staticInjectors.remove(injector);
	}
	
	public void setInitializationPolicy(InitializationPolicy policy) {
		this.initializationPolicy = policy;
	}
	
	public enum InitializationPolicy {
		
		REGISTER,
		COMMIT;
		
	}
	
	public static interface I18NSupplier {
		
		public I18N supply();
		
	}
	
	private class FieldFilter implements Predicate<Field> {
		
		public static final int INSTANCE_MODE = 0;
		public static final int STATIC_MODE = 1;
		
		private int mode;
		
		public FieldFilter(int mode) {
			this.mode = mode;
		}
		
		@Override
		public boolean apply(Field input) {
			int modifiers = input.getModifiers();
			int result = (modifiers & Modifier.STATIC);
			
			return mode == 0 ? result == 0 : mode != 1 || result != 0;
		}
	};
	
	public class FlagClassHolder {
		
		private Class<? extends AbstractFlag<?>> flagClass;
		private Field[] injectingFields;
		private I18NSupplier supplier;
		private Object cookie;
		private boolean staticFieldsInjected;
		private boolean staticMethodsInitialized;
		
		public Class<? extends AbstractFlag<?>> getFlagClass() {
			return flagClass;
		}
		
		public Field[] getInjectingFields() {
			return injectingFields;
		}
		
		public I18NSupplier getSupplier() {
			return supplier;
		}
		
		public Object getCookie() {
			return cookie;
		}
		
		public boolean isStaticFieldsInjected() {
			return staticFieldsInjected;
		}
		
		public boolean isStaticMethodsInitialized() {
			return staticMethodsInitialized;
		}
		
	}

}