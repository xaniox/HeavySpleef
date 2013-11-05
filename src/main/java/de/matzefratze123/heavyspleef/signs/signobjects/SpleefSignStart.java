package de.matzefratze123.heavyspleef.signs.signobjects;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.command.CommandStart;
import de.matzefratze123.heavyspleef.command.HSCommand;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.signs.SpleefSignExecutor;
import de.matzefratze123.heavyspleef.util.LanguageHandler;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignStart implements SpleefSign {

	@Override
	public void onClick(SpleefPlayer player, Sign sign) {
		String[] lines = SpleefSignExecutor.stripSign(sign);
		
		if (!GameManager.hasGame(lines[2])) {
			player.sendMessage(LanguageHandler._("arenaDoesntExists"));
			return;
		}
		
		CommandStart.start(player, GameManager.getGame(sign.getLine(2)));
	}

	@Override
	public String getId() {
		return "sign.start";
	}

	@Override
	public String[] getLines() {
		String[] lines = new String[3];
		
		lines[0] = "[Start]";
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SIGN_START;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		if (!GameManager.hasGame(e.getLine(2).toLowerCase())) {
			e.getPlayer().sendMessage(LanguageHandler._("arenaDoesntExists"));
			e.getBlock().breakNaturally();
			return;
		}
		
		e.getPlayer().sendMessage(HSCommand._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + ChatColor.BOLD + "Start" + ChatColor.DARK_GRAY + "]");
	}

}
