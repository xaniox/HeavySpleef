package de.matzefratze123.heavyspleef.core.floor;

import javax.xml.bind.annotation.XmlTransient;

import org.bukkit.Location;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

public class SimpleCuboidFloor implements Floor {

	private static final int NO_LIMIT = -1;
	
	@XmlTransient
	private final EditSessionFactory factory;
	private final String name;
	@XmlTransient
	private CuboidRegion region;
	@XmlTransient
	private CuboidClipboard floorClipboard;
	
	public SimpleCuboidFloor(String name, CuboidClipboard clipboard) {
		this.factory = WorldEdit.getInstance().getEditSessionFactory();
		this.name = name;
		this.floorClipboard = clipboard;
		this.region = new CuboidRegion(floorClipboard.getOrigin(), floorClipboard.getOrigin().add(floorClipboard.getSize()));
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public CuboidClipboard getClipboard() {
		return floorClipboard;
	}

	@Override
	public boolean contains(Location location) {
		Vector vector = BukkitUtil.toVector(location);
		
		return region.contains(vector);
	}

	@Override
	public void regenerate() {
		EditSession session = factory.getEditSession(region.getWorld(), NO_LIMIT);
		
		try {
			floorClipboard.place(session, region.getMinimumPoint(), false);
		} catch (MaxChangedBlocksException e) {
			// Should not happen
			e.printStackTrace();
		}
	}
	
}
