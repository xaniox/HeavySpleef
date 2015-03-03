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
package de.matzefratze123.heavyspleef.core.collection;

import java.util.Map;
import java.util.Set;

public interface DualKeyMap<K1, K2, V> extends Map<DualKeyMap.DualKeyPair<K1, K2>, V> {
	
	public V put(K1 primaryKey, K2 secondaryKey, V value);
	
	public Set<K1> primaryKeySet();
	
	public Set<K2> secondaryKeySet();
	
	public static class DualKeyPair<K1, K2> {
		
		private K1 primaryKey;
		private K2 secondaryKey;
		
		public DualKeyPair(K1 primaryKey, K2 secondaryKey) {
			this.primaryKey = primaryKey;
			this.secondaryKey = secondaryKey;
		}
		
		public K1 getPrimaryKey() {
			return primaryKey;
		}
		
		public K2 getSecondaryKey() {
			return secondaryKey;
		}
		
	}
	
	public static class DualKeyEntry<K1, K2, V> implements Map.Entry<DualKeyPair<K1, K2>, V> {

		private DualKeyPair<K1, K2> keyPair;
		private V value;
		
		public DualKeyEntry(DualKeyPair<K1, K2> keyPair, V value) {
			this.keyPair = keyPair;
			this.value = value;
		}

		@Override
		public DualKeyPair<K1, K2> getKey() {
			return keyPair;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			throw new UnsupportedOperationException("Modifying key-value entries is not supported!");
		}
		
	}
	
}
