package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Output time series.
 *
 * @param <T> - Time type.
 */
public final class TSOutput<T extends Comparable<T>> extends TS<T> {

	private final List<TSEntry<T>> _sorted;
	private final List<TSEntry<T>> _sortedReadOnly;
	private TSEntry<T> _lastEntry;
	
	public TSOutput() {
		_sorted = new ArrayList<>();
		_sortedReadOnly = Collections.unmodifiableList(_sorted);
	}

	public void add(TSEntry<T> entry) {
		int lastTimeCmp = -1;
		if (_lastEntry != null) {
			lastTimeCmp = _lastEntry.getTime().compareTo(entry.getTime());
		}
		if (lastTimeCmp > 0) {
			throw new IllegalStateException("Values must be added in chronological order");
		}
		if (lastTimeCmp == 0) {
			// remove last entry with the same time
			_sorted.remove(_sorted.size()-1);
		}
		_sorted.add(entry);
		_lastEntry = entry;
	}
	
	@Override
	public List<TSEntry<T>> getSorted() {
		return _sortedReadOnly;
	}

}
