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

import com.google.common.collect.BiMap;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

import de.matzefratze123.heavyspleef.core.collection.DualKeyMap.DualKeyPair;

public class DualMaps {
	
	public static <K1, K2, V> DualKeyBiMap<K1, K2, V> immutableDualBiMap(DualKeyBiMap<K1, K2, V> map) {
		return new ForwardingImmutableDualKeyBiMap<K1, K2, V>(map);
	}
	
	private static final class ForwardingImmutableDualKeyBiMap<K1, K2, V> extends ForwardingMap<DualKeyPair<K1, K2>, V> implements DualKeyBiMap<K1, K2, V> {
		
		private DualKeyBiMap<K1, K2, V> delegate;
		private BiMap<V, DualKeyPair<K1, K2>> inverse;
		
		public ForwardingImmutableDualKeyBiMap(DualKeyBiMap<K1, K2, V> delegate) {
			this.delegate = delegate;
		}

		@Override
		public V put(K1 primaryKey, K2 secondaryKey, V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V forcePut(DualKeyPair<K1, K2> key, V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<V> values() {
			return delegate.values();
		}
		
		@Override
		public Set<K1> primaryKeySet() {
			return delegate.primaryKeySet();
		}
		
		@Override
		public Set<K2> secondaryKeySet() {
			return delegate.secondaryKeySet();
		}

		@Override
		public BiMap<V, DualKeyPair<K1, K2>> inverse() {
			return inverse == null ? (inverse = Maps.unmodifiableBiMap(delegate.inverse())) : inverse;
		}

		@Override
		public V forcePut(K1 primaryKey, K2 secondaryKey, V value) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected Map<DualKeyPair<K1, K2>, V> delegate() {
			return delegate;
		}
		
	}
	
}
