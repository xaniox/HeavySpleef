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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;

public class DefaultCommandExecution implements CommandExecution {

	private static final String[] HELP_IDENTIFIERS = {"?", "help"};
	
	private String prefix;
	private Plugin plugin;
	private Logger logger;
	
	public DefaultCommandExecution(Plugin plugin, String prefix) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.prefix = prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public void execute(CommandContext context, MessageBundle bundle, PermissionChecker checker, Object[] executionArgs) {
		CommandContainer command = context.getCommand();
		CommandSender sender = context.getSender();
		String prefix = this.prefix != null ? this.prefix : "";
		
		if (!(sender instanceof Player) && command.isPlayerOnly()) {
			sender.sendMessage(prefix + bundle.getMessage("message-player_only"));
			return;
		}
		
		if (!command.getPermission().isEmpty() && !checker.checkPermission(sender, command.getPermission())) {
			sender.sendMessage(prefix + bundle.getMessage("message-no_permission"));
			return;
		}
		
		if (context.argsLength() > 0 && isHelpArg(context.getStringSafely(0))) {
			String description = command.getDescription();
			
			if (description.isEmpty() && !command.getDescriptionRef().isEmpty()) {
				description = bundle.getMessage(command.getDescriptionRef());
			}
			
			sender.sendMessage(prefix + bundle.getMessage("message-description_format", command.getDescription()));
			return;
		}
		
		if (context.argsLength() < command.getMinArgs()) {
			sender.sendMessage(prefix + bundle.getMessage("message-usage_format", command.getUsage()));
			return;
		}
		
		invokeMethod(context, null, executionArgs, false);
	}
	
	@Override
	public List<String> tabComplete(CommandContext context, PermissionChecker checker, Object[] args) {
		CommandSender sender = context.getSender();
		CommandContainer command = context.getCommand();
		if (command.getTabCompleteMethod() == null) {
			return null;
		}
		
		if (!(sender instanceof Player) && command.isPlayerOnly()) {
			return null;
		}
		
		if (!command.getPermission().isEmpty() && !checker.checkPermission(sender, command.getPermission())) {
			return null;
		}
		
		List<String> tabCompleteList = Lists.newArrayList();
		invokeMethod(context, tabCompleteList, args, true);
		return tabCompleteList;
	}
	
	protected Object invokeMethod(CommandContext context, List<String> tabCompleteList, Object[] args, boolean completeTabsInvoke) {
		CommandContainer command = context.getCommand();
		
		Method method = completeTabsInvoke ? command.getTabCompleteMethod() : command.getCommandMethod();
		Object instance = context.getCommand().getCommandClassInstance();
		
		boolean accessible = method.isAccessible();
		method.setAccessible(true);
		
		//Analyse the method
		//Default method format is: methodName(CommandContext, OptionalArgs)
		
		//Tabformat is: methodName(CommandContext, List<String>, OptionalArgs)
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
					} else if (parameterType == List.class) {
						parameterValues[i] = tabCompleteList;
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
				
				return method.invoke(instance, parameterValues);
			}
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();

			if (cause instanceof CommandException) {
				((CommandException) cause).sendToPlayer(prefix, context.getSender());
			} else {
				logger.log(Level.SEVERE, "Unhandled exception executing " + (completeTabsInvoke ? "tab complete for " : "") + "command \""
						+ context.getCommand().getName() + "\"", cause);
				context.getSender().sendMessage(ChatColor.RED + "An internal error occured while executing this command. Please see the server log.");
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			logger.log(Level.SEVERE, "Could not invoke " + (completeTabsInvoke ? "tab complete " : "") + "method for '"
					+ context.getCommand().getFullyQualifiedName() + "'", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unhandled exception executing " + (completeTabsInvoke ? "tab complete for " : "") + "command '"
					+ context.getCommand().getFullyQualifiedName() + "'", e);
		} finally {
			method.setAccessible(accessible);
		}
		
		return null;
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
