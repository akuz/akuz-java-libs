package me.akuz.ts.filters.interp;

import java.util.Date;
import java.util.List;

import me.akuz.core.DateUtils;
import me.akuz.ts.Filter;
import me.akuz.ts.SeqIterator;
import me.akuz.ts.TItem;
import me.akuz.ts.log.TLog;

/**
 * Linearly interpolates the value between sequence
 * items, by looking ahead at the next item, if needed.
 *
 */
public final class LinearInterpDateAhead extends Filter<Date> {
	
	private TItem<Date> _lastItem;
	private TItem<Date> _currItem;

	@Override
	public void next(
			final TLog log, 
			final Date currTime, 
			final SeqIterator<Date> iter) {
		
		final List<TItem<Date>> movedItems = iter.getMovedItems();
		if (movedItems.size() > 0) {
			_lastItem = movedItems.get(movedItems.size() - 1);
		}
		
		if (_lastItem.getTime().equals(currTime)) {
			_currItem = new TItem<>(currTime, _lastItem.getNumber().doubleValue());
			return;
		}
		
		if (_lastItem != null) {

			final List<TItem<Date>> items = iter.getSeq().getItems();
			final int nextCursor = iter.getNextCursor();

			if (nextCursor < items.size()) {
				
				final TItem<Date> nextItem = items.get(nextCursor);
				final double partMs = (double)DateUtils.msBetween(_lastItem.getTime(), currTime);
				final double totalMs = (double)DateUtils.msBetween(_lastItem.getTime(), nextItem.getTime());
				
				// interpolate between
				double currValue = 0.0;
				currValue += _lastItem.getNumber().doubleValue() / totalMs * (totalMs - partMs);
				currValue += nextItem.getNumber().doubleValue() / totalMs * partMs;
				_currItem = new TItem<>(currTime, currValue);
				
			} else {
				
				// propagate last value
				_currItem = new TItem<>(currTime, _lastItem.getNumber().doubleValue());
			}
			
		} else {
			
			// there was no items yet
			_currItem = null;
		}
	}

	@Override
	public TItem<Date> getCurrItem() {
		return _currItem;
	}

}
