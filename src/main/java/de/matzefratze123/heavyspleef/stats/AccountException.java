/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.stats;

public class AccountException extends Exception {

	private static final long	serialVersionUID	= 3259591029892667831L;

	private String				detailMessage;
	private Exception			parent;

	public AccountException(String message) {
		this.detailMessage = message;
	}

	public AccountException(Exception parent) {
		this.parent = parent;
	}

	public AccountException() {
	}

	@Override
	public String getMessage() {
		return parent != null ? parent.getMessage() : detailMessage == null ? super.getMessage() : detailMessage;
	}

	@Override
	public void printStackTrace() {
		if (parent != null) {
			parent.printStackTrace();
		} else {
			super.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return parent != null ? parent.toString() : toString();
	}

}
