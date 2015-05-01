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
package de.matzefratze123.heavyspleef.commands.base;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.Sets;

public class CommandContainer {
	
	private Command command;
	@Getter @Setter
	private CommandExecution execution;
	@Getter @Setter(value = AccessLevel.PROTECTED)
	private Set<CommandContainer> childCommands;
	private @Getter boolean playerOnly;
	private @Getter CommandContainer parent;
	@Getter(value = AccessLevel.PROTECTED)
	private Method commandMethod;
	private @Getter Object commandClassInstance;
	
	protected CommandContainer(Method method, Object instance, Command command, boolean playerOnly, CommandExecution execution) {
		this.commandMethod = method;
		this.commandClassInstance = instance;
		this.command = command;
		this.playerOnly = playerOnly;
		this.execution = execution;
	}
	
	protected CommandContainer(Method method, Object instance, Command command, boolean playerOnly, CommandExecution execution, CommandContainer parent) {
		this(method, instance, command, playerOnly, execution);
		
		this.parent = parent;
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
	
	public static Set<CommandContainer> create(Class<?> rootClass, Instantiator instantiator, CommandExecution execution, Logger logger) {
		return buildHierarchy(new Class<?>[] {rootClass}, instantiator, null, logger, execution);
	}
	
	private static Set<CommandContainer> buildHierarchy(
			Class<?>[] nestedClasses, Instantiator instantiator,
			CommandContainer parent, Logger logger,
			CommandExecution execution) {
		Set<CommandContainer> containers = Sets.newHashSet();
		
		for (Class<?> clazz : nestedClasses) {
			for (Method method : clazz.getDeclaredMethods()) {
				if (!method.isAnnotationPresent(Command.class)) {
					continue;
				}
				
				Command command = method.getAnnotation(Command.class);
				Set<CommandContainer> childCommands = null;
				boolean playerOnly = method.isAnnotationPresent(PlayerOnly.class);
				
				Object instance;
				
				try {
					instance = instantiator.instantiate(clazz);
				} catch (InstantiationException e) {
					logger.warning("Could not instantiate class " + clazz.getName() + ": " + e.getMessage());
					logger.warning("Ignoring command class...");
					continue;
				}
				
				CommandContainer container = new CommandContainer(method, instance, command, playerOnly, execution, parent);
				
				if (method.isAnnotationPresent(NestedCommands.class)) {
					Class<?>[] nestedCommandClasses = method.getAnnotation(NestedCommands.class).value();
					
					childCommands = buildHierarchy(nestedCommandClasses, instantiator, container, logger, execution);
				}
				
				container.setChildCommands(childCommands);
				containers.add(container);
			}
		}
		
		return containers;
	}
	
}
