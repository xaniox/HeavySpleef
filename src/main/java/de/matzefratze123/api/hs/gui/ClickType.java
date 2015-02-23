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
package de.matzefratze123.api.hs.gui;

import org.bukkit.event.inventory.InventoryClickEvent;

public enum ClickType {

	LEFT_CLICK(false, true, false), RIGHT_CLICK(true, false, false), LEFT_SHIFT_CLICK(false, true, true), RIGHT_SHIFT_CLICK(true, false, true), OTHER_CLICK(false, false, false);

	private boolean	isLeft;
	private boolean	isRight;
	private boolean	isShift;

	private ClickType(boolean isRight, boolean isLeft, boolean isShift) {
		this.isRight = isRight;
		this.isLeft = isLeft;
		this.isShift = isShift;
	}

	public static ClickType byBooleans(boolean isRight, boolean isLeft, boolean isShift) {
		for (ClickType type : values()) {
			if (type.isRight != isRight || type.isLeft != isLeft || type.isShift != isShift) {
				continue;
			}

			return type;
		}

		return null;
	}

	public static ClickType byEvent(InventoryClickEvent e) {
		return byBooleans(e.isRightClick(), e.isLeftClick(), e.isShiftClick());
	}

}
