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

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Permissions;
import de.matzefratze123.heavyspleef.core.Unregister;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DefaultConfig;
import de.matzefratze123.heavyspleef.core.config.GeneralSection;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.event.PlayerEnterQueueEvent;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.extension.Extension;
import de.matzefratze123.heavyspleef.core.extension.ExtensionRegistry;
import de.matzefratze123.heavyspleef.core.extension.SignExtension;
import de.matzefratze123.heavyspleef.core.flag.BukkitListener;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagInit;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.player.PlayerStateHolder;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "spectate", hasCommands = true)
@BukkitListener
public class FlagSpectate extends LocationFlag {
	
	private static final String SPLEEF_COMMAND = "spleef";
	
	private Set<SpleefPlayer> spectators;
	private Set<SpleefPlayer> deadPlayers;
	
	@Command(name = "spectate", description = "Spectates a spleef game", 
			usage = "/spleef spectate [game]", permission = Permissions.PERMISSION_SPECTATE)
	@PlayerOnly
	public static void onSpectateCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		SpleefPlayer spleefPlayer = heavySpleef.getSpleefPlayer(player);
		String gameName = context.getStringSafely(0);
		final I18N i18n = I18NManager.getGlobal();
		
		GameManager manager = heavySpleef.getGameManager();
		
		Game game = null;
		FlagSpectate spectateFlag = null;
		
		for (Game otherGame : manager.getGames()) {
			if (!otherGame.isFlagPresent(FlagSpectate.class)) {
				continue;
			}
			
			FlagSpectate flag = otherGame.getFlag(FlagSpectate.class);
			if (!flag.isSpectating(spleefPlayer)) {
				continue;
			}
			
			game = otherGame;
			spectateFlag = flag;
			break;
		}
		
		if (game == null) {
			CommandValidate.isTrue(!gameName.isEmpty(), i18n.getVarString(Messages.Command.USAGE_FORMAT)
					.setVariable("usage", context.getCommand().getUsage())
					.toString());
			CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
					.setVariable("game", gameName)
					.toString());
			
			game = manager.getGame(gameName);
			
