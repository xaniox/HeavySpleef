/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.core.player;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;

public class SpleefPlayer implements CommandSender {
	
	public static final String ALLOW_NEXT_TELEPORT_KEY = "spleef_teleport";
	
	/* Only keep a weak reference to avoid memory leaks.
	 * Reference should be actually hold by Bukkit itself */
	private WeakReference<Player> bukkitPlayerRef;
	private String name;
	private boolean online;
	private final HeavySpleef heavySpleef;
	
	private Map<Object, PlayerStateHolder> playerStates;
	
	public SpleefPlayer(Player bukkitPlayer, HeavySpleef heavySpleef) {
		this.bukkitPlayerRef = new WeakReference<Player>(bukkitPlayer);
		this.online = bukkitPlayer.isOnline();
		this.playerStates = Maps.newHashMap();
		this.name = bukkitPlayer.getName();
		this.heavySpleef = heavySpleef;
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
	
	public String getDisplayName() {
		return (isVip() ? heavySpleef.getVipPrefix() : "") + getName();
	}
	
	@Override
	public String getName() {
		return bukkitPlayerRef.get() != null ? getBukkitPlayer().getName() : name;
	}
	
	public UUID getUniqueId() {
		validateOnline();
		return getBukkitPlayer().getUniqueId();
	}
	
	@Override
	public boolean hasPermission(String permission) {
		validateOnline();
		return getBukkitPlayer().hasPermission(permission);
	}
	
	public boolean isVip() {
		return hasPermission(Permissions.PERMISSION_VIP);
	}
	
	@Override
	public void sendMessage(String message) {
		validateOnline();
		
		if (message.isEmpty()) {
			return;
		}
		
		sendUnprefixedMessage(heavySpleef.getSpleefPrefix() + message);
	}
	
	public void sendUnprefixedMessage(String message) {
		validateOnline();
		getBukkitPlayer().sendMessage(message);
	}
	
	public void teleport(Location location) {
		validateOnline();
		Player bukkitPlayer = getBukkitPlayer();
		
		//Setting a metadata value to indicate that the next teleport is allowed
		bukkitPlayer.setMetadata(ALLOW_NEXT_TELEPORT_KEY, new FixedMetadataValue(heavySpleef.getPlugin(), true));
		getBukkitPlayer().teleport(location);
		
		//Remove previously set metadata key
		bukkitPlayer.removeMetadata(ALLOW_NEXT_TELEPORT_KEY, heavySpleef.getPlugin());
	}
	
	public void savePlayerState(Object key, GameMode gameMode) {
		validateOnline();
		
		PlayerStateHolder holder = PlayerStateHolder.create(getBukkitPlayer(), gameMode);
		playerStates.put(key, holder);
	}
	
	public void savePlayerState(Object key, PlayerStateHolder holder) {
		validateOnline();
		
		playerStates.put(key, holder);
	}
	
	public PlayerStateHolder getPlayerState(Object key) {
		return playerStates.get(key);
	}
	
	public PlayerStateHolder removePlayerState(Object key) {
		return playerStates.remove(key);
	}
	
	private void validateOnline() {
		Validate.isTrue(isOnline(), "Player is not online");
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin) {
		validateOnline();
		return getBukkitPlayer().addAttachment(plugin);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		validateOnline();
		return getBukkitPlayer().addAttachment(plugin, ticks);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		validateOnline();
		return getBukkitPlayer().addAttachment(plugin, name, value);
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
		validateOnline();
		return getBukkitPlayer().addAttachment(plugin, name, value, ticks);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		validateOnline();
		return getBukkitPlayer().getEffectivePermissions();
	}

	@Override
	public boolean hasPermission(Permission perm) {
		validateOnline();
		return getBukkitPlayer().hasPermission(perm);
	}

	@Override
	public boolean isPermissionSet(String name) {
		validateOnline();
		return getBukkitPlayer().isPermissionSet(name);
	}

	@Override
	public boolean isPermissionSet(Permission perm) {
		validateOnline();
		return getBukkitPlayer().isPermissionSet(perm);
	}

	@Override
	public void recalculatePermissions() {
		validateOnline();
		getBukkitPlayer().recalculatePermissions();
	}

	@Override
	public void removeAttachment(PermissionAttachment attachment) {
		validateOnline();
		getBukkitPlayer().removeAttachment(attachment);
	}

	@Override
	public boolean isOp() {
		validateOnline();
		return getBukkitPlayer().isOp();
	}

	@Override
	public void setOp(boolean op) {
		validateOnline();
		getBukkitPlayer().setOp(op);
	}

	@Override
	public Server getServer() {
		validateOnline();
		return getBukkitPlayer().getServer();
	}

	@Override
	public void sendMessage(String[] messages) {
		validateOnline();
		getBukkitPlayer().sendMessage(messages);
	}
	
}
