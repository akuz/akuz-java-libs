package me.akuz.ts.align;

import me.akuz.ts.TItem;

public class TSFillerDefaultWithLimitCount<T extends Comparable<T>> extends TSFiller<T> {
	
	private final String _logFieldName;
	private final int _maxMissingCount;
	private final Object _defaultValue;
	private int _currMissingCount;
	private TItem<T> _currItem;
	
	public TSFillerDefaultWithLimitCount(
			final String logFieldName,
			final int maxMissingCount,
			final Object defaultValue) {
		
		_logFieldName = logFieldName;
		_maxMissingCount = maxMissingCount;
		_defaultValue = defaultValue;
		_currItem = null;
	}

	@Override
	public TSAlignLogMsg next(T time, TItem<T> item) {
		String error = null;
		TSAlignLogMsg msg = null;
		_currItem = item;
		if (_currItem == null) {
			_currMissingCount++;
			if (_maxMissingCount <= 0 ||
				_currMissingCount <= _maxMissingCount) {
				_currItem = new TItem<T>(time, _defaultValue);
			} else {
				error = "could not fill, missing values count exceeded max of " + _maxMissingCount;
			}
		} else {
			_currMissingCount = 0;
		}
		if (_currItem == null) {
			msg = new TSAlignLogMsg(TSAlignLogMsgLevel.Error, _logFieldName + " value is missing (" + error + ")");
		}
		return msg;
	}

	@Override
	public TItem<T> getCurrent() {
		return _currItem;
	}

}
