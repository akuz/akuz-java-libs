package me.akuz.ts.io;

import java.io.IOException;

import me.akuz.ts.io.types.IOBoolean;
import me.akuz.ts.io.types.IOTDate;
import me.akuz.ts.io.types.IOTDateTime;
import me.akuz.ts.io.types.IODouble;
import me.akuz.ts.io.types.IOInteger;
import me.akuz.ts.io.types.IOString;

import com.google.gson.JsonObject;

/**
 * Time series IO data type.
 *
 */
public abstract class IOType {
	
	public static final IOType BooleanType    = new IOBoolean();
	public static final IOType TDateType      = new IOTDate();
	public static final IOType TDateTimeType  = new IOTDateTime();
	public static final IOType DoubleType     = new IODouble();
	public static final IOType IntegerType    = new IOInteger();
	public static final IOType StringType     = new IOString();
	
	public abstract Object fromJsonField(JsonObject obj, String name) throws IOException;
	public abstract void toJsonField(JsonObject obj, String name, Object value);
	
	public abstract Object fromString(String str) throws IOException;
	public abstract String toString(Object value);
	
}
