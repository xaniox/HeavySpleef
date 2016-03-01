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
package de.xaniox.heavyspleef.core;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public abstract class SimpleBasicTask implements BasicTask {
	
	private final Plugin plugin;
	private final BukkitScheduler scheduler;
	private final TaskType taskType;
	private final long[] taskArgs;
	private BukkitTask task;
	
	public SimpleBasicTask(Plugin plugin, TaskType taskType, long... taskArgs) {
		this.plugin = plugin;
		this.taskType = taskType;
		this.scheduler = Bukkit.getScheduler();
		this.taskArgs = taskArgs;
	}
	
	public long getTaskArgument(int index) {
		Validate.isTrue(index < taskArgs.length, "Index out of bounds");
		
		return taskArgs[index];
	}
	
	@Override
	public TaskType getTaskType() {
		return taskType;
	}
	
	@Override
	public void start() {
		if (isRunning()) {
			throw new IllegalStateException("Task is already running");
		}
		
		long delay = 0;
		long period = 0;
		
		if (taskType.getArgsLength() > 0) {
			Validate.isTrue(taskArgs.length >= 1, "Must specify delay in taskArgs");
			
			delay = taskArgs[0];
			
			if (taskType.getArgsLength() > 1) {
				Validate.isTrue(taskArgs.length >= 2, "Must specify delay and period in taskArgs");
				
				period = taskArgs[1];
			}
		}
		
		switch (taskType) {
		case ASYNC_DELAYED_TASK:
			task = scheduler.runTaskLaterAsynchronously(plugin, this, delay);
			break;
		case ASYNC_REPEATING_TASK:
			task = scheduler.runTaskTimerAsynchronously(plugin, this, delay, period);
			break;
		case ASYNC_RUN_TASK:
			task = scheduler.runTaskAsynchronously(plugin, this);
			break;
		case SYNC_DELAYED_TASK:
			task = scheduler.runTaskLater(plugin, this, delay);
			break;
		case SYNC_REPEATING_TASK:
			task = scheduler.runTaskTimer(plugin, this, delay, period);
			break;
		case SYNC_RUN_TASK:
			task = scheduler.runTask(plugin, this);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void cancel() {
		if (!isRunning()) {
			throw new IllegalStateException("Task is not running");
		}
		
		task.cancel();
		task = null;
	}

	@Override
	public boolean isSync() {
		return taskType.isSync();
	}

	@Override
	public boolean isRunning() {
		return task != null && (scheduler.isCurrentlyRunning(task.getTaskId()) || scheduler.isQueued(task.getTaskId()));
	}

}