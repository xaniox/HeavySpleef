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
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.i18n.I18N;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
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
		I18N i18n = I18N.getInstance();
		
		GameManager manager = heavySpleef.getGameManager();
		CommandValidate.isTrue(manager.hasGame(gameName), i18n.getVarString(Messages.Command.GAME_DOESNT_EXIST)
				.setVariable("game", gameName)
				.toString());
		
		Game game = manager.getGame(gameName);
		FlagSpectate spectateFlag = game.getFlag(FlagSpectate.class);
		
		CommandValidate.isTrue(spectateFlag != null, i18n.getString(Messages.Player.NO_SPECTATE_FLAG));
		
		if (spectateFlag.isSpectating(spleefPlayer)) {			
			spectateFlag.spectate(spleefPlayer, game);
			spleefPlayer.sendMessage(i18n.getString(Messages.Player.PLAYER_SPECTATE));
		} else {
			spectateFlag.leave(spleefPlayer);
			spleefPlayer.sendMessage(i18n.getString(Messages.Player.PLAYER_LEAVE_SPECTATE));
		}
	}
	
	public FlagSpectate() {
		this.spectators = Sets.newHashSet();
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the spectate mode for Spleef");
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
	
}