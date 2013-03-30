package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.flag.Flag;
import me.matzefratze123.heavyspleef.core.flag.FlagType;
import me.matzefratze123.heavyspleef.core.flag.LocationFlag;
import me.matzefratze123.heavyspleef.utility.ArrayHelper;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandFlag extends HSCommand {

	public CommandFlag() {
		setMinArgs(2);
		setOnlyIngame(true);
		setUsage("/spleef flag <name> <flag> [state]\n" +
				__(ChatColor.RED + "Available flags: " + ArrayHelper.enumAsSet(FlagType.flagList, true)));
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		
		boolean found = false;
		Flag<?> flag = null;
		
		for (Flag<?> f : FlagType.flagList) {
			if (f.getName().equalsIgnoreCase(args[1])) {
				found = true;
				flag = f;
				break;
			}
		}
		
		if (!found || flag == null) {
			player.sendMessage(_("invalidFlag"));
			player.sendMessage(__(ChatColor.RED + "Available flags: " + ArrayHelper.enumAsSet(FlagType.flagList, true)));
			return;
		}
		
		if (args.length > 2 && args[2].equalsIgnoreCase("clear")) {
			game.setFlag(flag, null);
			player.sendMessage(_("flagCleared", flag.getName()));
			return;
		}
		
		if (flag instanceof LocationFlag) {
			setFlag(game, flag, player, null);
			return;
		}
		
		if (args.length > 2) {
			if (args[2].equalsIgnoreCase("clear")) {
				game.setFlag(flag, null);
				player.sendMessage(_("flagCleared", flag.getName()));
				return;
			}
			
			try {
				Object value = flag.parse(player, args[2]);
				
				if (value == null) {
					player.sendMessage(_("invalidFlagFormat"));
					player.sendMessage(flag.getHelp());
					return;
				}
				
				setFlag(game, flag, player, args[2]);
			} catch (Exception e) {
				player.sendMessage(_("invalidFlagFormat"));
				player.sendMessage(flag.getHelp());
			}
		}
	}
	
	public <V> void setFlag(Game game, Flag<V> flag, Player player, String context) {
		game.setFlag(flag, flag.parse(player, context));
		player.sendMessage(_("flagSet", flag.getName()));
	} 
	
}
