package me.matzefratze123.heavyspleef.command;

import java.util.Set;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.ArrayHelper;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandList extends HSCommand {

	public CommandList() {
		setMaxArgs(1);
		setMinArgs(0);
		setOnlyIngame(true);
		setPermission(Permissions.LIST);
		setUsage("/spleef list [name]");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (args.length == 0) {
			if (GameManager.isInAnyGame(player)) {
				Game game = GameManager.fromPlayer(player);
				printList(game, player);
			} else {
				Set<String> games = ArrayHelper.asSet(GameManager.getGamesAsString());
				
				player.sendMessage(ChatColor.GRAY + "All games: " + games.toString());
			}
		} else if (args.length > 0) {
			if (!GameManager.hasGame(args[0])) {
				sender.sendMessage(_("arenaDoesntExists"));
				return;
			}
			
			Game game = GameManager.getGame(args[0]);
			printList(game, player);
		}
	}
	
	private void printList(Game game, Player player) {
		Set<Player> active = ArrayHelper.asSet(game.getPlayers());
		Set<Player> out = ArrayHelper.asSet(game.getOutPlayers());
		
		player.sendMessage(ChatColor.AQUA + "Active: " + active.toString());
		player.sendMessage(ChatColor.RED + "Out: " + out.toString());
	}
	
}
