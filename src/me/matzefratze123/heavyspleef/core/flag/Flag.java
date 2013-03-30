package me.matzefratze123.heavyspleef.core.flag;

import org.bukkit.entity.Player;

public abstract class Flag<T> {

	private String name;
	
	public Flag(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public abstract T parse(Player player, String input);
	
	public abstract String getHelp();
	
	@Override
	public String toString() {
		return name;
	}
}
