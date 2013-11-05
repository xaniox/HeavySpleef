package de.matzefratze123.heavyspleef.core;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CylinderRegion;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.Region;
import de.matzefratze123.heavyspleef.objects.RegionCylinder;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;
import de.matzefratze123.heavyspleef.util.Util;

public class GameCylinder extends Game {

	private final RegionCylinder region;
	
	public GameCylinder(String name, RegionCylinder region) {
		super(name);
		
		this.region = region;
	}

	@Override
	public GameType getType() {
		return GameType.CYLINDER;
	}

	@Override
	public boolean contains(Location location) {
		return region.contains(location);
	}

	@Override
	public Location getRandomLocation() {
		List<IFloor> floors = getComponents().getFloors();
		Collections.sort(floors);
		
		int y = floors.get(floors.size() - 1).getY() + 1;

	    double i = random.nextInt(360) + 1;
	    double r = random.nextInt(region.getWorldEditRegion().getRadius().getBlockX() - 1);

	    double angle = i * 3.141592653589793D / 180.0D;
	    int x = (int)(region.getWorldEditRegion().getCenter().getX() + r * Math.cos(angle));
	    int z = (int)(region.getWorldEditRegion().getCenter().getZ() + r * Math.sin(angle));

	    return new Location(BukkitUtil.toWorld(region.getWorldEditRegion().getWorld()), x, y, z);
	}

	@Override
	public void broadcast(String message, BroadcastType type) {
		switch(type) {
		case INGAME:
			for (SpleefPlayer player : getIngamePlayers()) {
				player.sendMessage(message);
			}
			break;
		case GLOBAL:
			Bukkit.broadcastMessage(message);
			break;
		case RADIUS:
			int radius = HeavySpleef.getSystemConfig().getInt("general.broadcast-radius", 40);
			int radiusSqared = radius * radius;
			
			
			
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getWorld() != BukkitUtil.toWorld(region.getWorldEditRegion().getWorld()))
					continue;
				double distanceSquared = Util.toBukkitLocation(region.getWorldEditRegion().getWorld(), region.getWorldEditRegion().getCenter()).distanceSquared(player.getLocation());
				
				if (this.hasPlayer(HeavySpleef.getInstance().getSpleefPlayer(player)) || distanceSquared <= radiusSqared)
					player.sendMessage(message);
			
			}
			
			break;
		}
	}

	@Override
	public Region getRegion() {
		return region;
	}
	
	@Override
	public ConfigurationSection serialize() {
		ConfigurationSection section = super.serialize();
		
		CylinderRegion weRegion = region.getWorldEditRegion();
		
		section.set("center", Parser.convertLocationtoString(Util.toBukkitLocation(weRegion.getWorld(), weRegion.getCenter())));
		section.set("radius", weRegion.getRadius().getBlockX());
		section.set("min", weRegion.getMinimumY());
		section.set("max", weRegion.getMaximumY());
		
		return section;
	}

}
