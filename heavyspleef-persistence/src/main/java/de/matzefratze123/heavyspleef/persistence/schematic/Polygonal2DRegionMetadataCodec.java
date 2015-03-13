package de.matzefratze123.heavyspleef.persistence.schematic;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.regions.Polygonal2DRegion;

public class Polygonal2DRegionMetadataCodec implements RegionMetadataCodec<Polygonal2DRegion> {

	@Override
	public void apply(Map<String, Tag> tags, Polygonal2DRegion region) {
		List<BlockVector2D> points = region.getPoints();
		int minY = region.getMinimumY();
		int maxY = region.getMaximumY();
		
		List<ListTag> pointList = Lists.newArrayList();
		for (BlockVector2D vector : points) {
			List<IntTag> vectorList = Lists.newArrayList();
			vectorList.add(new IntTag("x", vector.getBlockX()));
			vectorList.add(new IntTag("z", vector.getBlockZ()));
			
			ListTag vectorListTag = new ListTag(IntTag.class, vectorList);
			pointList.add(vectorListTag);
		}
		
		ListTag pointListTag = new ListTag(ListTag.class, pointList);
		
		tags.put("points", pointListTag);
		tags.put("minY", new IntTag(minY));
		tags.put("maxY", new IntTag(maxY));
	}

	@Override
	public Polygonal2DRegion asRegion(Map<String, Tag> tags) {
		ListTag pointsListTag = (ListTag) tags.get("points");
		List<Tag> pointList = pointsListTag.getValue();
		List<BlockVector2D> points = Lists.newArrayList();
		
		for (Tag vectorTag : pointList) {
			if (!(vectorTag instanceof ListTag)) {
				continue;
			}
			
			ListTag vectorListTag = (ListTag) vectorTag;
			int x = vectorListTag.getInt(0);
			int z = vectorListTag.getInt(1);
			
			BlockVector2D vector = new BlockVector2D(x, z);
			points.add(vector);
		}
		
		int minY = (int) tags.get("minY").getValue();
		int maxY = (int) tags.get("maxY").getValue();
		
		Polygonal2DRegion region = new Polygonal2DRegion();
		for (BlockVector2D point : points) {
			region.addPoint(point);
		}
		
		region.setMinimumY(minY);
		region.setMaximumY(maxY);
		return region;
	}

}
