package de.matzefratze123.heavyspleef.core.floor;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.regions.CuboidRegion;

public interface Floor {
	
	public String getName();
	
	public CuboidClipboard getClipboard();
	
	public CuboidRegion getRegion();
	
	public boolean contains(Block block);
	
	public boolean contains(Location location);
	
	public void regenerate();
	
}
