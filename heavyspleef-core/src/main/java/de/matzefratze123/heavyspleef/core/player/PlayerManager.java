package de.matzefratze123.heavyspleef.core.player;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Sets;

public class PlayerManager implements Listener {
	
	private Set<SpleefPlayer> onlineSpleefPlayers;
	
	public PlayerManager(JavaPlugin plugin) {
		onlineSpleefPlayers = Sets.newLinkedHashSet();
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public SpleefPlayer getSpleefPlayer(Player bukkitPlayer) {
		return getUniquePlayer(player -> player.getBukkitPlayer() == bukkitPlayer);
	}
	
	public SpleefPlayer getSpleefPlayer(String name) {
		return getUniquePlayer(player -> player.getName().equalsIgnoreCase(name));
	}
	
	public SpleefPlayer getSpleefPlayer(UUID uuid) {
		return getUniquePlayer(player -> player.getUniqueId().equals(uuid));
	}
	
	private SpleefPlayer getUniquePlayer(Predicate<SpleefPlayer> predicate) {
		Optional<SpleefPlayer> optional = onlineSpleefPlayers.stream().filter(predicate).findFirst();
		return optional.isPresent() ? optional.get() : null;
	}
	
	public Set<SpleefPlayer> getSpleefPlayers() {
		return Collections.unmodifiableSet(onlineSpleefPlayers);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		
		SpleefPlayer spleefPlayer = new SpleefPlayer(player);
		onlineSpleefPlayers.add(spleefPlayer);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		handlePlayerLeave(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		handlePlayerLeave(e.getPlayer());
	}
	
	private void handlePlayerLeave(Player player) {
		SpleefPlayer spleefPlayer = getSpleefPlayer(player);
		
		spleefPlayer.setOnline(false);
		onlineSpleefPlayers.remove(spleefPlayer);
	}
	
}
