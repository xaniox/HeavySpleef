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
package de.matzefratze123.heavyspleef.persistence.xml;

import org.dom4j.Element;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CylinderRegion;

public class CylinderRegionXMLCodec implements XMLRegionMetadataCodec<CylinderRegion> {

	@Override
	public void apply(Element applyTo, CylinderRegion region) {
		Vector center = region.getCenter();
		Vector2D radius = region.getRadius();
		int minY = region.getMinimumY();
		int maxY = region.getMaximumY();
		
		Element centerElement = applyTo.addElement("center");
		centerElement.addElement("x").addText(String.valueOf(center.getBlockX()));
		centerElement.addElement("y").addText(String.valueOf(center.getBlockY()));
		centerElement.addElement("z").addText(String.valueOf(center.getBlockZ()));
		
		Element radiusElement = applyTo.addElement("radius");
		radiusElement.addElement("x").addText(String.valueOf(radius.getBlockX()));
		radiusElement.addElement("z").addText(String.valueOf(radius.getBlockZ()));
		
		applyTo.addElement("minY").addText(String.valueOf(minY));
		applyTo.addElement("maxY").addText(String.valueOf(maxY));
	}

	@Override
	public CylinderRegion asRegion(Element container) {
		Element centerElement = container.element("center");
		Element radiusElement = container.element("radius");
		Element minYElement = container.element("minY");
		Element maxYElement = container.element("maxY");
		
		int cx = Integer.parseInt(centerElement.elementText("x"));
		int cy = Integer.parseInt(centerElement.elementText("y"));
		int cz = Integer.parseInt(centerElement.elementText("z"));
		
		int rx = Integer.parseInt(radiusElement.elementText("x"));
		int rz = Integer.parseInt(radiusElement.elementText("z"));
		
		int minY = Integer.parseInt(minYElement.getText());
		int maxY = Integer.parseInt(maxYElement.getText());
		
		Vector center = new Vector(cx, cy, cz);
		Vector2D radius = new Vector2D(rx, rz);
		
		return new CylinderRegion(center, radius, minY, maxY);
	}

}
