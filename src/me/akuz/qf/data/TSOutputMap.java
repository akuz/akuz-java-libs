package me.akuz.qf.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Output time series map.
 *
 * @param <K> - Keys type.
 * @param <T> - Time type.
 */
public class TSOutputMap<K, T extends Comparable<T>> {

	private final boolean _allowDuplicateTimes;
	private final Map<K, TSOutput<T>> _map;
	private final Map<K, TSOutput<T>> _mapReadOnly;
	
	public TSOutputMap() {
		this(false);
	}
	
	public TSOutputMap(boolean allowDuplicateTimes) {
		_map = new HashMap<>();
		_mapReadOnly = Collections.unmodifiableMap(_map);
		_allowDuplicateTimes = allowDuplicateTimes;
	}
	
	public void add(K key, T time, Object value) {
		add(key, new TSEntry<T>(time, value));
	}
	
	public void add(K key, TSEntry<T> entry) {
		TSOutput<T> ts = _map.get(key);
		if (ts == null) {
			ts = new TSOutput<>(_allowDuplicateTimes);
			_map.put(key, ts);
		}
		ts.add(entry);
	}
	
	public Map<K, TSOutput<T>> getMap() {
		return _mapReadOnly;
	}
}
