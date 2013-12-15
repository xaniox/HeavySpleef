package de.matzefratze123.api.gui;

public class GuiInventoryPoint {

	private int	x, y;

	public GuiInventoryPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public GuiInventoryPoint add(GuiInventoryPoint point) {
		return add(point.getX(), point.getY());
	}
	
	public GuiInventoryPoint add(int x, int y) {
		this.x += x;
		this.y += y;
		
		return this;
	}

}
