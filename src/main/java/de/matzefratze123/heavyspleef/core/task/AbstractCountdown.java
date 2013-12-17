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

public abstract class AbstractCountdown implements Runnable {

	protected int remaining;
	protected int start;
	
	protected AbstractCountdown(int start) {
		this.remaining = start;
		this.start = start;
	}

	@Override
	public void run() {
		if (remaining == 0) {
			onFinish();
		} else if (remaining > 0){//Do pre countdown
			onCount();
			remaining--;
		} else if (remaining < 0) {//Call the interrupt method, something is going false...
			onCorrupt();
		}
	}
	
	public void onFinish() {}
	
	public void onCount() {}
	
	public void onCorrupt() {}
	
	public int getTimeRemaining() {
		return this.remaining;
	}
	
	public int getStart() {
		return start;
	}
	
}
