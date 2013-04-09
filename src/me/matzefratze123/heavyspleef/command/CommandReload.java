/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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
package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.FileConfig;
import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.ScoreBoard;
import me.matzefratze123.heavyspleef.listener.PlayerListener;
import me.matzefratze123.heavyspleef.utility.LanguageHandler;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandReload extends HSCommand {

	public CommandReload() {
		setPermission(Permissions.RELOAD);
		setUsage("/spleef reload");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		long millis = System.currentTimeMillis();
		HeavySpleef.config = new FileConfig(plugin);//Create a new config if the file was deleted
		plugin.reloadConfig();//First reload our config
		
		int antiCampTid = HeavySpleef.instance.antiCampTid;//Get the task id's
		int saverTid = HeavySpleef.instance.saverTid;
		
		if (antiCampTid >= 0)
			Bukkit.getScheduler().cancelTask(antiCampTid);//And cancel the tasks
		if (saverTid >= 0)
			Bukkit.getScheduler().cancelTask(saverTid);
		
		plugin.startAntiCampingTask();//Restart the tasks
		plugin.startSaveTask();
		
		PlayerListener.cantBreak = HeavySpleef.instance.getConfig().getIntegerList("blocks.cantBreak");//And refresh the cant-break list
		PlayerListener.loseOnTouchWaterOrLava = HeavySpleef.instance.getConfig().getBoolean("blocks.loseOnTouchWaterOrLava", true);
		
		ScoreBoard.refreshData();//Refresh scoreboard data
		LanguageHandler.loadLanguageFiles();//Reload languages files
		
		plugin.getSelectionManager().setup();//Reload selection
		
		sender.sendMessage(_("pluginReloaded", HeavySpleef.instance.getDescription().getVersion(), String.valueOf(System.currentTimeMillis() - millis)));
		//And we are done!
	}

}
