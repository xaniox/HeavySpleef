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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CylinderRegion;

public class CylinderRegionMetadataCodec implements RegionMetadataCodec<CylinderRegion> {

	@Override
	public void apply(Map<String, Tag> tags, CylinderRegion region) {
		Vector center = region.getCenter();
		Vector2D radius = region.getRadius();
		int minY = region.getMinimumY();
		int maxY = region.getMaximumY();
		
		List<IntTag> centerList = Lists.newArrayList();
		centerList.add(new IntTag(center.getBlockX()));
		centerList.add(new IntTag(center.getBlockY()));
		centerList.add(new IntTag(center.getBlockZ()));
		
		ListTag centerTag = new ListTag(IntTag.class, centerList);
		
		List<IntTag> radiusList = Lists.newArrayList();
		radiusList.add(new IntTag(radius.getBlockX()));
		radiusList.add(new IntTag(radius.getBlockZ()));
		
		ListTag radiusTag = new ListTag(IntTag.class, radiusList);
		
		tags.put("center", centerTag);
		tags.put("radius", radiusTag);
		tags.put("minY", new IntTag(minY));
		tags.put("maxY", new IntTag(maxY));
	}

	@Override
	public CylinderRegion asRegion(Map<String, Tag> tags) {
		ListTag centerTag = (ListTag) tags.get("center");
		ListTag radiusTag = (ListTag) tags.get("radius");
		
		int centerX = centerTag.getInt(0);
		int centerY = centerTag.getInt(1);
		int centerZ = centerTag.getInt(2);
		
		int pos2X = radiusTag.getInt(0);
		int pos2Z = radiusTag.getInt(1);
		
		Vector center = new Vector(centerX, centerY, centerZ);
		Vector2D radius = new Vector2D(pos2X, pos2Z);
		int minY = (int) tags.get("minY").getValue();
		int maxY = (int) tags.get("maxY").getValue();
		
		return new CylinderRegion(center, radius, minY, maxY);
	}

}
