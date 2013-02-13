package me.matzefratze123.heavyspleef.selection;

import org.bukkit.Location;

public class Selection {

	private Location firstSel;
	private Location secondSel;
	
	public Selection(Location firstSelection, Location secondSelection) {
		setFirstSel(firstSelection);
		setSecondSel(secondSelection);
	}

	public Location getSecondSel() {
		return secondSel;
	}

	public void setSecondSel(Location minSel) {
		this.secondSel = minSel;
	}

	public Location getFirstSel() {
		return firstSel;
	}

	public void setFirstSel(Location firstSel) {
		this.firstSel = firstSel;
	}
	
	
}
