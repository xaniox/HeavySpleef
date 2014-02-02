package de.matzefratze123.heavyspleef.core;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import de.matzefratze123.heavyspleef.database.DatabaseSerializeable;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;

public class SignWallNew extends RegionCuboid implements DatabaseSerializeable {

	public SignWallNew(int id, Location firstPoint, Location secondPoint) {
		super(id, firstPoint, secondPoint);
		// TODO Automatisch generierter Konstruktorstub
	}

	@Override
	public ConfigurationSection serialize() {
		// TODO Automatisch generierter Methodenstub
		return null;
	}

}
