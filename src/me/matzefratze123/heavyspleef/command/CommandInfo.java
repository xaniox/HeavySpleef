package me.matzefratze123.heavyspleef.command;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.flag.Flag;
import me.matzefratze123.heavyspleef.utility.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInfo extends HSCommand {

	public CommandInfo() {
		setMaxArgs(1);
		setMinArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.INFO);
		setUsage("/spleef info <name>");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		
		player.sendMessage(ChatColor.YELLOW + "Name: " + game.getName() + ChatColor.GRAY + ", type: " + game.getType().name());
		if (game.getFlags().size() > 0)
			player.sendMessage(ChatColor.BLUE + "Flags: " + parseFlags(game));
		
	}
	
	private Set<String> parseFlags(Game game) {
		Map<Flag<?>, Object> flags = game.getFlags();
		Set<String> set = new HashSet<String>();
		
		for (Flag<?> flag : flags.keySet()) {
			String flagValue = flags.get(flag).toString();
			if (flags.get(flag) instanceof Location)
				flagValue = "LOCATION";
			set.add(flag.getName() + ": " + flagValue);
		}
		
		return set;
	}

}
