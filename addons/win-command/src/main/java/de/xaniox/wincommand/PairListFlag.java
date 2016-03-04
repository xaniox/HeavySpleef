/*
 * This file is part of addons.
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
package de.xaniox.wincommand;

import de.xaniox.heavyspleef.flag.presets.ListFlag;
import de.xaniox.heavyspleef.flag.presets.ListInputParser;
import org.dom4j.Element;

public abstract class PairListFlag<K, V> extends ListFlag<PairListFlag.Pair<K, V>> {

    @Override
    public void marshalListItem(Element element, Pair<K, V> item) {
        Element keyElement = element.addElement("key");
        Element valueElement = element.addElement("value");

        marshalKey(keyElement, item.getKey());
        marshalValue(valueElement, item.getValue());
    }

    @Override
    public Pair<K, V> unmarshalListItem(Element element) {
        Element keyElement = element.element("key");
        Element valueElement = element.element("value");

        K key = unmarshalKey(keyElement);
        V value = unmarshalValue(valueElement);

        return new Pair<K, V>(key, value);
    }

    @Override
    public String getListItemAsString(Pair<K, V> item) {
        return "[key: " + item.getKey() + ", value: " + item.getValue() + "]";
    }

    @Override
    public ListInputParser<Pair<K, V>> createParser() {
        //Let implementations handle that themselves
        throw new UnsupportedOperationException("Operation not supported");
    }

    public abstract void marshalKey(Element element, K key);

    public abstract K unmarshalKey(Element element);

    public abstract void marshalValue(Element element, V value);

    public abstract V unmarshalValue(Element element);

    public static class Pair<K, V> {

        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

}
