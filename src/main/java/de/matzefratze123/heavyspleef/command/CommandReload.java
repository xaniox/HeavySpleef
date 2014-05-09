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
package de.matzefratze123.heavyspleef.command;

import static de.matzefratze123.heavyspleef.util.I18N._;

import org.bukkit.command.CommandSender;

import de.matzefratze123.api.hs.command.Command;
import de.matzefratze123.api.hs.command.CommandHelp;
import de.matzefratze123.api.hs.command.CommandListener;
import de.matzefratze123.api.hs.command.CommandPermissions;
import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.handler.UserType;
import de.matzefratze123.heavyspleef.command.handler.UserType.Type;
import de.matzefratze123.heavyspleef.core.task.TaskAdvancedAntiCamping;
import de.matzefratze123.heavyspleef.util.I18N;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandReload implements CommandListener {

	@Command(value = "reload")
	@CommandPermissions(value = { Permissions.RELOAD })
	@CommandHelp(usage = "/spleef reload", description = "Reloads the entire spleef plugin")
	public void execute(CommandSender sender) {
		long millis = System.currentTimeMillis();
		HeavySpleef.getSystemConfig().reload();

		HeavySpleef.PREFIX = HeavySpleef.getSystemConfig().getGeneralSection().getPrefix();
		HeavySpleef.getInstance().getAntiCampingTask().restart();
		
		boolean advancedAntiCampingEnabled = HeavySpleef.getSystemConfig().getAnticampingSection().isAdvancedAnticampingEnabled();
		
		TaskAdvancedAntiCamping advancedAntiCampingTask = HeavySpleef.getInstance().getAdvancedAntiCampingTask();
		
		if (advancedAntiCampingTask != null) {
			if (advancedAntiCampingEnabled) {
				advancedAntiCampingTask.restart();
			} else {
				advancedAntiCampingTask.stop();
			}
		} else if (advancedAntiCampingEnabled) {
				advancedAntiCampingTask = new TaskAdvancedAntiCamping();
				
				HeavySpleef.getInstance().setAdvancedAntiCampingTask(advancedAntiCampingTask);
		}

		I18N.loadLanguageFiles();// Reload languages files
		HeavySpleef.getInstance().getSelectionManager().setup();// Reload
																// selection
		HeavySpleef.getInstance().initStatisticDatabase();

		sender.sendMessage(_("pluginReloaded", HeavySpleef.getInstance().getDescription().getVersion(), String.valueOf(System.currentTimeMillis() - millis)));
		// And we're done!
	}

}
