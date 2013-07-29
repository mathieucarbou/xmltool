/**
 * Copyright (C) 2008 Mathieu Carbou <mathieu.carbou@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mycila.xmltool;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class SoftHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final Object NULL_KEY = new Object();

    private Entry<K, V>[] table;
    private int size;
    private int threshold;
    private final float loadFactor;
    private final ReferenceQueue<K> queue = new ReferenceQueue<K>();
    private volatile int modCount;
    private transient Set<Map.Entry<K, V>> entrySet = null;
    private transient volatile Set<K> keySet = null;
    private transient volatile Collection<V> values = null;

    @SuppressWarnings({"unchecked"})
    public SoftHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal Initial Capacity: " + initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY) initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) throw new IllegalArgumentException("Illegal Load factor: " + loadFactor);
        int capacity = 1;
        while (capacity < initialCapacity) capacity <<= 1;
        table = new Entry[capacity];
        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
    }

    public SoftHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    @SuppressWarnings({"unchecked"})
    public SoftHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int) (DEFAULT_INITIAL_CAPACITY);
        table = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    public SoftHashMap(Map<? extends K, ? extends V> t) {
        this(Math.max((int) (t.size() / DEFAULT_LOAD_FACTOR) + 1, 16), DEFAULT_LOAD_FACTOR);
        putAll(t);
    }

    public int size() {
        if (size == 0) return 0;
        expungeStaleEntries();
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public V get(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        int index = indexFor(h, tab.length);
        Entry<K, V> e = tab[index];
        while (e != null) {
            if (e.hash == h && eq(k, e.get())) return e.value;
            e = e.next;
        }
        return null;
    }

    public V put(K key, V value) {
        K k = SoftHashMap.<K>maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);
        for (Entry<K, V> e = tab[i]; e != null; e = e.next) {
            if (h == e.hash && eq(k, e.get())) {
                V oldValue = e.value;
                if (value != oldValue) e.value = value;
                return oldValue;
            }
        }
        modCount++;
        Entry<K, V> e = tab[i];
        tab[i] = new Entry<K, V>(k, value, queue, h, e);
        if (++size >= threshold) resize(tab.length * 2);
        return null;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0) return;
        if (numKeysToBeAdded > threshold) {
            int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY) targetCapacity = MAXIMUM_CAPACITY;
            int newCapacity = table.length;
            while (newCapacity < targetCapacity) newCapacity <<= 1;
            if (newCapacity > table.length) resize(newCapacity);
        }
        for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
            Map.Entry<? extends K, ? extends V> e = i.next();
            put(e.getKey(), e.getValue());
        }
    }

    public V remove(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);
        Entry<K, V> prev = tab[i];
        Entry<K, V> e = prev;
        while (e != null) {
            Entry<K, V> next = e.next;
            if (h == e.hash && eq(k, e.get())) {
                modCount++;
                size--;
                if (prev == e) tab[i] = next;
                else prev.next = next;
                return e.value;
            }
            prev = e;
            e = next;
        }
        return null;
    }

    public void clear() {
        while (queue.poll() != null) ;
        modCount++;
        Entry[] tab = table;
        for (int i = 0; i < tab.length; ++i) tab[i] = null;
        size = 0;
        while (queue.poll() != null) ;
    }

    public boolean containsValue(Object value) {
        if (value == null) return containsNullValue();
        Entry[] tab = getTable();
        for (int i = tab.length; i-- > 0;)
            for (Entry e = tab[i]; e != null; e = e.next)
                if (value.equals(e.value))
                    return true;
        return false;
    }

    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = entrySet;
        return (es != null ? es : (entrySet = new EntrySet()));
    }

    private void expungeStaleEntries() {
        Entry<K, V> e;
        while ((e = (Entry<K, V>) queue.poll()) != null) {
            int h = e.hash;
            int i = indexFor(h, table.length);
            Entry<K, V> prev = table[i];
            Entry<K, V> p = prev;
            while (p != null) {
                Entry<K, V> next = p.next;
                if (p == e) {
                    if (prev == e) table[i] = next;
                    else prev.next = next;
                    e.next = null;  // Help GC
                    e.value = null; //  "   "
                    size--;
                    break;
                }
                prev = p;
                p = next;
            }
        }
    }

    private Entry<K, V>[] getTable() {
        expungeStaleEntries();
        return table;
    }

    private Entry<K, V> getEntry(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        int index = indexFor(h, tab.length);
        Entry<K, V> e = tab[index];
        while (e != null && !(e.hash == h && eq(k, e.get()))) e = e.next;
        return e;
    }

    @SuppressWarnings({"unchecked"})
    private void resize(int newCapacity) {
        Entry<K, V>[] oldTable = getTable();
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        Entry<K, V>[] newTable = new Entry[newCapacity];
        transfer(oldTable, newTable);
        table = newTable;
        if (size >= threshold / 2) {
            threshold = (int) (newCapacity * loadFactor);
        } else {
            expungeStaleEntries();
            transfer(newTable, oldTable);
            table = oldTable;
        }
    }

    private void transfer(Entry<K, V>[] src, Entry<K, V>[] dest) {
        for (int j = 0; j < src.length; ++j) {
            Entry<K, V> e = src[j];
            src[j] = null;
            while (e != null) {
                Entry<K, V> next = e.next;
                Object key = e.get();
                if (key == null) {
                    e.next = null;  // Help GC
                    e.value = null; //  "   "
                    size--;
                } else {
                    int i = indexFor(e.hash, dest.length);
                    e.next = dest[i];
                    dest[i] = e;
                }
                e = next;
            }
        }
    }

    private Entry<K, V> removeMapping(Object o) {
        if (!(o instanceof Map.Entry)) return null;
        Entry<K, V>[] tab = getTable();
        Map.Entry entry = (Map.Entry) o;
        Object k = maskNull(entry.getKey());
        int h = hash(k);
        int i = indexFor(h, tab.length);
        Entry<K, V> prev = tab[i];
        Entry<K, V> e = prev;
        while (e != null) {
            Entry<K, V> next = e.next;
            if (h == e.hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e) tab[i] = next;
                else prev.next = next;
                return e;
            }
            prev = e;
            e = next;
        }
        return null;
    }

    private boolean containsNullValue() {
        Entry[] tab = getTable();
        for (int i = tab.length; i-- > 0;)
            for (Entry e = tab[i]; e != null; e = e.next)
                if (e.value == null)
                    return true;
        return false;
    }

    @SuppressWarnings({"unchecked"})
    private static <K> K maskNull(Object key) {
        return (K) (key == null ? NULL_KEY : key);
    }

    @SuppressWarnings({"unchecked"})
    private static <K> K unmaskNull(Object key) {
        return (K) (key == NULL_KEY ? null : key);
    }

    private static boolean eq(Object x, Object y) {
        return x == y || x.equals(y);
    }

    private static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    private static int hash(Object key) {
        return hash(key.hashCode());
    }

    private static int hash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    private static class Entry<K, V> extends SoftReference<K> implements Map.Entry<K, V> {
        private V value;
        private final int hash;
        private Entry<K, V> next;

        Entry(K key, V value,
              ReferenceQueue<K> queue,
              int hash, Entry<K, V> next) {
            super(key, queue);
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        public K getKey() {
            return SoftHashMap.<K>unmaskNull(get());
        }

        public V getValue() {
            return value;
        }

        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) return true;
            }
            return false;
        }

        public int hashCode() {
            Object k = getKey();
            Object v = getValue();
            return ((k == null ? 0 : k.hashCode()) ^
                    (v == null ? 0 : v.hashCode()));
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    private abstract class HashIterator<T> implements Iterator<T> {
        int index;
        Entry<K, V> entry = null;
        Entry<K, V> lastReturned = null;
        int expectedModCount = modCount;
        Object nextKey = null;
        Object currentKey = null;

        HashIterator() {
            index = (size() != 0 ? table.length : 0);
        }

        public boolean hasNext() {
            Entry<K, V>[] t = table;
            while (nextKey == null) {
                Entry<K, V> e = entry;
                int i = index;
                while (e == null && i > 0) e = t[--i];
                entry = e;
                index = i;
                if (e == null) {
                    currentKey = null;
                    return false;
                }
                nextKey = e.get(); // hold on to key in strong ref
                if (nextKey == null) entry = entry.next;
            }
            return true;
        }

        protected Entry<K, V> nextEntry() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            if (nextKey == null && !hasNext()) throw new NoSuchElementException();
            lastReturned = entry;
            entry = entry.next;
            currentKey = nextKey;
            nextKey = null;
            return lastReturned;
        }

        public void remove() {
            if (lastReturned == null) throw new IllegalStateException();
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
            SoftHashMap.this.remove(currentKey);
            expectedModCount = modCount;
            lastReturned = null;
            currentKey = null;
        }
    }

    private class ValueIterator extends HashIterator<V> {
        public V next() {
            return nextEntry().value;
        }
    }

    private class KeyIterator extends HashIterator<K> {
        public K next() {
            return nextEntry().getKey();
        }
    }

    private class EntryIterator extends HashIterator<Map.Entry<K, V>> {
        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    private class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return SoftHashMap.this.size();
        }

        public boolean contains(Object o) {
            return containsKey(o);
        }

        public boolean remove(Object o) {
            if (containsKey(o)) {
                SoftHashMap.this.remove(o);
                return true;
            } else return false;
        }

        public void clear() {
            SoftHashMap.this.clear();
        }

        public Object[] toArray() {
            Collection<K> c = new ArrayList<K>(size());
            for (Iterator<K> i = iterator(); i.hasNext();) c.add(i.next());
            return c.toArray();
        }

        public <T> T[] toArray(T[] a) {
            Collection<K> c = new ArrayList<K>(size());
            for (Iterator<K> i = iterator(); i.hasNext();) c.add(i.next());
            return c.toArray(a);
        }
    }

    private class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return SoftHashMap.this.size();
        }

        public boolean contains(Object o) {
            return containsValue(o);
        }

        public void clear() {
            SoftHashMap.this.clear();
        }

        public Object[] toArray() {
            Collection<V> c = new ArrayList<V>(size());
            for (Iterator<V> i = iterator(); i.hasNext();) c.add(i.next());
            return c.toArray();
        }

        public <T> T[] toArray(T[] a) {
            Collection<V> c = new ArrayList<V>(size());
            for (Iterator<V> i = iterator(); i.hasNext();) c.add(i.next());
            return c.toArray(a);
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            Entry candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }

        public int size() {
            return SoftHashMap.this.size();
        }

        public void clear() {
            SoftHashMap.this.clear();
        }

        public Object[] toArray() {
            Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(size());
            for (Iterator<Map.Entry<K, V>> i = iterator(); i.hasNext();)
                c.add(new SimpleEntry<K, V>(i.next()));
            return c.toArray();
        }

        public <T> T[] toArray(T[] a) {
            Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(size());
            for (Iterator<Map.Entry<K, V>> i = iterator(); i.hasNext();)
                c.add(new SimpleEntry<K, V>(i.next()));
            return c.toArray(a);
        }
    }

    private static class SimpleEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(Map.Entry<K, V> e) {
            this.key = e.getKey();
            this.value = e.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry) o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
        }

        public int hashCode() {
            return ((key == null) ? 0 : key.hashCode()) ^
                    ((value == null) ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }

        private static boolean eq(Object o1, Object o2) {
            return (o1 == null ? o2 == null : o1.equals(o2));
        }
    }
}
