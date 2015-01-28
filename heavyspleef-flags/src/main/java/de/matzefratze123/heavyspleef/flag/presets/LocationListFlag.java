package de.matzefratze123.heavyspleef.flag.presets;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.dom4j.Element;

import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public abstract class LocationListFlag extends ListFlag<Location> {
	
	@Override
	public List<Location> parseInput(Player player, String input) throws InputParseException {
		throw new InputParseException("Multiple locations cannot be parsed");
	}
	
	@Override
	public void marshalListItem(Element element, Location item) {
		Element worldElement = element.addElement("world");
		Element xElement = element.addElement("x");
		Element yElement = element.addElement("y");
		Element zElement = element.addElement("z");
		
		worldElement.addText(item.getWorld().getName());
		xElement.addText(String.valueOf(item.getX()));
		yElement.addText(String.valueOf(item.getY()));
		zElement.addText(String.valueOf(item.getZ()));
	}

	@Override
	public Location unmarshalListItem(Element element) {
		Element worldElement = element.element("world");
		Element xElement = element.element("x");
		Element yElement = element.element("y");
		Element zElement = element.element("z");
		
		World world = Bukkit.getWorld(worldElement.getText());
		double x = Double.parseDouble(xElement.getText());
		double y = Double.parseDouble(yElement.getText());
		double z = Double.parseDouble(zElement.getText());
		
		Location location = new Location(world, x, y, z);
		return location;
	}

	@Override
	public ListInputParser<Location> createParser() {
		//Location lists cannot be parsed by a string
		return null;
	}

}
