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
package de.matzefratze123.heavyspleef.persistence.schematic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.floor.SimpleCuboidFloor;

public class FloorAccessor extends SchematicAccessor<Floor> {

	private static final String ROOT_TAG_NAME = "floor-schematic";
	
	@Override
	public Class<Floor> getObjectClass() {
		return Floor.class;
	}

	@Override
	public void write(OutputStream out, Floor floor) throws IOException, CodecException {
		StringTag nameTag = new StringTag("name", floor.getName());
		
		Clipboard clipboard = floor.getClipboard();
		Region region = clipboard.getRegion();
		
		int width = region.getWidth();
		int height = region.getHeight();
		int length = region.getLength();
		
		ShortTag widthTag = new ShortTag("width", (short)width);
		ShortTag heightTag = new ShortTag("height", (short)height);
		ShortTag lengthTag = new ShortTag("length", (short)length);
		
		Map<String, Tag> boundariesMap = Maps.newHashMap();
		boundariesMap.put("width", widthTag);
		boundariesMap.put("height", heightTag);
		boundariesMap.put("length", lengthTag);
		
		CompoundTag boundariesTag = new CompoundTag("boundaries", boundariesMap);
		
		Vector origin = clipboard.getOrigin();
		
		List<IntTag> originCoordinateList = Lists.newArrayList();
		originCoordinateList.add(new IntTag(origin.getBlockX()));
		originCoordinateList.add(new IntTag(origin.getBlockY()));
		originCoordinateList.add(new IntTag(origin.getBlockZ()));
		
		ListTag originTag = new ListTag("origin", IntTag.class, originCoordinateList);
		
		byte[] blocks = new byte[width * height * length];
		byte[] data = new byte[width * height * length];
		byte[] addBlocks = null;
		List<Tag> tileEntities = new ArrayList<Tag>();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int z = 0; z < length; z++) {
					int index = y * width * length + z * width + x;
					BaseBlock block = clipboard.getBlock(new Vector(x, y, z));
					
					if (block.getId() > Byte.MAX_VALUE - Byte.MIN_VALUE) {
						if (addBlocks == null) {
							addBlocks = new byte[(blocks.length >> 1) + 1];
						}
						
						if ((index & 1) == 0) {
							data[index >> 1] = (byte) (data[index >> 1] & 0xF0 | (block.getId() >> 8) & 0x0F);
						} else {
							data[index >> 1] = (byte) (data[index >> 1] & 0x0F | (block.getId() >> 4) & 0xF0);
						}
					}
					
					blocks[index] = (byte)(block.getId() & 0xFF);
					data[index] = (byte)(block.getData() & 0xFF);
					
					// Get the list of key/values from the block
					CompoundTag rawTag = block.getNbtData();
					if (rawTag != null) {
						Map<String, Tag> values = new HashMap<String, Tag>();
						for (Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
							values.put(entry.getKey(), entry.getValue());
						}
						
						values.put("id", new StringTag("id", block.getNbtId()));
						values.put("x", new IntTag("x", x));
						values.put("y", new IntTag("y", y));
						values.put("z", new IntTag("z", z));
						
						CompoundTag tileEntityTag = new CompoundTag("", values);
						tileEntities.add(tileEntityTag);
					}
				}
			}
		}
		
		ByteArrayTag blocksTag = new ByteArrayTag("blocks", blocks);
		ByteArrayTag dataTag = new ByteArrayTag("data", data);
		ListTag tileEntitiesTag = new ListTag("tileentities", CompoundTag.class, tileEntities);
		
		Map<String, Tag> childs = Maps.newHashMap();
		childs.put("name", nameTag);
		childs.put("boundaries", boundariesTag);
		childs.put("origin", originTag);
		childs.put("blocks", blocksTag);
		childs.put("data", dataTag);
		childs.put("tileentities", tileEntitiesTag);
		
		if (addBlocks != null) {
			childs.put("addblocks", new ByteArrayTag("addblocks", addBlocks));
		}
		
		CompoundTag rootTag = new CompoundTag(ROOT_TAG_NAME, childs);
		
		try (NBTOutputStream nbtOut = new NBTOutputStream(
				new GZIPOutputStream(out))) {
			nbtOut.writeTag(rootTag);
		}
	}

	@Override
	public Floor read(InputStream in) throws IOException, CodecException {
		NBTInputStream nbtStream = new NBTInputStream(
				new GZIPInputStream(in));
		
		CompoundTag rootTag = (CompoundTag) nbtStream.readTag();
		nbtStream.close();
		
		if (rootTag.getName().equals(ROOT_TAG_NAME)) {
			throw new CodecException("Tag \"" + ROOT_TAG_NAME + "\" does not exist or is not first");
		}
		
		Vector origin = new Vector();
		
		Map<String, Tag> childs = rootTag.getValue();
		
		CompoundTag boundariesTag = getChildTag(childs, "boundaries", CompoundTag.class);
		Map<String, Tag> boundariesChilds = boundariesTag.getValue();
		
		String name = getChildTag(childs, "name", StringTag.class).getValue();
		short length = getChildTag(boundariesChilds, "length", ShortTag.class).getValue();
		short width = getChildTag(boundariesChilds, "width", ShortTag.class).getValue();
		short height = getChildTag(boundariesChilds, "height", ShortTag.class).getValue();
		
		if (childs.containsKey("origin")) {
			ListTag originTag = getChildTag(childs, "origin", ListTag.class);
			if (!IntTag.class.isAssignableFrom(originTag.getType())) {
				throw new CodecException("Found list tag \"origin\" but their type is not an instance of IntTag");
			}
	
			List<Tag> originCoordinates = originTag.getValue();
			if (originCoordinates.size() < 3) {
				throw new CodecException("List Tag \"origin\" must have at least 3 entries (x, y, z)");
			}
			
			int originX = ((IntTag)originCoordinates.get(0)).getValue();
			int originY = ((IntTag)originCoordinates.get(1)).getValue();
			int originZ = ((IntTag)originCoordinates.get(2)).getValue();
			
			origin = new Vector(originX, originY, originZ);
		}
		
		// Get blocks
		byte[] blockId = getChildTag(childs, "blocks", ByteArrayTag.class).getValue();
		byte[] blockData = getChildTag(childs, "data", ByteArrayTag.class).getValue();
		byte[] addId = new byte[0];
		short[] blocks = new short[blockId.length]; // Have to later combine IDs
		
		// We support 4096 block IDs using the same method as vanilla Minecraft, where
		// the highest 4 bits are stored in a separate byte array.
		if (childs.containsKey("addblocks")) {
			addId = getChildTag(childs, "addblocks", ByteArrayTag.class).getValue();
		}
		
		// Combine the AddBlocks data with the first 8-bit block ID
		for (int index = 0; index < blockId.length; index++) {
			if ((index >> 1) >= addId.length) { 
				// No corresponding AddBlock index
				blocks[index] = (short) (blockId[index] & 0xFF);
			} else {
				if ((index & 1) == 0) {
					blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
				} else {
					blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
				}
			}
		}
		
		// Need to pull out tile entities
		List<Tag> tileEntities = getChildTag(childs, "tileentities", ListTag.class).getValue();
		Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<BlockVector, Map<String, Tag>>();
		
		for (Tag tag : tileEntities) {
			if (!(tag instanceof CompoundTag)) {
				continue;
			}
			
			CompoundTag t = (CompoundTag) tag;
			
			int x = 0;
			int y = 0;
			int z = 0;
			
			Map<String, Tag> values = new HashMap<String, Tag>();
			for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
				if (entry.getKey().equals("x")) {
					if (entry.getValue() instanceof IntTag) {
						x = ((IntTag) entry.getValue()).getValue();
					}
				} else if (entry.getKey().equals("y")) {
					if (entry.getValue() instanceof IntTag) {
						y = ((IntTag) entry.getValue()).getValue();
					}
				} else if (entry.getKey().equals("z")) {
					if (entry.getValue() instanceof IntTag) {
						z = ((IntTag) entry.getValue()).getValue();
					}
				}
				
				values.put(entry.getKey(), entry.getValue());
			}
			
			BlockVector vec = new BlockVector(x, y, z);
			tileEntitiesMap.put(vec, values);
		}
		
		Vector size = new Vector(width, height, length);
		Vector maxPt = origin.add(size);
		
		Region region = new CuboidRegion(origin, maxPt);
		BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
		clipboard.setOrigin(origin);
		
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {
					int index = y * width * length + z * width + x;
					BlockVector pt = new BlockVector(x, y, z);
					BaseBlock block = new BaseBlock(blocks[index], blockData[index]);
					
					if (tileEntitiesMap.containsKey(pt)) {
						block.setNbtData(new CompoundTag(tileEntitiesMap.get(pt)));
					}
					
					try {
						clipboard.setBlock(pt, block);
					} catch (WorldEditException e) {
						throw new CodecException(e);
					}
				}
			}
		}
		
		Floor floor = new SimpleCuboidFloor(name, clipboard);
		return floor;
	}
	
	private static <T extends Tag> T getChildTag(Map<String, Tag> childs, String name, Class<T> expected) throws CodecException {
		if (!childs.containsKey(name)) {
			throw new CodecException("Missing nbt tag \"" + name + "\"");
		}
		
		Tag childTag = childs.get(name);
		if (!expected.isInstance(childTag)) {
			throw new CodecException("Child tag \"" + name + "\" is not an instance of " + expected.getName());
		}
		
		return expected.cast(childTag);
	}

}
