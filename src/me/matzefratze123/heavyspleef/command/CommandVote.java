package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandVote extends HSCommand {

	public CommandVote() {
		setPermission(Permissions.VOTE);
		setOnlyIngame(true);
		setUsage("/spleef vote");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!HeavySpleef.instance.getConfig().getBoolean("general.autostart-vote-enabled", true)) {
			player.sendMessage(_("votesDisabled"));
			return;
		}
		
		if (!GameManager.isInAnyGame(player)) {
			player.sendMessage(_("onlyLobby"));
			return;
		}
		
		Game game = GameManager.fromPlayer(player);
		if (!game.isPreLobby()) {
			player.sendMessage("onlyLobby");
			return;
		}
		
		game.addVote(player.getName());
		player.sendMessage(_("successfullyVoted"));
		
		if (game.canBegin())
			game.countdown();
	}

}
