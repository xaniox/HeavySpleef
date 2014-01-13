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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class Countdown implements Runnable, Task {

	private int finishTicks;
	private int currentTick;
	
	private boolean pause;
	
	private int pid;
	
	private List<CountdownListener> listener;
	
	public Countdown(int ticks) {
		this.finishTicks = ticks;
		this.currentTick = 0;
		this.pause = false;
		this.listener = new ArrayList<CountdownListener>();
		this.pid = -1;
	}
	
	public final int start() {
		if (pid != -1) {
			throw new IllegalStateException("Countdown already started!");
		}
		
		pid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 0L, 20L);
		for (CountdownListener l : listener) {
			l.onStart();
		}
		
		return pid;
	}
	
	public final void cancel() {
		if (pid == -1) {
			return;
		}
		
		Bukkit.getScheduler().cancelTask(pid);
		for (CountdownListener l : listener) {
			l.onCancel();
		}
		
		pid = -1;
	}
	
	public final boolean isAlive() {
		return pid != -1;
	}
	
	@Override
	public final void run() {
		if (!pause) {
			if (++currentTick < finishTicks) {
				for (CountdownListener l : listener) {
					l.onTick();
				}
			} else {
				for (CountdownListener l : listener) {
					l.onFinish();
				}
			}
		}
	}
	
	public int currentTick() {
		return currentTick;
	}
	
	public int getTicksLeft() {
		return finishTicks - currentTick;
	}
	
	public int getFinishTicks() {
		return finishTicks;
	}
	
	public void reset() {
		currentTick = 0;
	}
	
	public void pause() {
		if (pause) {
			return;
		}
		
		pause = true;
		for (CountdownListener l : listener) {
			l.onPause();
		}
	}
	
	public void unpause() {
		if (!pause) {
			return;
		}
		
		pause = false;
		for (CountdownListener l : listener) {
			l.onUnpause();
		}
	}
	
	public boolean isPause() {
		return pause;
	}
	
	public void addCountdownListener(CountdownListener listener) {
		this.listener.add(listener);
	}
	
	public void removeListener(CountdownListener listener) {
		this.listener.remove(listener);
	}
	
}
