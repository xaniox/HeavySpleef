package de.matzefratze123.heavyspleef.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.command.UserType.Type;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.SignWall;
import de.matzefratze123.heavyspleef.selection.Selection;
import de.matzefratze123.heavyspleef.util.Permissions;

@UserType(Type.ADMIN)
public class CommandAddWall extends HSCommand {
	
	public CommandAddWall() {
		setMinArgs(1);
		setMaxArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_WALL);
		setUsage("/spleef addwall <name>");
		setHelp("Adds a self updating wall to a game");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		
		Selection s = HeavySpleef.getInstance().getSelectionManager().getSelection(p);
		if (!s.has()) {
			p.sendMessage(_("needSelection"));
			return;
		}
		if (s.isTroughWorlds()) {
			p.sendMessage(_("selectionCantTroughWorlds"));
			return;
		}
		if (!SignWall.oneCoordSame(s.getFirst(), s.getSecond())) {
			p.sendMessage(_("didntSelectWall"));
			return;
		}
		if (SignWall.getDifference(s.getFirst(), s.getSecond()) < 2) {
			p.sendMessage(_("lengthMustBeOver1"));
			return;
		}
		if (s.getFirst().getBlockY() != s.getSecond().getBlockY()) {
			p.sendMessage(_("yMustBeSame"));
			return;
		}
		if (!SignWall.isAllSign(s.getFirst(), s.getSecond())) {
			p.sendMessage(_("notASign"));
			return;
		}
		
		game.addWall(s.getFirst(), s.getSecond());
		p.sendMessage(_("signWallAdded"));
	}

}
