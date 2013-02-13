package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetLobby extends HSCommand {

	public CommandSetLobby() {
		setMaxArgs(1);
		setMinArgs(1);
		setPermission(Permissions.SET_PREGAMEPOINT.getPerm());
		setOnlyIngame(true);
		setUsage("/spleef setlobby <Name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0].toLowerCase())) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0].toLowerCase());
		game.setPreGamePoint(player.getLocation());
		player.sendMessage(_("lobbyPointSet", game.getName()));
	}

}
