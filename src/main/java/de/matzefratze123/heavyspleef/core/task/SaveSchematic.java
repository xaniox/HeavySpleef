package de.matzefratze123.heavyspleef.core.task;

import java.io.File;

import org.bukkit.Location;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

import de.matzefratze123.heavyspleef.core.GameComponents;
import de.matzefratze123.heavyspleef.core.region.FloorCuboid;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.util.Logger;
import de.matzefratze123.heavyspleef.util.Util;

public class SaveSchematic implements Runnable {

	private IFloor floor;
	private EditSession editSession;
	
	private static final int UNLIMITED_BLOCKS = -1;
	
	public SaveSchematic(IFloor floor) {
		this.floor = floor;
		this.editSession = new EditSession(new BukkitWorld(floor.getWorld()), UNLIMITED_BLOCKS);
	}

	@Override
	public void run() {
		try {
			File file = new File(((GameComponents)floor.getGame().getComponents()).getFloorFolder(), floor.getId() + "." + IFloor.FILE_EXTENSION);
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FloorCuboid cuboidFloor = (FloorCuboid) floor;
			
			Location minL = Util.getMin(cuboidFloor.getFirstPoint(), cuboidFloor.getSecondPoint());
			Location maxL = Util.getMax(cuboidFloor.getFirstPoint(), cuboidFloor.getSecondPoint());
			
			Vector min = Util.toWorldEditVector(minL);
			Vector max = Util.toWorldEditVector(maxL);
			
			editSession.enableQueue();
			CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);
			clipboard.copy(editSession);
			SchematicFormat.MCEDIT.save(clipboard, file);
			editSession.flushQueue();
		} catch (Exception e) {
			Logger.severe("Failed to save floor " + floor.getId() + " to schematic file: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
