package mobac.utilities.collections;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A HashMap that uses internally SoftReferences for the value (if the app is
 * running out of memory the values stored in a {@link SoftReference} will be
 * automatically freed.
 * 
 * Therefore it may happen that this map "looses" values.
 * 
 * @param <K>
 * @param <V>
 */
public class SoftHashMap<K, V> implements Map<K, V> {

	HashMap<K, SoftReference<V>> map;

	public SoftHashMap(int initialCapacity) {
		map = new HashMap<K, SoftReference<V>>(initialCapacity);
	}

	public V get(Object key) {
		SoftReference<V> ref = map.get(key);
		return (ref != null) ? ref.get() : null;
	}

	public V put(K key, V value) {
		SoftReference<V> ref = map.put(key, new SoftReference<V>(value));
		return (ref != null) ? ref.get() : null;
	}

	public V remove(Object key) {
		SoftReference<V> ref = map.remove(key);
		return (ref != null) ? ref.get() : null;
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<K> keySet() {
		return map.keySet();
	}

	public int size() {
		return map.size();
	}

	public Set<Map.Entry<K, V>> entrySet() {
		throw new RuntimeException("Not implemented");
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		throw new RuntimeException("Not implemented");
	}

	public Collection<V> values() {
		throw new RuntimeException("Not implemented");
	}
}
