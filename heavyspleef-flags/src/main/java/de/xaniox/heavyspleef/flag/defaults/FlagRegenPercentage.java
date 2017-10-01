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
package de.xaniox.heavyspleef.flag.defaults;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import de.xaniox.heavyspleef.core.flag.Flag;
import de.xaniox.heavyspleef.core.flag.Inject;
import de.xaniox.heavyspleef.core.floor.Floor;
import de.xaniox.heavyspleef.core.floor.FloorRegenerator;
import de.xaniox.heavyspleef.core.floor.FloorRegeneratorFactory;
import de.xaniox.heavyspleef.core.floor.RegenerationCause;
import de.xaniox.heavyspleef.core.game.Game;
import de.xaniox.heavyspleef.flag.presets.IntegerFlag;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

@Flag(name = "regen-percentage")
public class FlagRegenPercentage extends IntegerFlag {
	
	@Inject
	private Game game;
	private ComponentFloorRegeneratorFactory regeneratorFactory = new ComponentFloorRegeneratorFactory();
	private ComponentFloorRegenerator regenerator;
	
	public FlagRegenPercentage() {
		this.regenerator = new ComponentFloorRegenerator();
	}
	
	@Override
	public void onFlagAdd(Game game) {
		game.setFloorRegeneratorFactory(regeneratorFactory);
	}
	
	@Override
	public void onFlagRemove(Game game) {
		game.setFloorRegeneratorFactory(null);
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Regenerates only a certain percentage of blocks for each floor in the game");
	}

	private class ComponentFloorRegeneratorFactory extends FloorRegeneratorFactory {

		@Override
		public FloorRegenerator retrieveRegeneratorInstance() {
			return regenerator;
		}
	}
	
	private class ComponentFloorRegenerator implements FloorRegenerator {

		private static final int AIR_ID = 0;
		private Random random = new Random();
		
		@Override
		public void regenerate(Floor floor, EditSession session, RegenerationCause cause) {
			if (cause == RegenerationCause.COUNTDOWN) {
				return;
			}
			
			Region region = floor.getRegion();
			
			double percentage = getValue() / 100D;
			int area = region.getArea();
			
			int notRegenerating = area - (int) (percentage * area);
			
			Iterator<BlockVector> iterator = region.iterator();
			List<BlockVector> vectors = Lists.newArrayList();
			Map<BlockVector, BaseBlock> clipboardBlockCache = Maps.newHashMap();
			Clipboard clipboard = floor.getClipboard();
			
			while (iterator.hasNext()) {
				BlockVector vector = iterator.next();
				BaseBlock block = clipboard.getLazyBlock(vector);
				clipboardBlockCache.put(vector, block);
				
				if (block.getType() == AIR_ID) {
					continue;
				}
				
				vectors.add(vector);
			}
			
			for (int i = 0; i < notRegenerating; i++) {
				int rnd = random.nextInt(vectors.size());
				vectors.remove(rnd);
			}
			
			World world = region.getWorld();
			
			try {
				//Iterate over all remaining block vectors and regenerate these blocks
				for (BlockVector regeneratingBlock : vectors) {
					BaseBlock clipboardBlock = clipboardBlockCache.get(regeneratingBlock);
					BaseBlock worldBlock = world.getBlock(regeneratingBlock);
					
					int id = clipboardBlock.getId();
					int data = clipboardBlock.getData();
					
					worldBlock.setIdAndData(id, data);
					world.setBlock(regeneratingBlock, worldBlock);
				}
			} catch (WorldEditException e) {
				getHeavySpleef().getLogger().log(Level.SEVERE, "Failed to regenerate floor " + floor.getName() + " for game " + game.getName(), e);
			}
		}
		
	}

}