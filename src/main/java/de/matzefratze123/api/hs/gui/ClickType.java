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
