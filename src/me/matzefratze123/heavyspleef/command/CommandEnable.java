package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.GameState;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandEnable extends HSCommand {

	public CommandEnable() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ENABLE.getPerm());
		setUsage("/spleef enable <Name>");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())){
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		if (!game.isDisabled()) {
			player.sendMessage(_("gameIsAlreadyEnabled"));
			return;
		}
		game.broadcast(_("gameEnabled", game.getName(), player.getName()));
		game.setGameState(GameState.NOT_INGAME);
		player.sendMessage(_("gameEnabledToPlayer", game.getName()));
	}

}
