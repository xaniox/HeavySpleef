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
package de.xaniox.heavyspleef.commands.base;

import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandContainer {
	
	private Command command;
	private CommandExecution execution;
	private Set<CommandContainer> childCommands;
	private boolean playerOnly;
	private CommandContainer parent;
	private Method commandMethod;
	private Method tabCompleteMethod;
	private Object commandClassInstance;
	
	protected CommandContainer(Method method, Method tabCompleteMethod, Object instance, Command command, boolean playerOnly, CommandExecution execution) {
		this.commandMethod = method;
		this.commandClassInstance = instance;
		this.command = command;
		this.playerOnly = playerOnly;
		this.execution = execution;
	}
	
	protected CommandContainer(Method method, Method tabCompleteMethod, Object instance, Command command, boolean playerOnly, CommandExecution execution, CommandContainer parent) {
		this(method, tabCompleteMethod, instance, command, playerOnly, execution);
		
		this.parent = parent;
	}
	
	public CommandExecution getExecution() {
		return execution;
	}
	
	public void setExecution(CommandExecution execution) {
		this.execution = execution;
	}
	
	public Set<CommandContainer> getChildCommands() {
		return childCommands;
	}
	
	protected void setChildCommands(Set<CommandContainer> childCommands) {
		this.childCommands = childCommands;
	}
	
	public boolean isPlayerOnly() {
		return playerOnly;
	}
	
	public CommandContainer getParent() {
		return parent;
	}
	
	protected void setParent(CommandContainer parent) {
		this.parent = parent;
	}
	
	protected Method getCommandMethod() {
		return commandMethod;
	}
	
	protected Method getTabCompleteMethod() {
		return tabCompleteMethod;
	}
	
	public Object getCommandClassInstance() {
		return commandClassInstance;
	}
	
	public String getName() {
		return command.name().toLowerCase();
	}
	
	public String getFullyQualifiedName() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName());
		
		CommandContainer currentParent = parent;
		while (currentParent != null) {
			builder.insert(0, "/");
			builder.insert(0, currentParent.getName());
			
			currentParent = currentParent.getParent();
		}
		
		return builder.toString();
	}
	
	public String getUsage() {
		return command.usage();
	}
	
	public String getDescription() {
		return command.description();
	}
	
	public String getDescriptionRef() {
		return command.descref();
	}
	
	public String getI18NRef() {
		return command.i18nref();
	}
	
	public int getMinArgs() {
		return command.minArgs();
	}
	
	public String getPermission() {
		return command.permission();
	}
	
	public CommandContainer child(String identifier) {
		for (CommandContainer child : childCommands) {
			if (child.getName().equalsIgnoreCase(identifier)) {
				return child;
			}
		}
		
		return null;
	}
	
	void addChild(CommandContainer child) {
		childCommands.add(child);
	}
	
	public void execute(CommandContext context, MessageBundle messageBundle, PermissionChecker permissionChecker, Object[] args) {
		execution.execute(context, messageBundle, permissionChecker, args);
	}
	
	public List<String> tabComplete(CommandContext context, PermissionChecker permissionChecker, Object[] args) {
		return execution.tabComplete(context, permissionChecker, args);
	}

    @Deprecated
    public static Set<CommandContainer> create(Class<?> rootClass, Instantiator instantiator, CommandExecution execution, Logger logger) {
        return create(rootClass, null, instantiator, execution, logger);
    }

    public static Set<CommandContainer> create(Class<?> rootClass, CommandContainer parent, Instantiator instantiator, CommandExecution execution, Logger logger) {
		return buildHierarchy(new Class<?>[] {rootClass}, instantiator, parent, logger, execution);
	}
	
	private static Set<CommandContainer> buildHierarchy(
			Class<?>[] nestedClasses, Instantiator instantiator,
			CommandContainer parent, Logger logger,
			CommandExecution execution) {
		Set<CommandContainer> containers = Sets.newHashSet();
		
		for (Class<?> clazz : nestedClasses) {
			//Searching for commands
			for (Method method : clazz.getDeclaredMethods()) {
				if (!method.isAnnotationPresent(Command.class)) {
					continue;
				}
				
				Command command = method.getAnnotation(Command.class);
				Set<CommandContainer> childCommands = null;
				boolean playerOnly = method.isAnnotationPresent(PlayerOnly.class);
				
				Object instance = null;
				
				if ((method.getModifiers() & Modifier.STATIC) == 0) { 
					try {
						instance = instantiator.instantiate(clazz);
					} catch (InstantiationException e) {
						logger.warning("Could not instantiate class " + clazz.getName() + ": " + e.getMessage());
						logger.warning("Ignoring command class...");
						continue;
					}
				}
				
				CommandContainer container = new CommandContainer(method, null, instance, command, playerOnly, execution, parent);
				
				if (method.isAnnotationPresent(NestedCommands.class)) {
					Class<?>[] nestedCommandClasses = method.getAnnotation(NestedCommands.class).value();
					
					childCommands = buildHierarchy(nestedCommandClasses, instantiator, container, logger, execution);
				}
				
				container.setChildCommands(childCommands);
				containers.add(container);
			}
			
			//Searching for tab completers which are defined in the same class
			for (Method method : clazz.getDeclaredMethods()) {
				if (!method.isAnnotationPresent(TabComplete.class)) {
					continue;
				}
				
				TabComplete tabComplete = method.getAnnotation(TabComplete.class);
				String command = tabComplete.value();
				
				CommandContainer found = null;
				
				for (CommandContainer container : containers) {
					if (!container.getName().equals(command)) {
						continue;
					}
					
					found = container;
					break;
				}
				
				if (found == null) {
					logger.log(Level.WARNING, "TabComplete method " + method.getName() + " in class " + method.getDeclaringClass().getName()
							+ " found but no suitable command was found!");
					continue;
				}
				
				found.tabCompleteMethod = method;
			}
		}
		
		return containers;
	}
	
}