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

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

public class DualKeyHashBiMap<K1, K2, V> implements DualKeyBiMap<K1, K2, V> {
	
	private BiMap<K1, V> primaryDelegate;
	private BiMap<K2, V> secondaryDelegate;
	
	private Class<K1> primaryKeyClass;
	private Class<K2> secondaryKeyClass;
	
	private Inverse inverse;
	private Values values;
	
	public DualKeyHashBiMap(Class<K1> primaryKeyClass, Class<K2> secondaryKeyClass) {
		this.primaryKeyClass = primaryKeyClass;
		this.secondaryKeyClass = secondaryKeyClass;
		
		this.primaryDelegate = HashBiMap.create();
		this.secondaryDelegate = HashBiMap.create();
	}
	
	@Override
	public Class<K1> getPrimaryKeyClass() {
		return primaryKeyClass;
	}
	
	@Override
	public Class<K2> getSecondaryKeyClass() {
		return secondaryKeyClass;
	}
	
	@Override
	public int size() {
		return primaryDelegate.size();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return primaryDelegate.containsKey(key) || secondaryDelegate.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return primaryDelegate.containsValue(value) || secondaryDelegate.containsValue(value);
	}
	
	@Override
	public boolean isEmpty() {
		return primaryDelegate.isEmpty();
	}

	@Override
	public V get(Object key) {
		validateKeyType(key);
		
		V value = null;
		
		if (primaryKeyClass.isInstance(key)) {
			value = primaryDelegate.get(key);
		} else if (secondaryKeyClass.isInstance(key)) {
			value = secondaryDelegate.get(key);
		} else if (key instanceof DualKeyPair) {
			DualKeyPair<?, ?> pair = (DualKeyPair<?, ?>) key;
			
			value = pair.getPrimaryKey() != null ? primaryDelegate.get(pair.getPrimaryKey()) : secondaryDelegate.get(pair.getSecondaryKey());
		}
		
		return value;
	}

	@Override
	public V remove(Object key) {
		validateKeyType(key);
		
		V value = null;
		
		if (primaryKeyClass.isInstance(key)) {
			value = primaryDelegate.remove(key);
			secondaryDelegate.inverse().remove(value);
		} else if (secondaryKeyClass.isInstance(key)) {
			value = secondaryDelegate.remove(key);
			primaryDelegate.inverse().remove(value);
		} else throw new IllegalArgumentException();
		
		return value;
	}

	@Override
	public void clear() {
		primaryDelegate.clear();
		secondaryDelegate.clear();
	}

	@Override
	public Set<DualKeyPair<K1, K2>> keySet() {
		BiMap<V, K1> primaryInverse = primaryDelegate.inverse();
		BiMap<V, K2> secondaryInverse = secondaryDelegate.inverse();
		
		Set<DualKeyPair<K1, K2>> keySet = Sets.newHashSet();
		for (V value : primaryInverse.keySet()) {
			K1 primaryKey = primaryInverse.get(value);
			K2 secondaryKey = secondaryInverse.get(value);
			
			DualKeyPair<K1, K2> pair = new DualKeyPair<K1, K2>(primaryKey, secondaryKey);
			keySet.add(pair);
		}
		
		return keySet;
	}
	
	@Override
	public Set<K1> primaryKeySet() {
		return primaryDelegate.keySet();
	}
	
	@Override
	public Set<K2> secondaryKeySet() {
		return secondaryDelegate.keySet();
	}

	@Override
	public Set<Entry<DualKeyPair<K1, K2>, V>> entrySet() {
		Set<Entry<DualKeyPair<K1, K2>, V>> entries = Sets.newHashSet();
		
		for (V value : primaryDelegate.values()) {
			K1 primaryKey = primaryDelegate.inverse().get(value);
			K2 secondaryKey = secondaryDelegate.inverse().get(value);
			
			entries.add(new DualKeyEntry<K1, K2, V>(new DualKeyPair<K1, K2>(primaryKey, secondaryKey), value));
		}
		
		return entries;
	}

	@Override
	public V put(DualKeyPair<K1, K2> keyPair, V value) {
		return put(keyPair, value, false);
	}
	
	@Override
	public V put(K1 primaryKey, K2 secondaryKey, V value) {
		return put(new DualKeyPair<K1, K2>(primaryKey, secondaryKey), value);
	}

	@Override
	public V forcePut(DualKeyPair<K1, K2> keyPair, V value) {
		return put(keyPair, value, true);
	}
	
	@Override
	public V forcePut(K1 primaryKey, K2 secondaryKey, V value) {
		return put(new DualKeyPair<K1, K2>(primaryKey, secondaryKey), value, true);
	}
	
	private V put(DualKeyPair<K1, K2> keyPair, V value, boolean force) {
		K1 primaryKey = keyPair.getPrimaryKey();
		K2 secondaryKey = keyPair.getSecondaryKey();
		int valueHash = value.hashCode();
		
		if (primaryDelegate.containsKey(primaryKey) && secondaryDelegate.containsKey(secondaryKey)
				&& primaryDelegate.get(primaryKey).hashCode() == valueHash) {
			// Return the already present entry
			return value;
		}
		
		V previousValue = null;
		if (primaryDelegate.containsKey(primaryKey) || secondaryDelegate.containsKey(secondaryKey) 
				|| primaryDelegate.containsValue(value) || secondaryDelegate.containsValue(value)) {
			if (force) {
				previousValue = remove(primaryKey);
			} else {
				throw new IllegalStateException("value already present: " );
			}
		}
		
		primaryDelegate.put(primaryKey, value);
		secondaryDelegate.put(secondaryKey, value);
		return previousValue;
	}

