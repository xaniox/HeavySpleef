package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStartOnReachMinimum extends HSCommand {

	public CommandStartOnReachMinimum() {
		setMaxArgs(2);
		setMinArgs(1);
		setPermission(Permissions.STARTONMINPLAYERS.getPerm());
		setUsage("/spleef startonminplayers <Name> [true|false]");
		setOnlyIngame(true);
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			p.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		if (args.length == 1) {
			p.sendMessage(_("startOnMinPlayersInfo", String.valueOf(game.isStartingOnMinPlayers())));
			return;
		} else if (args.length == 2) {
			boolean start = Boolean.parseBoolean(args[1]);
			game.setStartOnMinPlayers(start);
			
			String message = start ? _("startOnMinPlayersTrue") : _("startOnMinPlayersFalse");
			p.sendMessage(message);
		}
	}

}
