/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.core.task;

import static de.matzefratze123.heavyspleef.core.flag.FlagType.ROUNDS;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

import de.matzefratze123.heavyspleef.command.handler.HSCommand;
import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.BroadcastType;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameState;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.I18N;

public class RoundsCountdownTask extends Countdown implements CountdownListener {

	public static final String TASK_ID_KEY = "roundsTask";
	
	private Game game;
	
	public RoundsCountdownTask(Game game, int ticks) {
		super(ticks);
		
		this.game = game;
		addCountdownListener(this);
	}

	@Override
	public void onStart() {
		this.game.setGameState(GameState.COUNTING);
	}

	@Override
	public void onCancel() {
		this.game.setGameState(GameState.INGAME);
	}

	@Override
	public void onFinish() {
		int rounds = game.getFlag(ROUNDS);
		
		game.setGameState(GameState.INGAME);
		game.broadcast(HSCommand.__(ChatColor.GREEN + "GO!"), ConfigUtil.getBroadcast(MessageType.GAME_COUNTDOWN));
		game.broadcast(I18N._("roundStarted", String.valueOf(game.getRoundsPlayed() + 1), String.valueOf(rounds)), ConfigUtil.getBroadcast(MessageType.GAME_START_INFO));
		game.removeBoxes();
		cancel();
	}

	@Override
	public void onTick() {		
		if (getTicksLeft() <= 5) {
			for (SpleefPlayer player : game.getIngamePlayers()) {
				player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.NOTE_PLING, 4.0F, player.getBukkitPlayer().getLocation().getPitch());
			}
			
			game.broadcast(I18N._("roundStartsIn", String.valueOf(getTicksLeft())), BroadcastType.INGAME);
		} else {
			if (getTicksLeft() % 5 == 0) {
				game.broadcast(I18N._("roundStartsIn", String.valueOf(getTicksLeft())), BroadcastType.INGAME);
			}
		}
	}

	@Override
	public void onPause() {}

	@Override
	public void onUnpause() {}

}
