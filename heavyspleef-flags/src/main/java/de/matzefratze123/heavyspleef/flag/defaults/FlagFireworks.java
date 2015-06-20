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
package de.matzefratze123.heavyspleef.flag.defaults;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.core.event.PlayerWinGameEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.flag.Flag;
import de.matzefratze123.heavyspleef.core.flag.InputParseException;
import de.matzefratze123.heavyspleef.core.flag.NullFlag;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.flag.presets.LocationFlag;
import de.matzefratze123.heavyspleef.flag.presets.LocationListFlag;

@Flag(name = "fireworks", ignoreParseException = true)
public class FlagFireworks extends LocationListFlag {
	
	private static final int MAX_TRYS = 100;
	
	private final Random random = new Random();
	private final List<Type> typeValues = Collections.unmodifiableList(Arrays.asList(Type.values()));
	private final List<Color> colorValues = Collections.unmodifiableList(Arrays.asList(new Color[] {
			Color.AQUA, Color.BLACK, Color.BLUE, Color.FUCHSIA, Color.GRAY, Color.GREEN, Color.LIME,
			Color.MAROON, Color.NAVY, Color.OLIVE, Color.ORANGE, Color.PURPLE, Color.RED, Color.SILVER,
			Color.TEAL, Color.WHITE, Color.YELLOW
	}));
	
	public FlagFireworks() {
		List<Location> list = Lists.newArrayList();
		setValue(list);
	}
	
	@Override
	public List<Location> parseInput(SpleefPlayer player, String input) throws InputParseException {
		throw new InputParseException("Use fireworks:add to add a spawn location for fireworks and fireworks:remove to remove recent one");
	}
	
	@Override
	public void getDescription(List<String> description) {
		description.add("Defines multiple spawn locations for fireworks on win");
		description.add("About 3-5 fireworks will be spawned with an maximum radius of 4 relative to the spawn location");
	}
	
	@Subscribe
	public void onPlayerWinGame(PlayerWinGameEvent event) {
		for (Location location : getValue()) {
			int amount = random.nextInt(3) + 3;
			
			for (int i = 0; i < amount; i++) {
				Location spawn;
				
				int trys = 0;
				do {
					int x = random.nextInt(8) - 4;
					int y = random.nextInt(8) - 4;
					int z = random.nextInt(8) - 4;
					
					spawn = location.clone().add(x, y, z);
					Block block = spawn.getBlock();
					
					if (!block.isLiquid() && block.getType() != Material.AIR) {
						//Do another search
						spawn = null;
					}
				} while (spawn == null && ++trys < MAX_TRYS);
				
				if (spawn == null) {
					continue;
				}
				
				Firework firework = (Firework) spawn.getWorld().spawnEntity(spawn, EntityType.FIREWORK);
				FireworkMeta meta = firework.getFireworkMeta();
				
				Type type = typeValues.get(random.nextInt(typeValues.size()));
				Color c1 = colorValues.get(random.nextInt(colorValues.size()));
				Color c2 = colorValues.get(random.nextInt(colorValues.size()));

				FireworkEffect effect = FireworkEffect.builder()
						.flicker(random.nextBoolean())
						.withColor(c1)
						.withFade(c2)
						.with(type)
						.trail(random.nextBoolean())
						.build();

				meta.addEffect(effect);

				int rp = random.nextInt(3);
				meta.setPower(rp);

				firework.setFireworkMeta(meta);  
			}
		}
	}
	
	@Flag(name = "add", parent = FlagFireworks.class)
	public static class FlagAddFirework extends LocationFlag {
		
		@Override
		public void onFlagAdd(Game game) {
			FlagFireworks parent = (FlagFireworks) getParent();
			parent.add(getValue());
			
			game.removeFlag(getClass());
		}

		@Override
		public void getDescription(List<String> description) {
			description.add("Adds a firework location to the list of locations");
		}
		
	}
	
	@Flag(name = "remove", parent = FlagFireworks.class)
	public static class FlagRemoveFirework extends NullFlag {
		
		@Override
		public void onFlagAdd(Game game) {
			FlagFireworks parent = (FlagFireworks) getParent();
			int lastIndex = parent.size() - 1;
			
			parent.remove(lastIndex);
			game.removeFlag(getClass());
		}
		
		@Override
		public void getDescription(List<String> description) {
			description.add("Removes the recently added firework location");
		}
		
	}

}
