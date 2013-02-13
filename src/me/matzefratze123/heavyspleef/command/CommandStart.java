package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStart extends HSCommand {

	public CommandStart() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.START_GAME.getPerm());
		setUsage("/spleef start <Name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		if (game.isDisabled()) {
			player.sendMessage(_("gameIsDisabled"));
			return;
		}
		if (game.isCounting() || game.isIngame()) {
			player.sendMessage(_("cantStartGameWhileRunning"));
			return;
		}
		if (game.getPlayers().length < game.getNeededPlayers() || game.getNeededPlayers() < 2) {
			player.sendMessage(_("notEnoughPlayers", String.valueOf(game.getNeededPlayers())));
			return;
		}
		game.setupFloors();
		game.start();
		player.sendMessage(_("gameStarted"));
	}

}
