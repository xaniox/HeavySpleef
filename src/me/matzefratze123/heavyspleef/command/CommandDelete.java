package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;

public class CommandDelete extends HSCommand {

	public CommandDelete() {
		setMaxArgs(1);
		setMinArgs(1);
		setPermission(Permissions.DELETE_GAME.getPerm());
		setUsage("/spleef delete <Name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		if (game.isIngame() || game.isCounting() || game.isPreLobby()) {
			sender.sendMessage(_("cantDeleteGameWhileIngame"));
			return;
		}
		
		GameManager.removeAllPlayersFromGameQueue(args[0].toLowerCase());
		GameManager.deleteGame(args[0].toLowerCase());
		sender.sendMessage(_("gameDeleted"));
	}

}
