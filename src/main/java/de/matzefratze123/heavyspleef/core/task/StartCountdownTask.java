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

import org.bukkit.Sound;

import de.matzefratze123.heavyspleef.config.ConfigUtil;
import de.matzefratze123.heavyspleef.config.sections.SettingsSectionMessages.MessageType;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.ExperienceBar;
import de.matzefratze123.heavyspleef.util.I18N;

public class StartCountdownTask extends Countdown implements CountdownListener {
	
	public static final String TASK_ID_KEY = "startCountdown";
	
	private Game game;
	
	public StartCountdownTask(Game game, int ticks) {
		super(ticks);
		
		this.game = game;
		
		addCountdownListener(this);
	}

	@Override
	public void onStart() {}

	@Override
	public void onCancel() {}

	@Override
	public void onFinish() {
		for (SpleefPlayer player : game.getIngamePlayers()) {
			player.getBukkitPlayer().setExp(0f);
		}
		
		cancel();
		game.start();
	}

	@Override
	public void onTick() {
		game.setCountLeft(getTicksLeft());
		
		int procent = getTicksLeft() * 100 / getFinishTicks();
		
		for (SpleefPlayer player : game.getIngamePlayers()) {
			if (getTicksLeft() <= 5) {
				player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
			}
			
			ExperienceBar bar = new ExperienceBar(player.getBukkitPlayer());
			bar.setExp(procent);
		}
		
		if (getTicksLeft() <= 5){
			game.broadcast(I18N._("gameIsStarting", String.valueOf(getTicksLeft())), ConfigUtil.getBroadcast(MessageType.GAME_COUNTDOWN));
		} else {//Do pre countdown
			if (getTicksLeft() % 5 == 0)
				//Only message if the remaining value is divisible by 5
				game.broadcast(I18N._("gameIsStarting", String.valueOf(getTicksLeft())), ConfigUtil.getBroadcast(MessageType.GAME_COUNTDOWN));
		}
	}

	@Override
	public void onPause() {}

	@Override
	public void onUnpause() {}
	
	
}
