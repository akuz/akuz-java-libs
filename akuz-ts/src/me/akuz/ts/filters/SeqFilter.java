package me.akuz.ts.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.akuz.ts.Seq;
import me.akuz.ts.SeqIter;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;
import me.akuz.ts.sync.Synchronizable;

/**
 * {@link Seq} filter.
 *
 */
public final class SeqFilter<T extends Comparable<T>> implements Synchronizable<T> {
	
	private String _fieldName;
	private final SeqIter<T> _seqIter;
	private final List<Filter<T>> _filters;
	private TItem<T> _currItem;
	private TLog _log;
	
	public SeqFilter(final Seq<T> seq) {
		_seqIter = new SeqIter<>(seq);
		_filters = new ArrayList<>();
	}
	
	public SeqFilter(final Seq<T> seq, final Filter<T> filter) {
		this(seq);
		addFilter(filter);
	}
	
	public SeqFilter(final Seq<T> seq, final Collection<Filter<T>> filters) {
		this(seq);
		addFilters(filters);
	}
	
	public String getFieldName() {
		return _fieldName != null ? _fieldName : "unspecified";
	}
	
	public void setFieldName(final String fieldName) {
		_fieldName = fieldName;
	}
	
	public void setLog(final TLog log) {
		_log = log;
	}
	
	public void addFilter(Filter<T> filter) {
		final Filter<T> filterCopy = filter.clone();
		if (_fieldName != null) {
			filterCopy.setFieldName(_fieldName);
		}
		_filters.add(filterCopy);
	}
	
	public void addFilters(Collection<Filter<T>> filters) {
		for (final Filter<T> filter : filters) {
			addFilter(filter);
		}
	}
	
	public TItem<T> getCurrItem() {
		return _currItem;
	}

	@Override
	public void moveToTime(T time) {
		
		if (_filters.size() == 0) {
			throw new IllegalStateException(
					"SeqFilter on field \"" + getFieldName() + 
					"\" does not have any 1D filters assigned");
		}
		
		_seqIter.moveToTime(time);
		
		TItem<T> newCurrItem = null;
		
		for (int i=0; i<_filters.size(); i++) {
			
			final Filter<T> filter = _filters.get(i);
			
			filter.next(
					_log,
					time,
					_seqIter.getCurrItem(),
					_seqIter.getMovedItems());
			
			final TItem<T> proposedItem = filter.getCurrItem();
			
			if (proposedItem != null) {
				if (newCurrItem != null) {
					throw new IllegalStateException(
							"Two 1D filters have proposed current item " +
							"for SeqFilter on field \"" + getFieldName() +
							"\", cannot choose between them");
				}
				newCurrItem = proposedItem;
			}
		}
		_currItem = newCurrItem;
	}

}