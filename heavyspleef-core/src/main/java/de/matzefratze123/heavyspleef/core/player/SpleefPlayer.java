package de.matzefratze123.heavyspleef.core.player;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.core.HeavySpleef;

public class SpleefPlayer {
	
	/* Only keep a weak reference to avoid memory leaks.
	 * Reference should be actually hold by Bukkit itself */
	private WeakReference<Player> bukkitPlayerRef;
	private boolean online;
	
	private Map<Object, PlayerStateHolder> playerStates;
	
	public SpleefPlayer(Player bukkitPlayer) {
		this.bukkitPlayerRef = new WeakReference<Player>(bukkitPlayer);
	}
	
	public Player getBukkitPlayer() {
		return bukkitPlayerRef.get();
	}
	
	public boolean isOnline() {
		return online && bukkitPlayerRef.get() != null;
	}
	
	protected void setOnline(boolean online) {
		this.online = online;
	}
	
	public String getName() {
		return getBukkitPlayer().getName();
	}
	
	public UUID getUniqueId() {
		return getBukkitPlayer().getUniqueId();
	}
	
	public void sendMessage(String message) {
		getBukkitPlayer().sendMessage(HeavySpleef.PREFIX + message);
	}
	
	public void teleport(Location location) {
		getBukkitPlayer().teleport(location);
	}
	
	public void sendLocalizedMessage(Object arg) {
		//TODO
	}
	
	public void savePlayerState(Object key) {
		Validate.isTrue(isOnline(), "Player must be online");
		
		PlayerStateHolder holder = PlayerStateHolder.create(getBukkitPlayer());
		playerStates.put(key, holder);
	}
	
	public PlayerStateHolder getPlayerState(Object key) {
		return playerStates.get(key);
	}
	
}
