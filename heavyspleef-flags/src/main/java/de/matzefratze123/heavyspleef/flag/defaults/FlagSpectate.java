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
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.CommandValidate;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameManager;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Unregister;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.extension.Extension;
import de.matzefratze123.heavyspleef.core.extension.ExtensionRegistry;
import de.matzefratze123.heavyspleef.core.extension.SignExtension;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.FlagInit;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.I18NManager;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.player.PlayerStateHolder;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;

@Flag(name = "spectate")
public class FlagSpectate extends LocationFlag {
	
	private Set<SpleefPlayer> spectators;
	
	@Command(name = "spectate", description = "Spectates a spleef game", minArgs = 1, 
			usage = "/spleef spectate <game>", permission = "heavyspleef.spectate")
	@PlayerOnly
	public static void onSpectateCommand(CommandContext context, HeavySpleef heavySpleef) throws CommandException {
		Player player = context.getSender();
		SpleefPlayer spleefPlayer = heavySpleef.getSpleefPlayer(player);
		String gameName = context.getString(0);
		final I18N i18n = I18NManager.getGlobal();
		
		GameManager manager = heavySpleef.getGameManager();
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		FlagSpectate spectateFlag = game.getFlag(FlagSpectate.class);
		
		CommandValidate.isTrue(spectateFlag != null, i18n.getString(Messages.Player.NO_SPECTATE_FLAG));
		
		if (spectateFlag.isSpectating(spleefPlayer)) {			
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
		state.apply(player.getBukkitPlayer(), false);
		spectators.remove(player);
	}
	
	public boolean isSpectating(SpleefPlayer player) {
		return spectators.contains(player);
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