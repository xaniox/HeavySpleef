package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.PlayerEnterQueueEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerLeaveQueueEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.Inject;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "queuelobby")
@BukkitListener
public class FlagQueueLobby extends LocationFlag {

	@Inject
	private Game game;
	private Map<SpleefPlayer, Location> previousLocations;
	
	public FlagQueueLobby() {
		this.previousLocations = Maps.newHashMap();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Teleports queued players into a lobby where they cannot teleport until they left the queue");
	}
	
	@Subscribe
	public void onQueueEnter(PlayerEnterQueueEvent event) {
		Location teleportPoint = getValue();
		
		SpleefPlayer player = event.getPlayer();
		Player bukkitPlayer = player.getBukkitPlayer();
		
		Location now = bukkitPlayer.getLocation();
		previousLocations.put(player, now);
		bukkitPlayer.teleport(teleportPoint);
	}
	
	@Subscribe
	public void onQueueLeave(PlayerLeaveQueueEvent event) {
		SpleefPlayer player = event.getPlayer();
		Location previous = previousLocations.get(player);
		
		if (previous != null) {
			player.getBukkitPlayer().teleport(previous);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		
		if (!game.isQueued(player)) {
			return;
		}
		
		event.setCancelled(true);
		player.sendMessage(getI18N().getString(Messages.Player.CANNOT_TELEPORT_IN_QUEUE));
	}

}
