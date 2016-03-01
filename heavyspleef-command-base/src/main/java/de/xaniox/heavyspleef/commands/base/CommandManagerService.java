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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class CommandManagerService implements CommandExecutor, TabCompleter {
	
	private static Map<Class<?>, Transformer<?>> TRANSFORMERS = Maps.newHashMap(BaseTransformers.BASE_TRANSFORMERS);
	
	private final JavaPlugin plugin;
	private final Logger logger;
	private final DefaultCommandExecution execution;
	private List<Object> args;
	private Instantiator instantiator;
	private MessageBundle messageBundle;
	private PermissionChecker permissionChecker;
	private Map<String, CommandContainer> commandMap;
	
	public CommandManagerService(JavaPlugin plugin, Logger logger, MessageBundle.MessageProvider messageProvider, PermissionChecker permissionChecker, Object... args) {
		this.plugin = plugin;
		this.logger = logger;
		this.args = Lists.newArrayList(args);
		this.instantiator = new UnsafeInstantiator();
		this.commandMap = Maps.newHashMap();
		
		InputStream defaultMessagesStream = getClass().getResourceAsStream("/command_messages.yml");
		this.messageBundle = new MessageBundle(messageProvider, defaultMessagesStream);
		this.permissionChecker = permissionChecker;
		this.execution = new DefaultCommandExecution(plugin, null);
	}
	
	public DefaultCommandExecution getExecution() {
		return execution;
	}
	
	public static <T> void registerTransformer(Class<T> returnType, Transformer<T> transformer) {
		Validate.notNull(returnType);
		Validate.notNull(transformer);
		
		TRANSFORMERS.put(returnType, transformer);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Transformer<T> getTransformer(Class<T> clazz) {
		return (Transformer<T>) TRANSFORMERS.get(clazz);
	}
	
	public void addArgument(Object object) {
		args.add(object);
	}
	
	public void removeArgument(Object object) {
		args.remove(object);
	}
		
	public void registerCommands(Class<?> clazz) {
		registerCommands(clazz, null);
	}
	
	public void registerCommands(Class<?> clazz, CommandContainer base) {
		Validate.notNull(clazz);
		
		Set<CommandContainer> commands = CommandContainer.create(clazz, base, instantiator, execution, logger);
		Iterator<CommandContainer> iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			CommandContainer command = iterator.next();
			
			if (base == null) {
				if (commandMap.containsKey(command.getName())) {
					logger.warning("duplicate command " + command.getName() + "!");
					continue;
				}
				
				commandMap.put(command.getName(), command);
				
				PluginCommand bukkitCommand = plugin.getCommand(command.getName());
				if (bukkitCommand != null) {
					bukkitCommand.setExecutor(this);
				} else {
					logger.warning("Command " + command.getName() + " registered but could not find a matching command for plugin " + plugin.getName() + ". Did you forget to add the command to your plugin.yml?");
				}
			} else {
				// Just add it as a child
				base.addChild(command);
			}
		}
	}
	
	public void unregisterCommands(Class<?> clazz, CommandContainer base) {
		Iterator<CommandContainer> iterator;
		
		if (base == null) {
			iterator = commandMap.values().iterator();
		} else {
			iterator = base.getChildCommands().iterator();
		}
		
		unregisterRecursively(clazz, iterator);
	}
	
	private void unregisterRecursively(Class<?> clazz, Iterator<CommandContainer> iterator) {
		while (iterator.hasNext()) {
			CommandContainer container = iterator.next();
			Method method = container.getCommandMethod();
			Set<CommandContainer> childs = container.getChildCommands();
			
			if (method.getDeclaringClass() == clazz) {
				iterator.remove();
			} else if (childs != null && !childs.isEmpty()) {
				unregisterRecursively(clazz, childs.iterator());
			}
		}
	}
	
	public CommandContainer containerOf(String path) {
		Validate.notNull(path, "path cannot be null");
		Validate.isTrue(!path.isEmpty(), "path cannot be empty");
		
		String[] pathComponents = path.split("/");
		
		CommandContainer current = commandMap.get(pathComponents[0]);
		if (current == null) {
			return null;
		}
		
		for (int i = 1; i < pathComponents.length; i++) {
			CommandContainer child = current.child(pathComponents[i]);
			
			if (child != null) {
				current = child;
			} else {
				break;
			}
		}
		
		return current;
	}
	
	public CommandContainer getCommand(String baseCommand) {
		return commandMap.get(baseCommand);
	}
	
	public void setInstantiator(Instantiator instantiator) {
		this.instantiator = instantiator;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {		
		String name = cmd.getName();
		if (!commandMap.containsKey(name)) {
			//That command doesn't belong to us
			return true;
		}
		
		SearchResult result = searchCommand(name, args);
		if (result == null || result.container == null) {
			sender.sendMessage(messageBundle.getMessage("message-unknown-command"));
			return true;
		}
		
		CommandContainer command = result.container;
		int deepness = result.deepness;
		
		//Cut the args to be suitable to the sub-command-deepness
		String[] cutArgs = new String[args.length - deepness];
		System.arraycopy(args, deepness, cutArgs, 0, args.length - deepness);
		
		CommandContext context = new CommandContext(cutArgs, command, sender);
		command.execute(context, messageBundle, permissionChecker, this.args.toArray(new Object[this.args.size()]));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		String name = cmd.getName();
		if (!commandMap.containsKey(name)) {
			//That command doesn't belong to us
			return null;
		}
		
		SearchResult result = searchCommand(name, args);
		if (result == null || result.container == null) {
			return null;
		}
		
		CommandContainer container = result.container;
		int deepness = result.deepness;
		
		//Cut the args to be suitable to the sub-command-deepness
		String[] cutArgs = new String[args.length - deepness];
		System.arraycopy(args, deepness, cutArgs, 0, args.length - deepness);
		
		CommandContext context = new CommandContext(cutArgs, container, sender);
		List<String> tabCompletes = container.tabComplete(context, permissionChecker, this.args.toArray(new Object[this.args.size()]));
		if (tabCompletes == null) {
			tabCompletes = Lists.newArrayList();
		}
		
		if (args.length > 0 && !args[args.length - 1].isEmpty()) {
			//Remove unrelevant completes
			String lastArgument = args[args.length - 1];
			
			Iterator<String> iterator = tabCompletes.iterator();
			while (iterator.hasNext()) {
				String complete = iterator.next().toLowerCase();
				
				if (!complete.startsWith(lastArgument.toLowerCase())) {
					iterator.remove();
				}
			}
		}
		
		return tabCompletes;
	}
	
	public SearchResult searchCommand(String name, String[] args) {
		CommandContainer command = commandMap.get(name);
		
		//Try to find the deepest available sub-command
		int index = 0;
		boolean subFound;
		
		if (args.length > 0) {
			do {
				subFound = false;
				
				Set<CommandContainer> childs = command.getChildCommands();
				
				if (childs != null) {
					for (CommandContainer child : childs) {
						if (child.getName().equals(args[index])) {
							command = child;
							index++;
							subFound = true;
							break;
						}
					}
				}
			} while (index < args.length && subFound);
		}
		
		SearchResult result = new SearchResult();
		result.container = command;
		result.deepness = index;
		
		return result;
	}
	
	public class SearchResult {
		
		private CommandContainer container;
		private int deepness;
		
		public CommandContainer getContainer() {
			return container;
		}
		
		public int getDeepness() {
			return deepness;
		}
		
	}
 	
}