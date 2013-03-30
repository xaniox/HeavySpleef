package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.ScoreBoard;
import me.matzefratze123.heavyspleef.listener.PlayerListener;
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
		HeavySpleef.instance.reloadConfig();//First reload our config
		
		int antiCampTid = HeavySpleef.instance.antiCampTid;//Get the task id's
		int saverTid = HeavySpleef.instance.saverTid;
		
		if (antiCampTid >= 0)
			Bukkit.getScheduler().cancelTask(antiCampTid);//And cancel the tasks
		if (saverTid >= 0)
			Bukkit.getScheduler().cancelTask(saverTid);
		
		HeavySpleef.instance.startAntiCampingTask();//Restart the tasks
		HeavySpleef.instance.startSaveTask();
		
		PlayerListener.cantBreak = HeavySpleef.instance.getConfig().getIntegerList("blocks.cantBreak");//And refresh the cant-break list
		PlayerListener.loseOnTouchWaterOrLava = HeavySpleef.instance.getConfig().getBoolean("blocks.loseOnTouchWaterOrLava", true);
		
		ScoreBoard.refreshData();
		
		sender.sendMessage(_("pluginReloaded", HeavySpleef.instance.getDescription().getVersion(), String.valueOf(System.currentTimeMillis() - millis)));
	}

}
