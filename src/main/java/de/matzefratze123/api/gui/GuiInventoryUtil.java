package de.matzefratze123.api.gui;

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