	@Override
	public void putAll(Map<? extends DualKeyPair<K1, K2>, ? extends V> map) {
		for (Entry<? extends DualKeyPair<K1, K2>, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Set<V> values() {
		return (values == null ? values = new Values() : values);
	}

	@Override
	public BiMap<V, DualKeyPair<K1, K2>> inverse() {
		return (inverse == null ? inverse = new Inverse() : inverse);
	}
	
	private void validateKeyType(Object key) {
		if (!primaryKeyClass.isInstance(key) && !secondaryKeyClass.isInstance(key)) {
			throw new IllegalArgumentException("key is not an instance of " + primaryKeyClass.getName() + " nor of " + secondaryKeyClass.getName());
		}
	}
	
	private final class Values extends AbstractCollection<V> implements Set<V> {
		
		@Override
		public Iterator<V> iterator() {
			return new ForwardingValueIterator(primaryDelegate.values()
					.iterator(), secondaryDelegate.values().iterator());
		}

		@Override
		public int size() {
			return primaryDelegate.size();
		}
		
		private final class ForwardingValueIterator implements Iterator<V> {

			private final Iterator<V> delegate1;
			private final Iterator<V> delegate2;
			
			public ForwardingValueIterator(Iterator<V> delegate1, Iterator<V> delegate2) {
				this.delegate1 = delegate1;
				this.delegate2 = delegate2;
			}

			@Override
			public boolean hasNext() {
				return delegate1.hasNext();
			}

			@Override
			public V next() {
				delegate2.next();
				return delegate1.next();
			}

			@Override
			public void remove() {
				delegate1.remove();
				delegate2.remove();
			}
			
		}
		
	}
	
	private final class Inverse extends AbstractMap<V, DualKeyPair<K1, K2>> implements BiMap<V, DualKeyPair<K1, K2>> {

		DualKeyHashBiMap<K1, K2, V> forward() {
			return DualKeyHashBiMap.this;
		}
		
		@Override
		public int size() {
			return forward().size();
		}
		
		@Override
		public void clear() {
			forward().clear();
		}
		
		@Override
		public boolean containsKey(Object key) {
			return forward().containsValue(key);
		}
		
		@Override
		public DualKeyPair<K1, K2> get(Object key) {
			BiMap<V, K1> primaryInverse = primaryDelegate.inverse();
			BiMap<V, K2> secondaryInverse = secondaryDelegate.inverse();
			
			K1 primaryKey = primaryInverse.get(key);
			K2 secondaryKey = secondaryInverse.get(key);
			
			if (primaryKey == null || secondaryKey == null) {
				return null;
			}
			
			return new DualKeyPair<K1, K2>(primaryKey, secondaryKey);
		}
		
		@Override
		public DualKeyPair<K1, K2> put(V key, DualKeyPair<K1, K2> value) {
			DualKeyPair<K1, K2> previousValue = get(key);
			forward().put(value, key, false);
			
			return previousValue;
		}
		
		@Override
		public DualKeyPair<K1, K2> forcePut(V key, DualKeyPair<K1, K2> value) {
			forward().put(value, key, true);
			return value;
		}
		
		@Override
		public DualKeyPair<K1, K2> remove(Object key) {
			K1 primaryKey = primaryDelegate.inverse().remove(key);
			K2 secondaryKey = secondaryDelegate.inverse().remove(key);
			
			return primaryKey != null && secondaryKey != null ? new DualKeyPair<K1, K2>(primaryKey, secondaryKey) : null;
		}
		
		@Override
		public Set<V> keySet() {
			return forward().values();
		}

		@Override
		public Set<DualKeyPair<K1, K2>> values() {
			return forward().keySet();
		}

		@Override
		public BiMap<DualKeyPair<K1, K2>, V> inverse() {
			return forward();
		}

		@Override
		public Set<Entry<V, DualKeyPair<K1, K2>>> entrySet() {
			Set<Entry<V, DualKeyPair<K1, K2>>> entries = Sets.newHashSet();
			
			for (final V value : primaryDelegate.values()) {
				K1 primaryKey = primaryDelegate.inverse().get(value);
				K2 secondaryKey = secondaryDelegate.inverse().get(value);
				
				final DualKeyPair<K1, K2> keyPair = new DualKeyPair<K1, K2>(primaryKey, secondaryKey);
				
				entries.add(new Map.Entry<V, DualKeyPair<K1,K2>>() {
					
					@Override
					public V getKey() {
						return value;
					}

					@Override
					public DualKeyPair<K1, K2> getValue() {
						return keyPair;
					}

					@Override
					public DualKeyPair<K1, K2> setValue(DualKeyPair<K1, K2> value) {
						throw new UnsupportedOperationException("Modifying value's in entries is not supported");
					}
				});
			}
			
			return entries;
		}
		
	}
	
}
