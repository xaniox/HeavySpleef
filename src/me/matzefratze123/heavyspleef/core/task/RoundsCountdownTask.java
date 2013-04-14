/**
 *   HeavySpleef - The simple spleef plugin for bukkit
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
package me.matzefratze123.heavyspleef.core.task;

import static me.matzefratze123.heavyspleef.core.flag.FlagType.ROUNDS;
import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.command.HSCommand;
import me.matzefratze123.heavyspleef.core.Game;
import me.matzefratze123.heavyspleef.core.GameState;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class RoundsCountdownTask extends Countdown {

	private Game game;
	
	public RoundsCountdownTask(int start, Game game) {
		super(start);
		this.game = game;
		game.setGameState(GameState.COUNTING);
	}
	
	@Override
	public void onCount() {
		if (getTimeRemaining() <= 5){//Do improved countdown
			if (HeavySpleef.getSystemConfig().getBoolean("sounds.plingSound", true)) {
				for (Player p : game.getPlayers())
					p.playSound(p.getLocation(), Sound.NOTE_PLING, 4.0F, p.getLocation().getPitch());
			}
			game.tellAll(Game._("roundStartsIn", String.valueOf(getTimeRemaining())));
		} else {//Do pre countdown
			if (getTimeRemaining() % 5 == 0)//Only message if the remaining value is divisible by 5
				game.tellAll(Game._("roundStartsIn", String.valueOf(getTimeRemaining())));
		}
	}
	
	@Override
	public void onFinish() {
		int rounds = game.getFlag(ROUNDS);
		
		game.setGameState(GameState.INGAME);
		game.broadcast(HSCommand.__(ChatColor.DARK_BLUE + "GO!"));
		game.broadcast(Game._("roundStarted", String.valueOf(game.getCurrentRound()), String.valueOf(rounds)));
		game.removeBoxes();
		Bukkit.getScheduler().cancelTask(game.roundTid);
	}

}
