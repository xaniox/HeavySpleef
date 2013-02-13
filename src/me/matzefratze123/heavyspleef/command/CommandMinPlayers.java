package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMinPlayers extends HSCommand {

	public CommandMinPlayers() {
		setMaxArgs(2);
		setMinArgs(2);
		setPermission(Permissions.SET_MIN_PLAYERS.getPerm());
		setUsage("/spleef setminplayers <Name> <Players>");
		setOnlyIngame(true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		try {
			int needed = Integer.parseInt(args[1]);
			if (needed <= 1) {
				player.sendMessage(_("minimumToLow"));
				return;
			}
			game.setNeededPlayers(needed);
			player.sendMessage(_("setMinimumPlayers", String.valueOf(needed), game.getName()));
		} catch (NumberFormatException e) {
			player.sendMessage(_("notANumber", args[1]));
		}
	}

}
