package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetWin extends HSCommand {

	public CommandSetWin() {
		setMaxArgs(1);
		setMinArgs(1);
		setPermission(Permissions.SET_WINPOINT.getPerm());
		setOnlyIngame(true);
		setUsage("/spleef setwin <Name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage("arenaDoesntExists");
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		game.setWinPoint(player.getLocation());
		player.sendMessage(_("winPointSet", game.getName()));
	}

}
