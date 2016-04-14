/*
 * This file is part of addons.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.bungeemode;

import de.xaniox.bungeemode.BungeemodeConfig.SendBackCriteria;
import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.DefaultConfig;
import de.xaniox.heavyspleef.core.config.GeneralSection;
import de.xaniox.heavyspleef.core.event.*;
import de.xaniox.heavyspleef.core.event.PlayerQueueFlushEvent.FlushResult;
import de.xaniox.heavyspleef.core.event.Subscribe.Priority;
import de.xaniox.heavyspleef.core.game.*;
import de.xaniox.heavyspleef.core.game.Game.JoinResult;
import de.xaniox.heavyspleef.core.game.JoinRequester.JoinFutureCallback;
import de.xaniox.heavyspleef.core.game.JoinRequester.JoinValidationException;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.defaults.FlagSpectate;
import de.xaniox.heavyspleef.flag.defaults.FlagSpectate.SpectateLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class BungeemodeListener implements Listener, SpleefListener {

	private BungeemodeAddon addon;
	private boolean restarting;
	
	public BungeemodeListener(BungeemodeAddon addon) {
		this.addon = addon;
	}
	
	public boolean isRestarting() {
		return restarting;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		BungeemodeConfig config = addon.getConfig();
		
		if (!config.isEnabled()) {
			return;
		}
		
		String gameName = config.getGame();
		GameManager manager = addon.getHeavySpleef().getGameManager();
		SpleefPlayer player = addon.getHeavySpleef().getSpleefPlayer(event.getPlayer());
		
		if (!manager.hasGame(gameName)) {
			addon.getLogger().log(Level.WARNING, "Cannot handle player join for '" + player.getName() + "': Game " + gameName + " does not exist!");
			return;
		}
		
		Game game = manager.getGame(gameName);
		if (restarting) {
			//Don't join the game when we're restarting
			return;
		}
		
		try {
			game.getJoinRequester().request(player, new JoinFutureCallback() {
				
				@Override
				public void onJoin(SpleefPlayer player, Game game, JoinResult result) {
					handleJoinResult(player, game, result);
				}
			});
		} catch (JoinValidationException e) {
			handleJoinResult(player, game, e.getResult());
		}
	}
	
	private void handleJoinResult(SpleefPlayer player, Game game, JoinResult result) {
		if (result == JoinResult.ALLOW) {
			return;
		}
		
		if (game.isFlagPresent(FlagSpectate.class)) {
			FlagSpectate spectate = game.getFlag(FlagSpectate.class);
			spectate.spectate(player, game);
			player.sendMessage(addon.getI18n().getVarString(Messages.Player.PLAYER_SPECTATE)
					.setVariable("game", game.getName())
					.toString());
		} else {
			String message = addon.getI18n().getString(BungeemodeMessages.KICK_MESSAGE_INGAME);
			player.getBukkitPlayer().kickPlayer(message);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPreJoin(AsyncPlayerPreLoginEvent event) {
		BungeemodeConfig config = addon.getConfig();
		
		if (!config.isEnabled()) {
			return;
		}
		
		if (!addon.getHeavySpleef().isGamesLoaded()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(addon.getI18n().getString(BungeemodeMessages.KICK_MESSAGE_NOT_YET_READY));
            return;
        }

        String gameName = config.getGame();
		GameManager manager = addon.getHeavySpleef().getGameManager();
		
		if (!manager.hasGame(gameName)) {
			addon.getLogger().log(Level.WARNING, "Cannot handle player pre login for '" + event.getName() + "': Game " + gameName + " does not exist!");
			return;
		}
		
		Game game = manager.getGame(gameName);
		
		if (!game.getGameState().isGameEnabled()) {
			event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			event.setKickMessage(addon.getI18n().getString(BungeemodeMessages.KICK_MESSAGE_DISABLED));
			return;
		}
		
		boolean joinOnCountdown = game.getPropertyValue(GameProperty.JOIN_ON_COUNTDOWN);
		if (game.getGameState() == GameState.INGAME || (game.getGameState() == GameState.STARTING && !joinOnCountdown)) {
			if (config.getSpectateWhenIngame()) {
				if (game.isFlagPresent(FlagSpectate.class)) {
					return;
				} else {
					addon.getLogger().log(Level.WARNING,
							"Cannot transfer the player " + event.getName() + " into spectate mode: No spectate flag/point has been set");
				}
			}
			
			event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
			event.setKickMessage(addon.getI18n().getString(BungeemodeMessages.KICK_MESSAGE_INGAME));
			return;
		}
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onPlayerLeftGame(PlayerLeftGameEvent event) {
		BungeemodeConfig config = addon.getConfig();
		if (!config.isEnabled() || (config.getSendBackOn() != SendBackCriteria.LOSE && event.getGame().getGameState().isGameActive())) {
			return;
		}
		
		handleSendBack(event.getPlayer(), config.getSendBackTo());
	}
	
	@Subscribe
	public void onQueueFlush(PlayerQueueFlushEvent event) {
		BungeemodeConfig config = addon.getConfig();
		
		if (!config.isEnabled()) {
			return;
		}
		
		event.setResult(FlushResult.DENY);
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onGameEnd(final GameEndEvent event) {
		final BungeemodeConfig config = addon.getConfig();
		if (!config.isEnabled()) {
			return;
		}
		
		DefaultConfig defaultConfig = addon.getHeavySpleef().getConfiguration(ConfigType.DEFAULT_CONFIG);
		final GeneralSection generalSection = defaultConfig.getGeneralSection();
		restarting = true;
		
		if (addon.getHeavySpleef().getPlugin().isEnabled()) {
			int restartCountdown = config.getRestartCountdown();
			
			new CountdownTask(addon.getHeavySpleef().getPlugin(), restartCountdown, new CountdownTask.CountdownCallback() {
				
				@Override
				public void onCountdownFinish(CountdownTask task) {
					Bukkit.broadcastMessage(generalSection.getSpleefPrefix() + " " + addon.getI18n().getString(BungeemodeMessages.SERVER_RESTART));
					
					for (Player player : Bukkit.getOnlinePlayers()) {
						SpleefPlayer spleefPlayer = addon.getHeavySpleef().getSpleefPlayer(player);
						handleSendBack(spleefPlayer, config.getSendBackTo());
					}
					
					if (config.isRestart()) {
						Bukkit.getScheduler().runTaskLater(addon.getHeavySpleef().getPlugin(), new Runnable() {
							
							@Override
							public void run() {								
								restartServer();
								restarting = false;
							}
						}, 10L);
					} else {
						restarting = false;
					}
				}
				
				@Override
				public void onCountdownCount(CountdownTask task) {
					int remaining = task.getRemaining();
					
					if (remaining % 10 == 0 || remaining <= 5) {
						Bukkit.broadcastMessage(generalSection.getSpleefPrefix() + " "
								+ addon.getI18n().getVarString(BungeemodeMessages.SERVER_RESTART_COUNTDOWN)
										.setVariable("left", String.valueOf(remaining)).toString());
					}
				}
			}).start();
			
			if (restartCountdown > 0) {
				Bukkit.broadcastMessage(generalSection.getSpleefPrefix() + " "
						+ addon.getI18n().getVarString(BungeemodeMessages.SERVER_RESTART_COUNTDOWN)
								.setVariable("left", String.valueOf(restartCountdown)).toString());
			}
		}
	}

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        BungeemodeConfig config = addon.getConfig();

        if (!config.isEnabled() || !config.isUseMotd()) {
            return;
        }

        if (!addon.getHeavySpleef().isGamesLoaded()) {
            event.setMotd(addon.getI18n().getString(BungeemodeMessages.MOTD_SERVER_STARTING));
            return;
        }

        String gameName = config.getGame();
        GameManager manager = addon.getHeavySpleef().getGameManager();

        if (!manager.hasGame(gameName)) {
            addon.getLogger().log(Level.WARNING, "Cannot handle server list ping: Game " + gameName + " does not exist!");
            return;
        }

        Game game = manager.getGame(gameName);
        String motdKey;

        switch (game.getGameState()) {
            case DISABLED:
                motdKey = BungeemodeMessages.MOTD_DISABLED;
                break;
            case WAITING:
                motdKey = BungeemodeMessages.MOTD_WAITING;
                break;
            case LOBBY:
                motdKey = BungeemodeMessages.MOTD_LOBBY;
                break;
            case STARTING:
                motdKey = BungeemodeMessages.MOTD_COUNTDOWN;
                break;
            case INGAME:
                motdKey = BungeemodeMessages.MOTD_INGAME;
                break;
            default:
                motdKey = BungeemodeMessages.MOTD_UNKNOWN;
                break;
        }

        String motd = addon.getI18n().getString(motdKey);
        event.setMotd(motd);
    }
	
	@Subscribe
	public void onPlayerLeaveSpectate(SpectateLeaveEvent event) {
		final BungeemodeConfig config = addon.getConfig();
		if (!config.isEnabled()) {
			return;
		}
		
		if (!event.getGame().getName().equalsIgnoreCase(config.getGame())) {
			return;
		}
		
		SpleefPlayer player = event.getPlayer();
		if (addon.getSendBackExceptions().contains(player)) {
			return;
		}
		
		addon.getSendBackExceptions().remove(player);
		handleSendBack(player, config.getSendBackTo());
	}
	
	private void handleSendBack(SpleefPlayer player, String server) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		
		try {
			out.writeUTF("Connect");
			out.writeUTF(server);
		} catch (IOException e) {
			addon.getLogger().log(Level.SEVERE, "Failed to send player " + player.getName() + " back to the server '" + server + "'", e);
		}
		
		byte[] message = baos.toByteArray();
		player.getBukkitPlayer().sendPluginMessage(addon.getHeavySpleef().getPlugin(), BungeemodeAddon.BUNGEECORD_CHANNEL, message);
	}
	
	private boolean restartServer() {
		Server.Spigot spigot = Bukkit.spigot();
		
		try {
			spigot.getClass().getMethod("restart");
			
			//Ok, just restart using the default way
			spigot.restart();
			return false;
		} catch (NoSuchMethodException | SecurityException e) {
			//Not available, try to access the RestartCommand via reflection
			try {
				Class<?> restartCommandClazz = Class.forName("org.spigotmc.RestartCommand");
				Class<?> spigotConfigClazz = Class.forName("org.spigotmc.SpigotConfig");
				
				Field field = spigotConfigClazz.getField("restartScript");
				String restartScript = (String) field.get(null);
				
				Method restartMethod = restartCommandClazz.getDeclaredMethod("restart", File.class);
				restartMethod.invoke(null, new File(restartScript));
				return true;
			} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
					| NoSuchMethodException | InvocationTargetException e1) {
				addon.getLogger().log(Level.SEVERE, "Failed to restart server", e1);
				return false;
			}
		}
	}
	
}