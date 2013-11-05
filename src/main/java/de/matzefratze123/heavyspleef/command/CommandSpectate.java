package de.matzefratze123.heavyspleef.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.PLAYER)
public class CommandSpectate extends HSCommand {

	public CommandSpectate() {
		setMinArgs(1);
		setPermission(Permissions.SPECTATE);
		setOnlyIngame(true);
		setUsage("/spleef spectate <Game>");
		setHelp("Spectates a game");
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		SpleefPlayer player = HeavySpleef.getInstance().getSpleefPlayer((Player)sender);
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(args[0]);
		
		if (game.getFlag(FlagType.SPECTATE) == null) {
			sender.sendMessage(_("noSpectatePoint"));
			return;
		}
		
		game.spectate(player);
	}

}
