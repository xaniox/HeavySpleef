package de.matzefratze123.heavyspleef.core;

import org.bukkit.OfflinePlayer;

import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public interface KillDetector {
	
	public OfflinePlayer detectKiller(Game game, SpleefPlayer deadPlayer);

}
