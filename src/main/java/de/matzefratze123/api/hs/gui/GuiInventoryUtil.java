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

public class GuiInventoryUtil {

	public static int toMinecraftSlot(int x, int y) {
		return y * GuiInventory.SLOTS_PER_LINE + x;
	}

	public static int toMinecraftSlot(GuiInventoryPoint point) {
		return toMinecraftSlot(point.getX(), point.getY());
	}

	public static GuiInventoryPoint toGuiSlot(int mcSlot) {
		int x, y;

		y = mcSlot / GuiInventory.SLOTS_PER_LINE;
		x = mcSlot % GuiInventory.SLOTS_PER_LINE;

		return new GuiInventoryPoint(x, y);
	}

}
