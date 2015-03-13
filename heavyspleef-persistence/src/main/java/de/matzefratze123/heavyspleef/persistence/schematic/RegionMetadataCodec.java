package de.matzefratze123.heavyspleef.persistence.schematic;

import java.util.Map;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.regions.Region;

public interface RegionMetadataCodec<T extends Region> {
	
	public void apply(Map<String, Tag> tags, T region);
	
	public T asRegion(Map<String, Tag> tags);

}
