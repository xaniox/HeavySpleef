package me.matzefratze123.heavyspleef.command;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.core.SignWall;
import me.matzefratze123.heavyspleef.selection.Selection;
import me.matzefratze123.heavyspleef.util.Permissions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddWall extends HSCommand {
	
	public CommandAddWall() {
		setMinArgs(1);
		setMaxArgs(1);
		setOnlyIngame(true);
		setPermission(Permissions.ADD_WALL);
		setUsage("/spleef addwall <name>");
		setTabHelp(new String[]{"<arena>"});
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		Player p = (Player)sender;
		
		if (!GameManager.hasGame(args[0])) {
			sender.sendMessage(_("arenaDoesntExists"));
			return;
		}
		Game game = GameManager.getGame(args[0]);
		
		Selection s = HeavySpleef.instance.getSelectionManager().getSelection(p);
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
			p.sendMessage(_("notASign"));//TODO
			return;
		}
		
		game.addWall(s.getFirst(), s.getSecond());
		p.sendMessage(_("signWallAdded"));
	}

}
