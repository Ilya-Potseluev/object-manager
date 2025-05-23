package objectmanager.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConcurrentHashMap<K, V> implements Map<K, V> {

    private final Map<K, V> map;
    private final Object mutex = new Object();

    public ConcurrentHashMap() {
        this.map = new HashMap<>();
    }

    @Override
    public V get(Object key) {
        synchronized (mutex) {
            return map.get(key);
        }
    }

    @Override
    public V put(K key, V value) {
        synchronized (mutex) {
            return map.put(key, value);
        }
    }

    @Override
    public V remove(Object key) {
        synchronized (mutex) {
            return map.remove(key);
        }
    }

    @Override
    public boolean containsKey(Object key) {
        synchronized (mutex) {
            return map.containsKey(key);
        }
    }

    @Override
    public boolean containsValue(Object value) {
        synchronized (mutex) {
            return map.containsValue(value);
        }
    }

    @Override
    public int size() {
        synchronized (mutex) {
            return map.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (mutex) {
            return map.isEmpty();
        }
    }

    @Override
    public void clear() {
        synchronized (mutex) {
            map.clear();
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        synchronized (mutex) {
            map.putAll(m);
        }
    }

    @Override
    public Set<K> keySet() {
        synchronized (mutex) {
            return new HashMap<>(map).keySet();
        }
    }

    @Override
    public Collection<V> values() {
        synchronized (mutex) {
            return new HashMap<>(map).values();
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        synchronized (mutex) {
            return new HashMap<>(map).entrySet();
        }
    }
}
