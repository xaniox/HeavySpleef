package de.matzefratze123.heavyspleef.signs.signobjects;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import de.matzefratze123.heavyspleef.command.HSCommand;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameCuboid;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.flag.FlagType;
import de.matzefratze123.heavyspleef.signs.SpleefSign;
import de.matzefratze123.heavyspleef.signs.SpleefSignExecutor;
import de.matzefratze123.heavyspleef.util.Permissions;

public class SpleefSignSpectate implements SpleefSign {

	@Override
	public void onClick(Player player, Sign sign) {
		String[] lines = SpleefSignExecutor.stripSign(sign);
		
		if (!GameManager.hasGame(lines[2])) {
			player.sendMessage(Game._("arenaDoesntExists"));
			return;
		}
		
		Game game = GameManager.getGame(lines[2]);
		
		if (game.getFlag(FlagType.SPECTATE) == null) {
			player.sendMessage(HSCommand._("noSpectatePoint"));
			return;
		}
		
		game.spectate(player);
	}

	@Override
	public String getId() {
		return "sign.spectate";
	}

	@Override
	public String[] getLines() {
		String[] lines = new String[3];
		
		lines[0] = "[Spectate]";
		
		return lines;
	}

	@Override
	public Permissions getPermission() {
		return Permissions.SIGN_SPECTATE;
	}

	@Override
	public void onPlace(SignChangeEvent e) {
		if (!GameManager.hasGame(e.getLine(2).toLowerCase())) {
			e.getPlayer().sendMessage(GameCuboid._("arenaDoesntExists"));
			e.getBlock().breakNaturally();
			return;
		}
		
		e.getPlayer().sendMessage(HSCommand._("spleefSignCreated"));
		
		e.setLine(1, ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "Spectate" + ChatColor.DARK_GRAY + "]");
	}

}
