package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetLose extends HSCommand {

	public CommandSetLose() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.SET_LOSEPOINT.getPerm());
		setUsage("/spleef setlose <Name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		game.setLosePoint(player.getLocation());
		player.sendMessage(_("losePointSet", game.getName()));
	}

}
