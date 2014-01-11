package de.matzefratze123.heavyspleef.core.task;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class Countdown implements Runnable {

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
	
	public void startCountdown() {
		if (pid != -1) {
			throw new IllegalStateException("Countdown already started!");
		}
		
		pid = Bukkit.getScheduler().scheduleSyncRepeatingTask(HeavySpleef.getInstance(), this, 20L, 20L);
		for (CountdownListener l : listener) {
			l.onStart();
		}
	}
	
	public void cancelCountdown() {
		if (pid == -1) {
			return;
		}
		
		Bukkit.getScheduler().cancelTask(pid);
		for (CountdownListener l : listener) {
			l.onCancel();
		}
		
		pid = -1;
	}
	
	@Override
	public void run() {
		if (!pause) {
			for (CountdownListener l : listener) {
				l.onTick();
			}
			
			if (++currentTick <= finishTicks) {
				for (CountdownListener l : listener) {
					l.onFinish();
				}
				
				Bukkit.getScheduler().cancelTask(pid);
				pid = -1;
			}
		}
	}
	
	public int currentTick() {
		return currentTick;
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
	
	public void addListener(CountdownListener listener) {
		this.listener.add(listener);
	}
	
	public void removeListener(CountdownListener listener) {
		this.listener.remove(listener);
	}
	
}
