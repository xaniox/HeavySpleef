package me.matzefratze123.heavyspleef.listener;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameManager;
import me.matzefratze123.heavyspleef.utility.SimpleBlockData;
import me.matzefratze123.heavyspleef.utility.Util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class ReadyListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		Block block = e.getClickedBlock();
		
		if (player == null)
			return;
		if (block == null)
			return;
		if (!GameManager.isInAnyGame(player))
			return;
		
		Game game = GameManager.fromPlayer(player);
		if (!game.isPreLobby())
			return;
		
		SimpleBlockData readyBlock = Util.fromString(HeavySpleef.getSystemConfig().getString("general.ready-block"), false);
		if (readyBlock == null)
			return;
		
		Material mat = readyBlock.getMaterial();
		byte data = readyBlock.getData();
		
		if (mat != block.getType() && data != block.getData())
			return;
		
		game.addVote(player);
		player.sendMessage(Game._("taggedAsReady"));
	}
	
}
