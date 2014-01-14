/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
