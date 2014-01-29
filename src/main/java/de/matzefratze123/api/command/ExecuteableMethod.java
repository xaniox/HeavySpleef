package de.matzefratze123.api.command;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.api.command.CommandExecutorService.TransformerMap;
import de.matzefratze123.api.command.transform.TransformException;
import de.matzefratze123.api.command.transform.Transformer;

public class ExecuteableMethod {
	
	private static final String USAGE_VARIABLE = "%usage%";
	private static final String DESCRIPTION_VARIABLE = "%description%";
	
	private final Class<?>[] parameterArgTypes;
	
	private Method method;
	private CommandListener instance;
	
	private CommandData data;
	
	public ExecuteableMethod(CommandListener listener, Method method) {
		if (!method.isAnnotationPresent(Command.class))
			throw new IllegalArgumentException("No Command annotation at " + method.getName() + " in type " + method.getClass().getSimpleName() + " present!");
		
		Command cmdAnnotation = method.getAnnotation(Command.class);
		Class<?>[] parameterTypes = method.getParameterTypes();
		parameterArgTypes = new Class<?>[parameterTypes.length - 1];
		
		for (int i = 1; i < parameterTypes.length; i++) {
			parameterArgTypes[i - 1] = parameterTypes[i];
		}
		
		this.instance = listener;
		this.method = method;
		
		String name = cmdAnnotation.value();
		int minArgs = cmdAnnotation.minArgs();
		boolean onlyIngame = cmdAnnotation.onlyIngame();
		
		String[] permissions = null;
		
		String usage = null;
		String description = null;
		String usageStyle = null;
		
		String[] aliases = null;
		
		if (method.isAnnotationPresent(CommandPermissions.class)) {
			CommandPermissions permAnnotation = method.getAnnotation(CommandPermissions.class);
			
			permissions = new String[permAnnotation.value().length];
			
			for (int i = 0; i < permAnnotation.value().length; i++) {
				permissions[i] = permAnnotation.value()[i].getPerm();
			}
		}
		
		if (method.isAnnotationPresent(CommandHelp.class)) {
			CommandHelp helpAnnotation = method.getAnnotation(CommandHelp.class);
			
			usage = helpAnnotation.usage();
			description = helpAnnotation.description();
			usageStyle = helpAnnotation.usageStyle();
		}
		
		if (method.isAnnotationPresent(CommandAliases.class)) {
			CommandAliases aliasesAnnotation = method.getAnnotation(CommandAliases.class);
			
			aliases = aliasesAnnotation.value();
		}
		
		this.data = new CommandData(name, minArgs, onlyIngame, usage, description, usageStyle, permissions, aliases);
	}
	
	public static List<ExecuteableMethod> findListenerMethods(CommandListener listener) {
		List<ExecuteableMethod> executeableMethods = new ArrayList<ExecuteableMethod>();
		
		for (Method method : listener.getClass().getMethods()) {
			if (!method.isAnnotationPresent(Command.class)) {
				continue;
			}
			
			ExecuteableMethod em = new ExecuteableMethod(listener, method);
			executeableMethods.add(em);
		}
		
		return executeableMethods;
	}
	
	public void execute(CommandSender sender, String[] args, TransformerMap transformers) {
		if (data.getMinArgs() > 0 && args.length < data.getMinArgs()) {
			sender.sendMessage(data.getUsage() == null ? ChatColor.RED + "Too few arguments!" : ChatColor.RED + data.getUsage());
			return;
		}
		
		if (data.onlyIngame() && !(sender instanceof Player)) {
			sender.sendMessage("This command can only be used by a player");
			return;
		}
		
		if (data.getPermissions() != null) {
			for (String permission : data.getPermissions()) {
				if (!sender.hasPermission(permission)) {
					sender.sendMessage(ChatColor.RED + "You don't have permission.");
					return;
				}
			}
		}
		
		if (args.length > 0 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help") && data.getUsage() != null && data.getDescription() != null)) {
			String helpLine = data.getUsageStyle().replace(USAGE_VARIABLE, data.getUsage()).replace(DESCRIPTION_VARIABLE, data.getDescription());
			
			sender.sendMessage(helpLine);
			return;
		}
		
		Argument<?>[] arguments = parseArguments(transformers, args);
		Object[] values = new Object[parameterArgTypes.length + 1];
		
		values[0] = sender;
		for (int i = 1; i < arguments.length + 1; i++) {
			values[i] = arguments[i - 1] == null ? null : arguments[i - 1].getValue();
		}
		
		try {
			method.invoke(instance, values);
		} catch (Exception e) {
			String detailMessage = e.getMessage();
			
			if (e instanceof IllegalAccessException) {
				detailMessage = "Unable to access method";
			} else if (e instanceof IllegalArgumentException) {
				detailMessage = "Illegal parameter types";
			} else if (e instanceof InvocationTargetException) {
				detailMessage = e.getMessage();
			}
			
			Bukkit.getLogger().severe("Cannot execute command " + data.getName() + " in method " + method.getName() + " of type " + method.getDeclaringClass().getName() + ": " + e + ": " + detailMessage);
			e.printStackTrace();
		}
	}
	
	private Argument<?>[] parseArguments(TransformerMap transformers, String[] args) {
		List<Argument<?>> argsList = new ArrayList<Argument<?>>();
		//Argument<?>[] argsArray = new Argument<?>[parameterArgTypes.length];
		
		for (int i = 0; i < args.length && i < parameterArgTypes.length; i++) {
			String arg = args[i];
			Transformer<?> transformer = null;
			
			if (i < parameterArgTypes.length) {
				//Arrays have to be the last parameter (as dynamic parameters do too)
				if (parameterArgTypes[i].isArray() && i == parameterArgTypes.length - 1) {
					Class<?> componentType = parameterArgTypes[i].getComponentType();
					transformer = transformers.get(componentType);
					
					if (transformer == null) {
						argsList.add(null);
						break;
					}
					
					Object[] array = (Object[])Array.newInstance(componentType, args.length - i);
					
					for (int j = i; j < args.length; j++) {
						try {
							Object argument = transformer.transform(args[j]);
							array[j - i] = argument;
						} catch (TransformException e) {
							array[j - i] = null;
						}
					}
					
					argsList.add(new Argument<Object>(array));
				} else {
					transformer = transformers.get(parameterArgTypes[i]);
					
					if (transformer == null) {
						//There is no transformer for this parameter typ
						argsList.add(null);
						continue;
					}
					
					try {
						Argument<?> argument = createArgument(transformer, arg);
						
						argsList.add(argument);
					} catch (TransformException e) {
						//Transform failed, argument null
						argsList.add(null);
					}
				}
			}
		}
		
		return argsList.toArray(new Argument<?>[argsList.size()]);
	}
	
	private <V> Argument<V> createArgument(Transformer<V> t, String arg) throws TransformException {
		V transformed = t.transform(arg);
		Argument<V> argument = new Argument<V>(transformed);
		
		return argument;
	}

	public CommandData getCommandData() {
		return data;
	}
	
	Method getMethod() {
		return method;
	}

}
