package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.GameState;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDisable extends HSCommand {

	public CommandDisable() {
		setMaxArgs(1);
		setMinArgs(1);
		setPermission(Permissions.DISABLE.getPerm());
		setUsage("/spleef disable <Name>");
		setOnlyIngame(true);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			player.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		
		if (game.isDisabled()) {
			player.sendMessage(_("gameIsAlreadyDisabled"));
			return;
		}
		game.broadcast(_("gameDisabled", game.getName(), player.getName()));
		if (game.isCounting() || game.isIngame() || game.isPreLobby())
			game.stop(true);
		game.setGameState(GameState.DISABLED);
		player.sendMessage(_("gameDisabledToPlayer", game.getName()));
	}

}
