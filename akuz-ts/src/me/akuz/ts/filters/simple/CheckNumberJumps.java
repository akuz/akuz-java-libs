package me.akuz.ts.filters.simple;

import java.util.List;

import me.akuz.ts.TItem;
import me.akuz.ts.filters.TFilter;
import me.akuz.ts.log.TLog;
import me.akuz.ts.log.TLogLevel;

public class CheckNumberJumps<T extends Comparable<T>> extends TFilter<T> {
	
	private String _fieldName;
	private final double _infoJump;
	private final double _warningJump;
	private final double _errorJump;
	private TItem<T> _lastItem;
	
	public CheckNumberJumps(
			final double infoJump,
			final double warningJump,
			final double errorJump) {
		
		if (infoJump <= 0.0) {
			throw new IllegalArgumentException("Argument infoJump must be positive");
		}
		if (warningJump <= 0.0) {
			throw new IllegalArgumentException("Argument warningJump must be positive");
		}
		if (errorJump <= 0.0) {
			throw new IllegalArgumentException("Argument errorJump must be positive");
		}
		
		_fieldName = "unspecified";
		_infoJump = infoJump;
		_warningJump = warningJump;
		_errorJump = errorJump;
		_lastItem = null;
	}
	
	@Override
	public void setFieldName(final String fieldName) {
		_fieldName = fieldName;
	}
	
	@Override
	public TItem<T> getCurrent() {
		// we are only checking for
		// jumps, but we don't
		// derive any state
		return null;
	}

	@Override
	public void next(
			TLog log,
			T currTime, 
			TItem<T> currItem,
			List<TItem<T>> movedItems) {

		if (log == null) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " filter requires a log");
		}
		
		for (int i=0; i<movedItems.size(); i++) {
			
			final TItem<T> prevItem = _lastItem;
			_lastItem = movedItems.get(i);
			
			if (prevItem != null) {
				checkNumberJump(log, prevItem, _lastItem);
			}
		}
	}
	
	private final void checkNumberJump(
			final TLog log,
			final TItem<T> prevItem,
			final TItem<T> currItem) {
		
		double prevNumber = prevItem.getNumber().doubleValue();
		double currNumber = _lastItem.getNumber().doubleValue();
		
		final double distance = Math.abs(currNumber - prevNumber);
		if (distance > Double.MIN_NORMAL) {
			
			final double absPrevNumber = Math.abs(prevNumber);
			final double absCurrNumber = Math.abs(currNumber);
			
			final double norm = Math.min(absPrevNumber, absCurrNumber);
			final double jump = distance / norm;

			if (jump > _errorJump) {
				log.add(TLogLevel.Error,   "Jump in \"" + _fieldName + "\" value: " + prevItem + " >> " + _lastItem);
			} else if (jump > _warningJump) {
				log.add(TLogLevel.Warning, "Jump in \"" + _fieldName + "\" value: " + prevItem + " >> " + _lastItem);
			} else if (jump > _infoJump) {
				log.add(TLogLevel.Info, "Jump in \"" + _fieldName + "\" value: " + prevItem + " >> " + _lastItem);
			}
		}
	}

}
