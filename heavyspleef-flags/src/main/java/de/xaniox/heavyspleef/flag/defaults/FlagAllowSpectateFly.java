/*
 * This file is part of HeavySpleef.
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
package de.xaniox.heavyspleef.flag.defaults;

import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BooleanFlag;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

@Flag(name = "allow-fly", parent = FlagSpectate.class)
public class FlagAllowSpectateFly extends BooleanFlag {
	
	@Override
	public void onFlagRemove(Game game) {
		FlagSpectate flag = (FlagSpectate) getParent();
		Set<SpleefPlayer> spectators = flag.getSpectators();
		
		for (SpleefPlayer player : spectators) {
			Player bukkitPlayer = player.getBukkitPlayer();
			bukkitPlayer.setAllowFlight(false);
			bukkitPlayer.setFlying(false);
		}
	}
	
	@Override
	public void onFlagAdd(Game game) {
		FlagSpectate flag = (FlagSpectate) getParent();
		
		if (flag != null) {
			Set<SpleefPlayer> spectators = flag.getSpectators();
			
			for (SpleefPlayer player : spectators) {
				player.getBukkitPlayer().setAllowFlight(getValue());
			}
		}
	}
	
	public void setValue(Boolean value) {
		super.setValue(value);
		
		FlagSpectate flag = (FlagSpectate) getParent();
		
		if (flag != null) {
			Set<SpleefPlayer> spectators = flag.getSpectators();
			
			for (SpleefPlayer player : spectators) {
				Player bukkitPlayer = player.getBukkitPlayer();
				bukkitPlayer.setAllowFlight(value);
				bukkitPlayer.setFlying(value);
			}
		}
	}
	
	@Subscribe
	public void onSpectateEntered(FlagSpectate.SpectateEnteredEvent event) {
		spectateEnter(event.getPlayer());
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Enables the ability to fly while spectating");
	}
	
	/* Spectate leave is handled by restoring the player state in parent flag */
	public void spectateEnter(SpleefPlayer spleefPlayer) {		
		boolean value = getValue();
		Player player = spleefPlayer.getBukkitPlayer();
		
		player.setAllowFlight(value);
		player.setFlying(value);
	}

}