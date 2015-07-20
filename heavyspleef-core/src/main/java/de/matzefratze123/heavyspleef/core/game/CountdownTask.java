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
package de.matzefratze123.heavyspleef.core.game;

import lombok.Getter;

import org.bukkit.plugin.Plugin;

import de.matzefratze123.heavyspleef.core.SimpleBasicTask;

public class CountdownTask extends SimpleBasicTask {
	
	private @Getter final int length;
	private @Getter int remaining;
	private CountdownCallback callback;
	
	public CountdownTask(Plugin plugin, int length, CountdownCallback callback) {
		super(plugin, TaskType.SYNC_REPEATING_TASK, 0L, 20L);
		
		this.length = length;
		this.remaining = length;
		this.callback = callback;
	}
	
	@Override
	public void run() {
		if (remaining == 0) {
			if (callback != null) {
				callback.onCountdownFinish(this);
			}
			
			cancel();
		} else {
			if (callback != null) {
				callback.onCountdownCount(this);
			}
		}
		
		--remaining;
	}
	
	public interface CountdownCallback {
		
		public void onCountdownCount(CountdownTask task);
		
		public void onCountdownFinish(CountdownTask task);
		
	}

}
