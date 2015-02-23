/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.util;

import java.lang.reflect.Field;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class FuzzyReflection {

	public static enum MCPackage {

		MINECRAFT_PACKAGE {
		@Override
		public String toString() {
			return "net.minecraft.server." + VERSION;
		}
		},

		CRAFTBUKKIT_PACKAGE {
		@Override
		public String toString() {
			return "org.bukkit.craftbukkit." + VERSION;
		}
		};
	}

	private static String	VERSION;

	static {
		if (VERSION == null) {
			Package[] knownPackages = Package.getPackages();

			for (Package pack : knownPackages) {
				if (pack.getName().startsWith("net.minecraft.server")) {
					String[] parts = pack.getName().split("\\.");
					if (parts.length < 4) {
						continue;
					}

					VERSION = parts[3];
					break;
				}
			}
		}
	}

	/**
	 * Plays a mobspellambiente effect at the given location
	 * 
	 * @param loc
	 *            The location to play the effect
	 * @param count
	 *            The count of "swirls"
	 * @param speed
	 *            Speed as a float
	 */
	public static void playMobSpellEffect(Location loc, int count, float speed) {
		World world = loc.getWorld();

		try {
			for (Entity entity : world.getEntities()) {
				if (!(entity instanceof Player)) {
					continue;
				}

				if (entity.getLocation().distanceSquared(loc) > 128 * 128) {
					continue;
				}

				Object entityPlayer = Class.forName(MCPackage.CRAFTBUKKIT_PACKAGE + ".entity.CraftPlayer").getMethod("getHandle").invoke(entity);
				Field field = entityPlayer.getClass().getDeclaredField("playerConnection");
				field.setAccessible(true);

				Object playerConnection = field.get(entityPlayer);
				Object packet = getPacket(loc, 0, 0, 0, speed, count);
				playerConnection.getClass().getMethod("sendPacket", Class.forName(MCPackage.MINECRAFT_PACKAGE + ".Packet")).invoke(playerConnection, packet);
			}
		} catch (Exception e) {
			// Do nothing here as we don't want to spam the console when
			// something in minecraft was changed
			// e.g. Mojang renamed methods or something similar
		}
	}

	private static Object getPacket(Location location, float offsetX, float offsetY, float offsetZ, float speed, int count) throws Exception {
		Object packet = Class.forName(MCPackage.MINECRAFT_PACKAGE + ".Packet63WorldParticles").getConstructor().newInstance();

		setValue(packet, "a", "mobSpellAmbient");
		setValue(packet, "b", (float) location.getX());
		setValue(packet, "c", (float) location.getY());
		setValue(packet, "d", (float) location.getZ());
		setValue(packet, "e", offsetX);
		setValue(packet, "f", offsetY);
		setValue(packet, "g", offsetZ);
		setValue(packet, "h", speed);
		setValue(packet, "i", count);
		return packet;
	}

	private static void setValue(Object obj, String fieldName, Object value) throws Exception {
		Field field = obj.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(obj, value);
	}

}
