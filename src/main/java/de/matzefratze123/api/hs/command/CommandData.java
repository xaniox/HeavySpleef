/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.api.hs.command;

/**
 * Provides a simple storing class for command data
 */
public class CommandData {

	public static final int	NO_MIN_ARGS	= -1;

	private String			name;
	private int				minArgs;
	private boolean			onlyIngame;

	private String			usage;
	private String			description;
	private String			usageStyle;

	private String[]		permissions;

	private String[]		aliases;

	public CommandData(String name, int minArgs, boolean onlyIngame, String usage, String description, String usageStyle, String[] permissions, String[] aliases) {
		this(name, minArgs, onlyIngame, usage, description, usageStyle, permissions);

		this.aliases = aliases;
	}

	public CommandData(String name, int minArgs, boolean onlyIngame, String usage, String description, String usageStyle, String[] permissions) {
		this(name, minArgs, onlyIngame, usage, description, usageStyle);

		this.permissions = permissions;
	}

	public CommandData(String name, int minArgs, boolean onlyIngame, String usage, String description, String usageStyle) {
		this(name, minArgs, onlyIngame, null);

		this.usage = usage;
		this.description = description;
		this.usageStyle = usageStyle;
	}

	public CommandData(String name, int minArgs, boolean onlyIngame, String[] permissions) {
		this(name);

		this.minArgs = minArgs;
		this.onlyIngame = onlyIngame;
		this.permissions = permissions;
	}

	public CommandData(String name, int minArgs, String usage, String description, String usageStyle, String[] permissions) {
		this(name, minArgs, false, permissions);

		this.usage = usage;
		this.description = description;
		this.usageStyle = usageStyle;
	}

	public CommandData(String name, boolean onlyIngame, String usage, String description, String usageStyle, String[] permissions) {
		this(name, NO_MIN_ARGS, onlyIngame, usage, description, usageStyle, permissions);
	}

	public CommandData(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinArgs() {
		return minArgs;
	}

	public void setMinArgs(int minArgs) {
		this.minArgs = minArgs;
	}

	public boolean onlyIngame() {
		return onlyIngame;
	}

	public void setOnlyIngame(boolean onlyIngame) {
		this.onlyIngame = onlyIngame;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUsageStyle() {
		return usageStyle;
	}

	public void setUsageStyle(String usageStyle) {
		this.usageStyle = usageStyle;
	}

	public String[] getPermissions() {
		return permissions;
	}

	public void setPermissions(String[] permissions) {
		this.permissions = permissions;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

}
