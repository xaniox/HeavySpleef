package de.matzefratze123.heavyspleef.command.handler;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class Help {

	private String			usage;
	private List<String>	help;

	public Help(HSCommand command) {
		this.help = new ArrayList<String>();

		command.getHelp(this);
	}

	public String getUsage() {
		return ChatColor.RED + "Usage: " + usage;
	}

	public String getRawUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public void addHelp(String line) {
		help.add(line);
	}

	public List<String> getHelp() {
		return help;
	}

}
