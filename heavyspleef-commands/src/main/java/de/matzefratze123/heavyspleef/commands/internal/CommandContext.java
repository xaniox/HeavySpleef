package de.matzefratze123.heavyspleef.commands.internal;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandContext {
	
	private CommandManagerService manager;
	private String[] args;
	private CommandContainer command;
	private CommandSender sender;
	
	public CommandContext(CommandManagerService manager, String[] args, CommandContainer command, CommandSender sender) {
		this.manager = manager;
		this.args = args;
		this.command = command;
		this.sender = sender;
	}
	
	public String getString(int index) throws CommandException {
		checkBounds(index);
		
		return args[index];
	}
	
	public Integer getInt(int index) throws CommandException {
		checkBounds(index);
		
		return manager.getTransformer(Integer.class).transform(args[index]);
	}
	
	public Double getDouble(int index) throws CommandException {
		checkBounds(index);
		
		return manager.getTransformer(Double.class).transform(args[index]);
	}
	
	public Boolean getBoolean(int index) throws CommandException {
		checkBounds(index);
		
		return manager.getTransformer(Boolean.class).transform(args[index]);
	}
	
	public Player getPlayer(int index) throws CommandException {
		checkBounds(index);
		
		return manager.getTransformer(Player.class).transform(args[index]);
	}
	
	public int argsLength() {
		return args.length;
	}
	
	private void checkBounds(int index) throws CommandException {
		if (index < 0 || index >= args.length) {
			throw new CommandException("args index out of bounds (0 <= index < " + args.length + ")");
		}
	}
	
	public <R> R getArgument(int index, Class<R> clazz) throws CommandException {
		checkBounds(index);
		
		Transformer<R> transformer = manager.getTransformer(clazz);
		return transformer.transform(args[index]);
	}
	
	public CommandContainer getCommand() {
		return command;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CommandSender> T getSender() {
		return (T) sender;
	}
	
}