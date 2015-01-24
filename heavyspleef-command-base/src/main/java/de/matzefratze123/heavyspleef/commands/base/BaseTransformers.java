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
package de.matzefratze123.heavyspleef.commands.base;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BaseTransformers {

	public static final Map<Class<?>, Transformer<?>> BASE_TRANSFORMERS;
	
	public static final Transformer<Integer> INTEGER_TRANSFORMER = new Transformer<Integer>() {

		@Override
		public Integer transform(String arg) throws TransformException {
			int result;
			
			try {
				result = Integer.parseInt(arg);
			} catch (NumberFormatException nfe) {
				throw new TransformException(nfe);
			}
			
			return result;
		}
	};
	
	public static final Transformer<Double> DOUBLE_TRANSFORMER = new Transformer<Double>() {

		@Override
		public Double transform(String arg) throws TransformException {
			double result;
			
			try {
				result = Double.parseDouble(arg);
			} catch (NumberFormatException nfe) {
				throw new TransformException(nfe);
			}
			
			return result;
		}
	};
	
	public static final Transformer<Boolean> BOOLEAN_TRANSFORMER = new Transformer<Boolean>() {
		
		private Set<String> falseKeywords = Sets.newHashSet("false", "no", "0");
		private Set<String> trueKeywords = Sets.newHashSet("true", "yes", "1");
		
		@Override
		public Boolean transform(String arg) throws TransformException {
			arg = arg.toLowerCase();
			
			if (falseKeywords.contains(arg)) {
				return false;
			} else if (trueKeywords.contains(arg)) {
				return true;
			} else throw new TransformException("argument " + arg + " not matching to any boolean expression");
		}
	};

	public static final Transformer<Player> PLAYER_TRANSFORMER = new Transformer<Player>() {
		
		@SuppressWarnings("deprecation")
		@Override
		public Player transform(String arg) throws TransformException {
			Player player = Bukkit.getPlayerExact(arg);
			
			return player;
		}
	};
	
	static {
		BASE_TRANSFORMERS = Maps.newHashMap();
		BASE_TRANSFORMERS.put(Integer.class, INTEGER_TRANSFORMER);
		BASE_TRANSFORMERS.put(Double.class, DOUBLE_TRANSFORMER);
		BASE_TRANSFORMERS.put(Boolean.class, BOOLEAN_TRANSFORMER);
		BASE_TRANSFORMERS.put(Player.class, PLAYER_TRANSFORMER);
	}
	
}
