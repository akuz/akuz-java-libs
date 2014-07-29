package me.akuz.ts.io.types;


import java.io.IOException;

import com.google.gson.JsonObject;

public final class TSIOTypeString extends TSIOType {

	@Override
	public Object fromJsonField(JsonObject obj, String name) {
		if (!obj.has(name)) {
			return null;
		}
		return obj.get(name).getAsString();
	}

	@Override
	public void toJsonField(JsonObject obj, String name, Object value) {
		if (value == null) {
			return;
		}
		obj.addProperty(name, (String)value);
	}
	
	@Override
	public String toString(Object value) {
		if (value == null) {
			return null;
		}
		return (String)value;
	}
	
	@Override
	public Object fromString(String str) throws IOException {
		return str;
	}
}
