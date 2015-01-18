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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.bukkit.block.Block;
import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
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

@Entity
@Table(name = "floors")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleCuboidFloor implements Floor {

	private static final int NO_LIMIT = -1;
	
	@XmlTransient
	@Transient
	private final EditSessionFactory factory;
	@XmlElement
	private String name;
	@XmlTransient
	@Transient
	private Clipboard floorClipboard;
	
	private SimpleCuboidFloor() {
		this.factory = new EditSessionFactory();
	}
	
	public SimpleCuboidFloor(String name, Clipboard clipboard) {
		this();
		
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
	public void regenerate() {
		Region region = floorClipboard.getRegion();
		World world = region.getWorld();
		WorldData data = world.getWorldData();
		
		EditSession session = factory.getEditSession(world, NO_LIMIT);
		ClipboardHolder holder = new ClipboardHolder(floorClipboard, data);
		
		Operation pasteOperation = holder.createPaste(session, data)
				.to(region.getMinimumPoint())
				.ignoreAirBlocks(false)
				.build();
		
		try {
			Operations.completeLegacy(pasteOperation);
		} catch (MaxChangedBlocksException e) {
			//Should not happen as we gave the session NO_LIMIT
			e.printStackTrace();
		}
	}
	
}
