/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.core.floor;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;

public class SimpleClipboardFloor implements Floor {
	
	private String name;
	private Clipboard floorClipboard;
	
	public SimpleClipboardFloor(String name, Clipboard clipboard) {
		this.name = name;
		this.floorClipboard = clipboard;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Clipboard getClipboard() {
		return floorClipboard;
	}
	
	@Override
	public Region getRegion() {
		return floorClipboard.getRegion();
	}

	@Override
	public boolean contains(Block block) {
		return contains(block.getLocation());
	}
	
	@Override
	public boolean contains(Location location) {
		Vector pt = BukkitUtil.toVector(location);
		
		return floorClipboard.getRegion().contains(pt);
	}

	@Override
	public void generate(EditSession session) {
		Region region = floorClipboard.getRegion();
		World world = region.getWorld();
		WorldData data = world.getWorldData();
		
		ClipboardHolder holder = new ClipboardHolder(floorClipboard, data);
		
		Operation pasteOperation = holder.createPaste(session, data)
				.to(region.getMinimumPoint())
				.ignoreAirBlocks(true)
				.build();
		
		try {
			Operations.completeLegacy(pasteOperation);
		} catch (MaxChangedBlocksException e) {
			throw new RuntimeException(e);
		}
	}
	
}
