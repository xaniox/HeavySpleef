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

import de.xaniox.heavyspleef.core.event.PlayerLeaveGameEvent;
import de.xaniox.heavyspleef.core.event.PlayerPreJoinGameEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.core.game.Game.JoinResult;
import de.xaniox.heavyspleef.core.game.GameState;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.flag.presets.BaseFlag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;

@Flag(name = "invisible", parent = FlagSpectate.class)
public class FlagInvisibleSpectate extends BaseFlag {

	private static final String INVISIBLE_TEAM = "invisible_team";
	private Scoreboard scoreboard;
	private Team team;
	private @Inject
    Game game;
	
	@Override
	public void onFlagAdd(Game game) {
		//Run the scoreboard initialization on the server thread as this method may get called by async game load
		Bukkit.getScheduler().runTask(game.getHeavySpleef().getPlugin(), new Runnable() {
			
			@Override
			public void run() {
				scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
				team = scoreboard.registerNewTeam(INVISIBLE_TEAM);
				team.setAllowFriendlyFire(false);
				team.setCanSeeFriendlyInvisibles(true);
			}
		});
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Makes players invisible for other players when they are spectating");
	}
	
	@SuppressWarnings("deprecation")
	@Subscribe
	public void onSpectateGame(FlagSpectate.SpectateEnteredEvent event) {
		SpleefPlayer player = event.getPlayer();
		Player bukkitPlayer = player.getBukkitPlayer();
		
		team.addPlayer(bukkitPlayer);
		
		PotionEffect effect = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, true);
		bukkitPlayer.addPotionEffect(effect, true);
		
		for (SpleefPlayer ingamePlayer : game.getPlayers()) {
			if (!ingamePlayer.getBukkitPlayer().canSee(bukkitPlayer)) {
				continue;
			}
			
			ingamePlayer.getBukkitPlayer().hidePlayer(bukkitPlayer);
		}
		
		bukkitPlayer.setScoreboard(scoreboard);
	}
	
	@SuppressWarnings("deprecation")
	@Subscribe
	public void onSpectateLeave(FlagSpectate.SpectateLeaveEvent event) {
		SpleefPlayer player = event.getPlayer();
		Player bukkitPlayer = player.getBukkitPlayer();
		
		team.removePlayer(bukkitPlayer);
		bukkitPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
		
		for (SpleefPlayer ingamePlayer : game.getPlayers()) {
			if (ingamePlayer.getBukkitPlayer().canSee(bukkitPlayer)) {
				continue;
			}
			
			ingamePlayer.getBukkitPlayer().showPlayer(bukkitPlayer);
		}
		
		Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		bukkitPlayer.setScoreboard(mainScoreboard);
	}
	
	@Subscribe(priority = Subscribe.Priority.MONITOR)
	public void onPlayerJoinGame(PlayerPreJoinGameEvent event) {
		Game game = event.getGame();
		
		if (event.getJoinResult() != JoinResult.ALLOW || game.getGameState() == GameState.INGAME) {
			return;
		}
		
		SpleefPlayer player = event.getPlayer();
		Player bukkitPlayer = player.getBukkitPlayer();
		
		FlagSpectate flag = (FlagSpectate) getParent();
		
		for (SpleefPlayer spectating : flag.getSpectators()) {
			if (!bukkitPlayer.canSee(spectating.getBukkitPlayer())) {
				continue;
			}
			
			bukkitPlayer.hidePlayer(spectating.getBukkitPlayer());
		}
	}
	
	@Subscribe
	public void onPlayerLeaveGame(PlayerLeaveGameEvent event) {
		SpleefPlayer player = event.getPlayer();
		Player bukkitPlayer = player.getBukkitPlayer();
		
		FlagSpectate flag = (FlagSpectate) getParent();
		
		for (SpleefPlayer spectating : flag.getSpectators()) {
			if (bukkitPlayer.canSee(spectating.getBukkitPlayer())) {
				continue;
			}
			
			bukkitPlayer.showPlayer(spectating.getBukkitPlayer());
		}
	}

}