package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.Team;
import me.matzefratze123.heavyspleef.utility.MaterialHelper;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddTeam extends HSCommand {
	
	public CommandAddTeam() {
		setMinArgs(2);
		setUsage("/spleef addteam <arena> <red|blue|green|yellow|gray>");
		setOnlyIngame(true);
		setPermission(Permissions.ADD_TEAM);
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
		
		game.addTeam(color);
		player.sendMessage(_("teamAdded", color + MaterialHelper.getName(color.name())));
	}

}
