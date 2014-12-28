package de.matzefratze123.heavyspleef.core.floor;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;

public interface Floor {
	
	public String getName();
	
	public Clipboard getClipboard();
	
	public Region getRegion();
	
	public boolean contains(Block block);
	
	public boolean contains(Location location);
	
	public void regenerate();
	
}
