package me.akuz.ts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.akuz.core.Out;
import me.akuz.ts.log.TLog;
import me.akuz.ts.sync.Synchronizable;

/**
 * {@link Seq} filter, which applies one or more 
 * 1D filters to the underlying sequence and allows
 * to obtain current filtered item for the sequence.
 * 
 * Only one of the filters is allowed to generate the 
 * current item, otherwise an exception will occur.
 *
 */
public final class SeqFilter<T extends Comparable<T>> 
implements Synchronizable<T>, SeqCursor<T> {
	
	private String _fieldName;
	private final SeqCursor<T> _seqCursor;
	private final List<Filter<T>> _filters;
	private List<TItem<T>> _movedItems;
	private TItem<T> _currItem;
	private T _currTime;
	private TLog<T> _log;
	
	public SeqFilter(final SeqCursor<T> seqCursor) {
		if (seqCursor == null) {
			throw new IllegalArgumentException("Cannot filter null sequence cursor");
		}
		_seqCursor = seqCursor;
		_filters = new ArrayList<>();
	}
	
	public SeqFilter(final SeqCursor<T> seqCursor, final Filter<T> filter) {
		this(seqCursor);
		addFilter(filter);
	}
	
	public SeqFilter(final SeqCursor<T> seqCursor, final Collection<Filter<T>> filters) {
		this(seqCursor);
		addFilters(filters);
	}
	
	public String getFieldName() {
		return _fieldName != null ? _fieldName : "unspecified";
	}
	
	public void setFieldName(final String fieldName) {
		_fieldName = fieldName;
	}
	
	public void setLog(final TLog<T> log) {
		_log = log;
	}
	
	public void addFilter(final Filter<T> filter) {
		final Filter<T> filterCopy = filter.clone();
		if (_fieldName != null) {
			filterCopy.setFieldName(_fieldName);
		}
		_filters.add(filterCopy);
	}
	
	public void addFilters(final Collection<Filter<T>> filters) {
		for (final Filter<T> filter : filters) {
			addFilter(filter);
		}
	}
	
	@Override
	public Seq<T> getSeq() {
		return _seqCursor.getSeq();
	}
	
	@Override
	public int getNextCursor() {
		return _seqCursor.getNextCursor();
	}
	
	@Override
	public T getCurrTime() {
		CurrTime.checkSet(_currTime);
		return _currTime;
	}
	
	@Override
	public TItem<T> getCurrItem() {
		CurrTime.checkSet(_currTime);
		return _currItem;
	}
	
	@Override
	public List<TItem<T>> getMovedItems() {
		CurrTime.checkSet(_currTime);
		return _movedItems;
	}
	
	@Override
	public boolean getNextTime(final Out<T> nextTime) {
		return _seqCursor.getNextTime(nextTime);
	}

	@Override
	public void moveToTime(final T time) {
		
		if (_filters.size() == 0) {
			throw new IllegalStateException(
					"SeqFilter on field \"" + getFieldName() + 
					"\" does not have any 1D filters assigned");
		}
		
		CurrTime.checkNew(_currTime, time);
		
		_seqCursor.moveToTime(time);
		
		_currItem = null;
		_movedItems = null;
		for (int i=0; i<_filters.size(); i++) {
			
			final Filter<T> filter = _filters.get(i);
			
			filter.next(
					time,
					_seqCursor,
					_log);
			
			final TItem<T> proposedCurrItem = filter.getCurrItem();
			
			if (proposedCurrItem != null) {
				if (_currItem != null) {
					throw new IllegalStateException(
							"Two 1D filters have proposed current item " +
							"for SeqFilter on field \"" + getFieldName() +
							"\", cannot choose between them");
				}
				_currItem = proposedCurrItem;
			}
			
			final List<TItem<T>> proposedMovedItems = filter.getMovedItems();
			if (proposedMovedItems != null && proposedMovedItems.size() > 0) {
				
				if (_movedItems != null) {
					throw new IllegalStateException(
							"Two 1D filters have proposed moved items " +
							"for SeqFilter on field \"" + getFieldName() +
							"\", cannot choose between them");
				}
				_movedItems = proposedMovedItems;
			}
		}
		_currTime = time;
	}

}
