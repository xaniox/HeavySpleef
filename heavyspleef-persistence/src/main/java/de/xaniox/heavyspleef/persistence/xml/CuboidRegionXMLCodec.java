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
package de.xaniox.heavyspleef.persistence.xml;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.dom4j.Element;

public class CuboidRegionXMLCodec implements XMLRegionMetadataCodec<CuboidRegion> {

	@Override
	public void apply(Element applyTo, CuboidRegion region) {
		Vector pos1 = region.getPos1();
		Vector pos2 = region.getPos2();
		
		Element pos1Element = applyTo.addElement("pos1");
		Element x1Element = pos1Element.addElement("x");
		Element y1Element = pos1Element.addElement("y");
		Element zElement = pos1Element.addElement("z");
		
		Element pos2Element = applyTo.addElement("pos2");
		Element x2Element = pos2Element.addElement("x");
		Element y2Element = pos2Element.addElement("y");
		Element z2Element = pos2Element.addElement("z");
		
		x1Element.addText(String.valueOf(pos1.getBlockX()));
		y1Element.addText(String.valueOf(pos1.getBlockY()));
		zElement.addText(String.valueOf(pos1.getBlockZ()));
		
		x2Element.addText(String.valueOf(pos2.getBlockX()));
		y2Element.addText(String.valueOf(pos2.getBlockY()));
		z2Element.addText(String.valueOf(pos2.getBlockZ()));
	}

	@Override
	public CuboidRegion asRegion(Element container) {
		Element pos1Element = container.element("pos1");
		Element pos2Element = container.element("pos2");
		
		int x1 = Integer.parseInt(pos1Element.elementText("x"));
		int y1 = Integer.parseInt(pos1Element.elementText("y"));
		int z1 = Integer.parseInt(pos1Element.elementText("z"));
		
		int x2 = Integer.parseInt(pos2Element.elementText("x"));
		int y2 = Integer.parseInt(pos2Element.elementText("y"));
		int z2 = Integer.parseInt(pos2Element.elementText("z"));
		
		Vector pos1 = new Vector(x1, y1, z1);
		Vector pos2 = new Vector(x2, y2, z2);
		
		return new CuboidRegion(pos1, pos2);
	}

}