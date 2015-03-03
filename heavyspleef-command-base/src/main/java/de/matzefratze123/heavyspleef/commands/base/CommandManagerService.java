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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

public abstract class CommandManagerService implements CommandExecutor {
	
	private static final Properties DEFAULT_MESSAGES;
	private static final String[] HELP_IDENTIFIERS = {"?", "help"};
	
	static {
		DEFAULT_MESSAGES = new Properties();
		DEFAULT_MESSAGES.put("message.player_only", "This command can only be executed by a player!");
		DEFAULT_MESSAGES.put("message.no_permission", "You don't have permission to execute this command!");
		DEFAULT_MESSAGES.put("message.description_format", "Description: %s");
		DEFAULT_MESSAGES.put("message.usage_format", "Usage: %s");
	}
	
	private final JavaPlugin plugin;
	private final Logger logger;
	private Object[] args;
	private Instantiator instantiator;
	private Map<String, CommandContainer> commandMap;
	private Map<Class<?>, Transformer<?>> transformers;
	
	public CommandManagerService(JavaPlugin plugin, Logger logger, Object... args) {
		this.plugin = plugin;
		this.logger = logger;
		this.args = args;
		this.instantiator = new UnsafeInstantiator();
		this.commandMap = Maps.newHashMap();
		this.transformers = Maps.newHashMap(BaseTransformers.BASE_TRANSFORMERS);
	}
	
	public abstract boolean checkPermission(CommandSender sender, String permission);
	
	public abstract String getMessage(String key, String... messageArgs);
	
	public String getMessage0(String key, String... messageArgs) {
		String message = getMessage(key, messageArgs);
		if (message == null) {
			message = DEFAULT_MESSAGES.getProperty(key);
		}
		
		return message;
	}
	
	public void registerCommands(Class<?> clazz) {
		registerCommands(clazz, null);
	}
	
	public void registerCommands(Class<?> clazz, CommandContainer base) {
		Validate.notNull(clazz);
		
		Set<CommandContainer> commands = CommandContainer.create(clazz, instantiator, logger);
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
					logger.warning("command " + command.getName() + " registered but could not find a matching command for plugin " + plugin.getName() + ". Did you forget to add the command to your plugin.yml?");
				}
			} else {
				// Just add it as a child
				base.addChild(command);
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
	
	public <T> void registerTransformer(Class<T> returnType, Transformer<T> transformer) {
		Validate.notNull(returnType);
		Validate.notNull(transformer);
		
		transformers.put(returnType, transformer);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Transformer<T> getTransformer(Class<T> clazz) {
		return (Transformer<T>) transformers.get(clazz);
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
		
		if (!(sender instanceof Player) && command.isPlayerOnly()) {
			sender.sendMessage(getMessage0("message.player_only"));
			return true;
		}
		
		if (!command.getPermission().isEmpty() && !checkPermission(sender, command.getPermission())) {
			sender.sendMessage(getMessage0("message.no_permission"));
			return true;
		}
		
		//Cut the args to be suitable to the sub-command-deepness
		String[] cutArgs = new String[args.length - index];
		System.arraycopy(args, index, cutArgs, 0, args.length - index);
		
		if (cutArgs.length > 0 && isHelpArg(cutArgs[0])) {
			sender.sendMessage(getMessage0("message.description_format", command.getDescription()));
			return true;
		}
		
		if (cutArgs.length < command.getMinArgs()) {
			sender.sendMessage(getMessage0("message.usage_format", command.getUsage()));
			return true;
		}
		
		CommandContext context = new CommandContext(this, cutArgs, command, sender);
		executeCommand(context);
		
		return true;
	}

	protected void executeCommand(CommandContext context) {
		Method method = context.getCommand().getCommandMethod();
		Object instance = context.getCommand().getCommandClassInstance();
		
		boolean accessible = method.isAccessible();
		method.setAccessible(true);
		
		//Analyse the method
		//Default method format is: methodName(CommandContext)
		
		try {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length == 0) {
				//No parameters in this method, so just invoke it
				method.invoke(instance);
			} else {
				Object[] parameterValues = new Object[parameterTypes.length];
				
				for (int i = 0; i < parameterTypes.length; i++) {
					Class<?> parameterType = parameterTypes[i];
					
					if (parameterType == CommandContext.class) {
						parameterValues[i] = context;
					} else if (plugin.getClass() == parameterType) {
						parameterValues[i] = plugin;
					} else if (parameterType.isPrimitive()) {
						parameterValues[i] = getDefaultPrimitiveValue(parameterType);
					} else {
						for (Object arg : args) {
							if (parameterType.isInstance(arg)) {
								parameterValues[i] = arg;
								break;
							}
						}
					}
				}
				
				method.invoke(instance, parameterValues);
			}
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			
			if (cause instanceof CommandException) {
				((CommandException) cause).sendToPlayer(context.getSender());
			} else {
				logger.log(Level.SEVERE, "Unhandled exception executing command \"" + context.getCommand().getName() + "\"", cause);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			logger.log(Level.SEVERE, "Could not invoke command method for '" + context.getCommand().getFullyQualifiedName() + "'", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unhandled exception executing command '" + context.getCommand().getFullyQualifiedName() + "'", e);
		} finally {
			method.setAccessible(accessible);
		}
	}
	
	private Object getDefaultPrimitiveValue(Class<?> clazz) {
		if (!clazz.isPrimitive()) {
			return null;
		}
		
		Object value = null;
		
		if (clazz == int.class || clazz == long.class || clazz == short.class
				|| clazz == byte.class || clazz == double.class
				|| clazz == float.class) {
			value = 0;
		} else if (clazz == boolean.class) {
			value = false;
		}
		
		return value;
	}
	
	private boolean isHelpArg(String arg) {
		for (String identifier : HELP_IDENTIFIERS) {
			if (identifier.equalsIgnoreCase(arg)) {
				return true;
			}
		}
		
		return false;
	}
 	
}
