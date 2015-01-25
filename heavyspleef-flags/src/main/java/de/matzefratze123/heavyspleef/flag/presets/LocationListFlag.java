package de.matzefratze123.heavyspleef.flag.presets;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.dom4j.Element;

public abstract class LocationListFlag extends ListFlag<Location> {

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

}
