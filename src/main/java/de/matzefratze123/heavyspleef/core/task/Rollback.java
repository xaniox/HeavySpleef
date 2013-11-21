package de.matzefratze123.heavyspleef.core.task;

import java.io.File;
import java.io.FileNotFoundException;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.GameComponents;
import de.matzefratze123.heavyspleef.core.region.FloorCuboid;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.hooks.HookManager;
import de.matzefratze123.heavyspleef.hooks.WorldEditHook;
import de.matzefratze123.heavyspleef.util.Logger;

public class Rollback {

	private Game game;
	private LocalSession localSession;
	private EditSession editSession;
	private WorldEdit worldEdit;
	
	private static final int UNLIMITED_BLOCKS = -1;
	
	public Rollback(Game game) {
		this.game = game;
		this.worldEdit = HookManager.getInstance().getService(WorldEditHook.class).getHook().getWorldEdit();
		this.localSession = new LocalSession(worldEdit.getConfiguration());
		this.editSession = new EditSession(new BukkitWorld(game.getWorld()), UNLIMITED_BLOCKS);
	}
	
	public void rollback() {
		for (IFloor floor : game.getComponents().getFloors()) {
			FloorCuboid cuboidFloor = (FloorCuboid) floor;
			
			if (cuboidFloor.getRandomWool()) {
				cuboidFloor.generateWool();
			} else {
				File file = new File(((GameComponents)floor.getGame().getComponents()).getFloorFolder(), floor.getId() + "." + IFloor.FILE_EXTENSION);
				
				try {
					if (!file.exists()) {
						throw new FileNotFoundException("Could not find schematic file for floor " + floor.getId());
					}
					
					editSession.enableQueue();
	                localSession.setClipboard(SchematicFormat.MCEDIT.load(file));
	                localSession.getClipboard().place(editSession, localSession.getClipboard().getOrigin(), false);
	                editSession.flushQueue();
				} catch (Exception e) {
					Logger.severe("Could not rollback floor " + floor.getId() + ": " + e.getMessage());
				}
			}
			
			
		}
	}
	
}
