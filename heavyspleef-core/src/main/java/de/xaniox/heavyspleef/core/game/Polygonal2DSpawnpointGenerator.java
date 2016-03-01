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
package de.xaniox.heavyspleef.core.game;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationAlgorithm;
import org.poly2tri.triangulation.TriangulationContext;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

import java.util.Iterator;
import java.util.List;

public class Polygonal2DSpawnpointGenerator implements SpawnpointGenerator<Polygonal2DRegion> {

	@Override
	public void generateSpawnpoints(Polygonal2DRegion region, World world, List<Location> spawnpoints, int n) {
		List<BlockVector2D> blockVectors = region.getPoints();
		List<PolygonPoint> points = Lists.newArrayList();
		
		int y = region.getMaximumY() + 1;
		
		for (int i = 0; i < blockVectors.size(); i++) {
			BlockVector2D vector = blockVectors.get(i);
			
			int x = vector.getBlockX();
			int z = vector.getBlockZ();
			
			PolygonPoint point = new PolygonPoint(x, z);
			points.add(point);
		}
		
		Polygon polygon = new Polygon(points);
		
		TriangulationContext<?> ctx = Poly2Tri.createContext(TriangulationAlgorithm.DTSweep);
		ctx.prepareTriangulation(polygon);
		
		Poly2Tri.triangulate(ctx);
		
		List<DelaunayTriangle> result = ctx.getTriangles();
		Iterator<DelaunayTriangle> iterator = result.iterator();
		
		while (iterator.hasNext()) {
			DelaunayTriangle next = iterator.next();
			if (!next.isInterior()) {
				iterator.remove();
			}
		}
		
		double totalArea = 0;
		for (DelaunayTriangle triangle : result) {
			totalArea += triangle.area();
		}
		
		for (int i = 0; i < n; i++) {
			double rand = Math.random() * totalArea;
			double current = 0;
			
			DelaunayTriangle triangle = null;
			
			for (int j = 0; j < result.size(); j++) {
				DelaunayTriangle tr = result.get(j);
				
				double oldCurrent = current;
				current += tr.area();
				
				if (oldCurrent < rand && current > rand) {
					triangle = tr;
					break;
				}
			}
			
			//Generate a random point in this triangle
			double r1 = Math.random();
			double r2 = Math.random();
			
			TriangulationPoint a = triangle.points[0];
			TriangulationPoint b = triangle.points[1];
			TriangulationPoint c = triangle.points[2];
			
			int x = (int) ((1 - Math.sqrt(r1)) * a.getX() + (Math.sqrt(r1) * (1 - r2)) * b.getX() + (Math.sqrt(r1) * r2) * c.getX());
			int z = (int) ((1 - Math.sqrt(r1)) * a.getY() + (Math.sqrt(r1) * (1 - r2)) * b.getY() + (Math.sqrt(r1) * r2) * c.getY());
			
			Location location = new Location(world, x + 0.5, y, z + 0.5);
			spawnpoints.add(location);
		}
	}

}