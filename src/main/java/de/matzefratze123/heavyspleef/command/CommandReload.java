/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
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
package de.matzefratze123.heavyspleef.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.command.handler.Help;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.config.FileConfig;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandReload extends HSCommand {

	public CommandReload() {
		setPermission(Permissions.RELOAD);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		long millis = System.currentTimeMillis();
		new FileConfig(plugin);//Create a new config if the file was deleted
		plugin.reloadConfig();//First reload our config
		
		HeavySpleef.PREFIX = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("general.spleef-prefix", HeavySpleef.PREFIX));
		HeavySpleef.getInstance().getAntiCampingTask().restart();
		
		I18N.loadLanguageFiles();//Reload languages files
		plugin.getSelectionManager().setup();//Reload selection
		HeavySpleef.getInstance().initStatisticDatabase();
		
		sender.sendMessage(_("pluginReloaded", HeavySpleef.getInstance().getDescription().getVersion(), String.valueOf(System.currentTimeMillis() - millis)));
		//And we're done!
	}

	@Override
	public Help getHelp(Help help) {
		help.setUsage("/spleef reload");
		help.addHelp("Reloads the entire spleef plugin");
		
		return help;
	}

}
