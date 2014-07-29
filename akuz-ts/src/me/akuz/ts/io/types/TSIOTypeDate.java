package me.akuz.ts.io.types;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


import com.google.gson.JsonObject;

public final class TSIOTypeDate extends TSIOType {

	private final String _format;
	private final TimeZone _timeZone;
	private final ThreadLocal<SimpleDateFormat> _threadLocal = new ThreadLocal<>();
	
	public TSIOTypeDate(String format, TimeZone timeZone) {
		_format = format;
		_timeZone = timeZone;
	}

	@Override
	public Object fromJsonField(JsonObject obj, String name) throws IOException {
		if (!obj.has(name)) {
			return null;
		}
		String str = obj.get(name).getAsString();
		SimpleDateFormat fmt = _threadLocal.get();
		if (fmt == null) {
			fmt = new SimpleDateFormat(_format);
			fmt.setTimeZone(_timeZone);
			_threadLocal.set(fmt);
		}
		try {
			return fmt.parse(str);
		} catch (ParseException e) {
			throw new IOException("Could not parse date '" + str + "'", e);
		}
	}

	@Override
	public void toJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		Date date = (Date)value;
		SimpleDateFormat fmt = _threadLocal.get();
		if (fmt == null) {
			fmt = new SimpleDateFormat(_format);
			fmt.setTimeZone(_timeZone);
			_threadLocal.set(fmt);
		}
		obj.addProperty(name, fmt.format(date));
	}
	
	@Override
	public String toString(Object value) {
		if (value == null) {
			return null;
		}
		Date date = (Date)value;
		SimpleDateFormat fmt = _threadLocal.get();
		if (fmt == null) {
			fmt = new SimpleDateFormat(_format);
			fmt.setTimeZone(_timeZone);
			_threadLocal.set(fmt);
		}
		return fmt.format(date);
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		SimpleDateFormat fmt = _threadLocal.get();
		if (fmt == null) {
			fmt = new SimpleDateFormat(_format);
			fmt.setTimeZone(_timeZone);
			_threadLocal.set(fmt);
		}
		try {
			return fmt.parse(str);
		} catch (ParseException e) {
			throw new IOException("Could not parse date '" + str + "'", e);
		}
	}
}
