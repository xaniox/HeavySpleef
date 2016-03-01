/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.xaniox.heavyspleef.migration;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.World;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.floor.SimpleClipboardFloor;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.persistence.schematic.FloorAccessor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings("deprecation")
public class FloorMigrator implements Migrator<File, OutputStream> {

	private final SchematicFormat mceditFormat = SchematicFormat.MCEDIT;
	private final FloorAccessor accessor = new FloorAccessor();
	
	@Override
	public void migrate(File inputSource, OutputStream outputSource, Object cookie) throws MigrationException {
		if (cookie == null || !(cookie instanceof Game)) {
			throw new MigrationException("Cookie must be the instance of a Game");
		}
		
		Game game = (Game) cookie;
		
		CuboidClipboard legacyClipboard;
		
		try {
			legacyClipboard = mceditFormat.load(inputSource);
		} catch (DataException | IOException e) {
			throw new MigrationException(e);
		}
		
		int width = legacyClipboard.getWidth();
		int height = legacyClipboard.getHeight();
		int length = legacyClipboard.getLength();
		
		String fileName = inputSource.getName();
		String floorName = "floor_" + fileName.substring(0, fileName.lastIndexOf('.'));
		
		Vector pos1 = legacyClipboard.getOrigin();
		Vector pos2 = pos1.add(legacyClipboard.getSize()).subtract(1, 1, 1);
		
		Region region = new CuboidRegion(pos1, pos2);
		World world = new BukkitWorld(game.getWorld());
		region.setWorld(world);
		
		Clipboard clipboard = new BlockArrayClipboard(region);
		
		//Manually copy the blocks as the legacy clipboard is not an extent
		try {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					for (int z = 0; z < length; z++) {
						Vector pos = new Vector(x, y, z);
						
						BaseBlock block = legacyClipboard.getBlock(pos);
						clipboard.setBlock(pos.add(pos1), block);
					}
				}
			}
		} catch (WorldEditException e) {
			throw new MigrationException(e);
		}
		
		Floor floor = new SimpleClipboardFloor(floorName, clipboard);
		
		try {
			accessor.write(outputSource, floor);
		} catch (IOException e) {
			throw new MigrationException(e);
		}
	}

}