			spectateFlag = game.getFlag(FlagSpectate.class);
			CommandValidate.notNull(spectateFlag, i18n.getString(Messages.Player.NO_SPECTATE_FLAG));
		}
		
		CommandValidate.isTrue(game.getFlag(FlagQueueLobby.class) == null || !game.isQueued(spleefPlayer), 
				i18n.getString(Messages.Command.CANNOT_SPECTATE_IN_QUEUE_LOBBY));
		
		if (!spectateFlag.isSpectating(spleefPlayer)) {			
			spectateFlag.spectate(spleefPlayer, game);
			spleefPlayer.sendMessage(i18n.getVarString(Messages.Player.PLAYER_SPECTATE)
					.setVariable("game", game.getName())
					.toString());
		} else {
			spectateFlag.leave(spleefPlayer);
			spleefPlayer.sendMessage(i18n.getVarString(Messages.Player.PLAYER_LEAVE_SPECTATE)
					.setVariable("game", game.getName())
					.toString());
		}
	}
	
	@FlagInit
	public static void initSpectateSign(HeavySpleef heavySpleef) {
		ExtensionRegistry registry = heavySpleef.getExtensionRegistry();
		registry.registerExtension(SpectateSignExtension.class);
	}
	
	@Unregister
	public static void unregisterSpectateSign(HeavySpleef heavySpleef) {
		ExtensionRegistry registry = heavySpleef.getExtensionRegistry();
		registry.registerExtension(SpectateSignExtension.class);
	}
	
	public FlagSpectate() {
		this.spectators = Sets.newHashSet();
		this.deadPlayers = Sets.newHashSet();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the spectate mode for Spleef");
	}
	
	@Subscribe
	public void onPlayerPreJoin(PlayerPreJoinGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		
		if (isSpectating(player)) {
			leave(player);
			player.sendMessage(getI18N().getVarString(Messages.Player.PLAYER_LEAVE_SPECTATE)
					.setVariable("game", event.getGame().getName())
					.toString());
		}
	}
	
	@Subscribe
	public void onPlayerEnterQueue(PlayerEnterQueueEvent event) {
		if (!isSpectating(event.getPlayer())) {
			return;
		}
		
		Game game = event.getGame();
		
		if (game.getFlag(FlagQueueLobby.class) != null) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getEntity());
		if (!isSpectating(player)) {
			return;
		}
		
		deadPlayers.add(player);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		if (!deadPlayers.contains(player)) {
			return;
		}
		
		deadPlayers.remove(player);
		leave(player);
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		HumanEntity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(entity);
		if (!isSpectating(player)) {
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		if (!isSpectating(player)) {
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Player)) {
			return;
		}
		
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(entity);
		if (!isSpectating(player)) {
			return;
		}
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		if (!isSpectating(player)) {
			return;
		}
		
		String message = event.getMessage();
		String[] components = message.split(" ");
		
		String command = components[0];
		command = command.substring(1);
		
		DefaultConfig config = getHeavySpleef().getConfiguration(ConfigType.DEFAULT_CONFIG);
		GeneralSection section = config.getGeneralSection();
		
		List<String> whitelistedCommands = section.getWhitelistedCommands();
		if (whitelistedCommands.contains(command) || command.equalsIgnoreCase(SPLEEF_COMMAND)) {
			return;
		}
		
		//Block this command
		event.setCancelled(true);
		event.getPlayer().sendMessage(getI18N().getString(Messages.Player.COMMAND_NOT_ALLOWED));
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		handleQuit(event);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		handleQuit(event);
	}
	
	private void handleQuit(PlayerEvent event) {
		SpleefPlayer player = getHeavySpleef().getSpleefPlayer(event.getPlayer());
		if (!isSpectating(player)) {
			return;
		}
		
		leave(player);
	}
	
	public void spectate(SpleefPlayer player, Game game) {
		player.savePlayerState(this);
		PlayerStateHolder.applyDefaultState(player.getBukkitPlayer());
		
		spectators.add(player);
		
		player.teleport(getValue());
		FlagAllowSpectateFly allowFlyFlag = getChildFlag(FlagAllowSpectateFly.class, game);
		if (allowFlyFlag != null) {
			allowFlyFlag.onSpectateEnter(player);
		}
	}
	
	public void leave(SpleefPlayer player) {
		PlayerStateHolder state = player.getPlayerState(this);
		if (state != null) {
			state.apply(player.getBukkitPlayer(), true);
		} else {
			//Ugh, something went wrong
			player.sendMessage(getI18N().getString(Messages.Player.ERROR_ON_INVENTORY_LOAD));
		}
		
		spectators.remove(player);
	}
	
	public boolean isSpectating(SpleefPlayer player) {
		return spectators.contains(player);
	}
	
	public Set<SpleefPlayer> getSpectators() {
		return ImmutableSet.copyOf(spectators);
	}
	
	@Extension(name = "spectate-sign")
	public static class SpectateSignExtension extends SignExtension {

		public static final String IDENTIFIER = "spectate";
		private final I18N i18n = I18NManager.getGlobal();
		
		@SuppressWarnings("unused")
		private SpectateSignExtension() {}
		
		public SpectateSignExtension(Location location) {
			super(location);
		}

		@Override
		public void onSignClick(SpleefPlayer player) {
			GameManager manager = getHeavySpleef().getGameManager();
			
			if (manager.getGame(player) != null) {
				player.sendMessage(i18n.getString(Messages.Command.CANNOT_DO_THAT_INGAME));
				return;
			}
			
			Game game = getGame();
			if (!game.isFlagPresent(FlagSpectate.class)) {
				player.sendMessage(i18n.getVarString(Messages.Command.GAME_DOESNT_ALLOW_SPECTATE)
						.setVariable("game", game.getName())
						.toString());
				return;
			}
			
			FlagSpectate flag = game.getFlag(FlagSpectate.class);
			
			if (flag.isSpectating(player)) {
				flag.leave(player);
				player.sendMessage(i18n.getVarString(Messages.Player.PLAYER_LEAVE_SPECTATE)
						.setVariable("game", game.getName())
						.toString());
			} else {
				flag.spectate(player, game);
				player.sendMessage(i18n.getVarString(Messages.Player.PLAYER_SPECTATE)
						.setVariable("game", game.getName())
						.toString());
			}
		}
		
		@Override
		public SignLayout retrieveSignLayout() {
			SignLayoutConfiguration config = heavySpleef.getConfiguration(ConfigType.SPECTATE_SIGN_LAYOUT_CONFIG);
			return config.getLayout();
		}
		
	}
	
}