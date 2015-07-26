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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.EnumWrappers.TitleAction;

import de.matzefratze123.heavyspleef.core.MinecraftVersion;
import de.matzefratze123.heavyspleef.core.event.GameCountdownChangeEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.hook.HookManager;
import de.matzefratze123.heavyspleef.core.hook.HookReference;
import de.matzefratze123.heavyspleef.core.hook.ProtocolLibHook;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.BaseFlag;

@Flag(name = "countdown-titles", requiresVersion = MinecraftVersion.V1_8, depend = HookReference.PROTOCOLLIB)
public class FlagCountdownTitles extends BaseFlag {

	private static final int FADE_IN = 5;
	private static final int FADE_OUT = 5;
	private static final int SHOW_LENGTH = 20;
	
	private ProtocolManager manager;
	
	@Override
	public void onFlagAdd(Game game) {
		HookManager hookManager = game.getHeavySpleef().getHookManager();
		ProtocolLibHook hook = (ProtocolLibHook) hookManager.getHook(HookReference.PROTOCOLLIB);
		this.manager = hook.getProtocolManager();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the Minecraft 1.8 title feature which shows a text in front of the players monitor");
	}
	
	@Subscribe
	public void onGameCountdownChange(GameCountdownChangeEvent event) {
		if (!event.isBroadcast()) {
			return;
		}
		
		for (SpleefPlayer player : event.getGame().getPlayers()) {
			try {
				sendTitle(player, String.valueOf(event.getCountdown().getRemaining()), null);
			} catch (InvocationTargetException e) {
				getHeavySpleef().getLogger().log(Level.SEVERE, "Failed to send countdown title to player", e);
			}
		}
	}
	
	private void sendTitle(SpleefPlayer player, String title, String subtitle) throws InvocationTargetException {
		Player bukkitPlayer = player.getBukkitPlayer();
		
		PacketContainer timesPacket = manager.createPacket(PacketType.Play.Server.TITLE);
		timesPacket.getTitleActions().write(0, TitleAction.TIMES);
		timesPacket.getIntegers().write(0, FADE_IN);
		timesPacket.getIntegers().write(1, SHOW_LENGTH);
		timesPacket.getIntegers().write(2, FADE_OUT);
		manager.sendServerPacket(bukkitPlayer, timesPacket);
		
		if (title != null) {
			PacketContainer titlePacket = manager.createPacket(PacketType.Play.Server.TITLE);
			titlePacket.getTitleActions().write(0, TitleAction.TITLE);
			titlePacket.getChatComponents().write(0, getChatComponent(title));
			manager.sendServerPacket(bukkitPlayer, titlePacket);
		}
		
		if (subtitle != null) {
			PacketContainer subtitlePacket = manager.createPacket(PacketType.Play.Server.TITLE);
			subtitlePacket.getTitleActions().write(0, TitleAction.SUBTITLE);
			subtitlePacket.getChatComponents().write(0, getChatComponent(subtitle));
			manager.sendServerPacket(bukkitPlayer, subtitlePacket);
		}
	}
	
	private WrappedChatComponent getChatComponent(String text) {
		return WrappedChatComponent.fromJson("{\"text\": \"" + text + "\"}");
	}

}
