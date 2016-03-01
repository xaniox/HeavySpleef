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

public interface BasicTask extends Runnable {

	public TaskType getTaskType();
	
	public void start();
	
	public void cancel();
	
	public boolean isSync();
	
	public boolean isRunning();
	
	public enum TaskType {
		
		SYNC_RUN_TASK(true, 0),
		SYNC_DELAYED_TASK(true, 1),
		SYNC_REPEATING_TASK(true, 2),
		ASYNC_RUN_TASK(false, 0),
		ASYNC_DELAYED_TASK(false, 1),
		ASYNC_REPEATING_TASK(false, 2);
		
		private boolean sync;
		private int argsLength;
		
		private TaskType(boolean sync, int argsLength) {
			this.sync = sync;
			this.argsLength = argsLength;
		}
		
		public boolean isSync() {
			return sync;
		}
		
		public int getArgsLength() {
			return argsLength;
		}
		
	}
	
}