package de.matzefratze123.heavyspleef.commands.internal;

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

import de.matzefratze123.heavyspleef.core.HeavySpleef;

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
	
	private HeavySpleef heavySpleef;
	private JavaPlugin plugin;
	private Logger logger;
	private Instantiator instantiator;
	private Map<String, CommandContainer> commandMap;
	private Map<Class<?>, Transformer<?>> transformers;
	private Properties messageProperties;
	
	public CommandManagerService(HeavySpleef heavySpleef, Logger logger) {
		this.heavySpleef = heavySpleef;
		this.plugin = heavySpleef.getPlugin();
		this.logger = logger;
		this.instantiator = new UnsafeInstantiator();
		this.commandMap = Maps.newHashMap();
		this.transformers = Maps.newHashMap(BaseTransformers.BASE_TRANSFORMERS);
	}
	
	public abstract boolean checkPermission(CommandSender sender, String permission);
	
	public void registerCommands(Class<?> clazz) {
		Validate.notNull(clazz);
		
		Set<CommandContainer> commands = CommandContainer.create(clazz, instantiator, logger);
		Iterator<CommandContainer> iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			CommandContainer command = iterator.next();
			
			if (commands.contains(command.getName())) {
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
		}
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
	
	public void setMessages(Properties props) {
		this.messageProperties = props;
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
		boolean subFound = true;
		while (index < args.length && subFound) {
			Set<CommandContainer> childs = command.getChildCommands();
			
			for (CommandContainer child : childs) {
				if (child.getName().equals(args[index])) {
					command = child;
					index++;
					continue;
				}
			}
			
			subFound = false;
		}
		
		if (!(sender instanceof Player) && command.isPlayerOnly()) {
			sender.sendMessage(messageProperties.getProperty("message.player_only"));
			return true;
		}
		
		if (!command.getPermission().isEmpty() && !checkPermission(sender, command.getPermission())) {
			sender.sendMessage(messageProperties.getProperty("message.no_permission"));
			return true;
		}
		
		//Cut the args to be suitable to the sub-command-deepness
		String[] cutArgs = new String[args.length - index];
		System.arraycopy(args, index, cutArgs, 0, args.length - index);
		
		if (cutArgs.length > 0 && isHelpArg(cutArgs[0])) {
			sender.sendMessage(String.format(messageProperties.getProperty("message.description_format"), command.getDescription()));
			return true;
		}
		
		if (cutArgs.length < command.getMinArgs()) {
			sender.sendMessage(String.format(messageProperties.getProperty("message.usage_format"), command.getUsage()));
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
						break;
					} else if (plugin.getClass() == parameterType) {
						parameterValues[i] = plugin;
					} else if (heavySpleef.getClass() == parameterType) {
						parameterValues[i] = heavySpleef;
					} else if (parameterType.isPrimitive()) {
						parameterValues[i] = getDefaultPrimitiveValue(parameterType);
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
		for (String helpIdentifier : HELP_IDENTIFIERS) {
			if (helpIdentifier.equalsIgnoreCase(arg)) {
				return true;
			}
		}
		
		return false;
	}
 	
}
