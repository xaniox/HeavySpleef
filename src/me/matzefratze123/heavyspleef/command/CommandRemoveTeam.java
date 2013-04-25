package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Team;
import me.matzefratze123.heavyspleef.utility.Util;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRemoveTeam extends HSCommand {
	
	public CommandRemoveTeam() {
		setMinArgs(2);
		setUsage("/spleef removeteam <arena> <red|blue|green|yellow|gray>");
		setPermission(Permissions.REMOVE_TEAM);
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		ChatColor color = null;
		
		for (ChatColor colors : Team.allowedColors) {
			if (colors.name().equalsIgnoreCase(args[1]))
				color = colors;
		}
		
		if (color == null) {
			player.sendMessage(getUsage());
			return;
		}
		
		game.removeTeam(color);
		player.sendMessage(_("teamRemoved", color + Util.getName(color.name())));
	}

}
