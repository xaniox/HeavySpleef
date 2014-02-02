package de.matzefratze123.api.hs.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.matzefratze123.api.hs.command.transform.BlockDataTransformer;
import de.matzefratze123.api.hs.command.transform.BooleanTransformer;
import de.matzefratze123.api.hs.command.transform.DefaultTransformer;
import de.matzefratze123.api.hs.command.transform.DoubleTransformer;
import de.matzefratze123.api.hs.command.transform.IntegerTransformer;
import de.matzefratze123.api.hs.command.transform.PlayerTransformer;
import de.matzefratze123.api.hs.command.transform.Transformer;
import de.matzefratze123.api.hs.command.transform.BlockDataTransformer.BlockData;

/**
 * Provides an executor service for root-sub commands.
 * By 
 */
public class CommandExecutorService implements CommandExecutor {
	
	private TransformerMap transformers;
	
	private final String rootCommand;
	private final JavaPlugin plugin;
	
	private RootCommandExecutor rootCommandExecutor;
	private final Map<CommandListener, ExecuteableMethod[]> listeners;
	
	private String unknownCommandMessage = ChatColor.RED + "Command not found.";
	
	public CommandExecutorService(String rootCommand, JavaPlugin plugin) {
		this.rootCommand = rootCommand;
		this.plugin = plugin;
		this.listeners = new HashMap<CommandListener, ExecuteableMethod[]>();
		
		final PluginCommand cmd = plugin.getCommand(rootCommand);
		Validate.notNull(cmd, "Plugin " + plugin.getName() + " does not declare command " + rootCommand);
		
		cmd.setExecutor(this);
		
		initTransformers();
	}
	
	private void initTransformers() {
		transformers = new TransformerMap();
		transformers.put(BlockData.class, new BlockDataTransformer());
		transformers.put(Player.class, new PlayerTransformer());
		transformers.put(Double.class, new DoubleTransformer());
		transformers.put(Integer.class, new IntegerTransformer());
		transformers.put(Boolean.class, new BooleanTransformer());
		transformers.put(String.class, new DefaultTransformer());
	}
	
	public <V> Transformer<V> getTransformer(Class<V> clazz) {
		return transformers.get(clazz);
	}
	
	public <V> void registerTransformer(Class<V> clazz, Transformer<V> transformer) {
		transformers.put(clazz, transformer);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!cmd.getName().equalsIgnoreCase(rootCommand)) {
			//Catch other commands which were registered by other plugins on this executor
			return true;
		}
		
		String[] argsCut = new String[args.length == 0 ? 0 : args.length - 1];
		for (int i = 1; i < args.length; i++) {
			argsCut[i - 1] = args[i];
		}
		
		if (args.length == 0) {
			if (rootCommandExecutor != null) {
				rootCommandExecutor.execute(sender);
			}
		} else {
			for (Entry<CommandListener, ExecuteableMethod[]> entry : listeners.entrySet()) {
				ExecuteableMethod[] methods = entry.getValue();
				
				for (ExecuteableMethod method : methods) {
					if (!method.getCommandData().getName().equalsIgnoreCase(args[0])) {
						continue;
					}
					
					method.execute(sender, argsCut, transformers);
					return true;
				}
			}
			
			//No command found, we should try to find commands by aliases
			for (ExecuteableMethod[] methods : listeners.values()) {
				for (ExecuteableMethod method : methods) {
					if (method.getCommandData().getAliases() == null) {
						continue;
					}
					
					for (String alias : method.getCommandData().getAliases()) {
						if (alias.equalsIgnoreCase(args[0])) {
							method.execute(sender, argsCut, transformers);
							return true;
						}
					}
				}
			}
			
			//No command found
			sender.sendMessage(unknownCommandMessage);
		}
		
		return true;
	}
	
	public void setUnknownCommandMessage(String unknownCommandMessage) {
		this.unknownCommandMessage = unknownCommandMessage;
	}
	
	public String getUnknownCommandMessage() {
		return unknownCommandMessage;
	}
	
	public void registerListener(CommandListener listener) {
		Validate.notNull(listener, "listener cannot be null");
		
		if (listeners.containsKey(listener)) {
			return;
		}
		
		List<ExecuteableMethod> methods = ExecuteableMethod.findListenerMethods(listener);
		Iterator<ExecuteableMethod> iterator = methods.iterator();
		
		while (iterator.hasNext()) {
			ExecuteableMethod method = iterator.next();
			
			if (hasCommand(method.getCommandData().getName())) {
				Bukkit.getLogger().warning("Warning: Plugin " + plugin.getName() + " v" + plugin.getDescription().getVersion() + " has tried to register an already registered command: " + method.getCommandData().getName());
				iterator.remove();
			}
		}
		
		listeners.put(listener, methods.toArray(new ExecuteableMethod[methods.size()]));
	}
	
	public void unregisterListener(CommandListener listener) {
		if (!listeners.containsKey(listener)) {
			return;
		}
		
		listeners.remove(listener);
	}
	
	private boolean hasCommand(String name) {
		for (ExecuteableMethod[] methods : listeners.values()) {
			for (ExecuteableMethod method : methods) {
				if (method.getCommandData().getName().equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Map<CommandListener, CommandData[]> getCommands() {
		Map<CommandListener, CommandData[]> map = new HashMap<CommandListener, CommandData[]>();
		
		for (Entry<CommandListener, ExecuteableMethod[]> entry : listeners.entrySet()) {
			CommandData[] data = new CommandData[entry.getValue().length];
			
			for (int i = 0; i < data.length; i++) {
				data[i] = entry.getValue()[i].getCommandData();
			}
			
			map.put(entry.getKey(), data);
		}
		
		return map;
	}
	
	public void setRootCommandExecutor(RootCommandExecutor executor) {
		this.rootCommandExecutor = executor;
	}
	
	public String getRootCommand() {
		return rootCommand;
	}
	
	public JavaPlugin getPlugin() {
		return plugin;
	}
	
	public static class TransformerMap {
		
		private HashMap<Class<?>, Transformer<?>> map;
		
		public TransformerMap() {
			map = new HashMap<Class<?>, Transformer<?>>();
		}
		
		@SuppressWarnings("unchecked")
		public <V> Transformer<V> put(Class<V> clazz, Transformer<V> transformer) {
			return (Transformer<V>) map.put(clazz, transformer);
		}
		
		@SuppressWarnings("unchecked")
		public <V> Transformer<V> get(Class<V> clazz) {			
			return (Transformer<V>)map.get(clazz);
		}
		
		public boolean contains(Class<?> clazz) {
			return map.containsKey(clazz);
		}
		
	}

}
