package me.akuz.ts.io;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import me.akuz.ts.TFrame;
import me.akuz.ts.TItem;
import me.akuz.ts.TSeq;
import me.akuz.ts.TType;
import me.akuz.ts.align.TSAlignIterator;

/**
 * CSV time series IO functions.
 *
 */
public final class CSV_TSIO {
	
	private static final String SEP = ",";
	private static final String SEP_ESCAPED = "\\,";
	private static final String SEP_REGEX = "(?<=(^|[^\\])),";
	private static final String SEP_REGEX_ESCAPED = "\\\\,";
	private static final String NEW_LINE = "\n";

	public static final <K, T extends Comparable<T>> String toCSV(
			final TSeq<T> seq, 
			final String timeFieldName,
			final TType timeDataType,
			final String valueFieldName, 
			final TType valueDataType) {
		
		TFrame<String, T> frame = new TFrame<>();
		frame.addSeq(valueFieldName, seq);
		
		return toCSV(
				frame,
				timeFieldName,
				timeDataType,
				valueDataType);
	}

	public static final <K, T extends Comparable<T>> String toCSV(
			final TFrame<K, T> frame, 
			final String timeFieldName,
			final TType timeDataType, 
			final TType valueDataType) {
		
		TSIOMap<K> tsioMap = new TSIOMap<>(frame, valueDataType);

		return toCSV(
				frame, 
				timeFieldName, 
				timeDataType, 
				tsioMap);
	}

	public static final <K, T extends Comparable<T>> String toCSV(
			final TFrame<K, T> frame,
			final String timeFieldName,
			final TType timeDataType, 
			final TSIOMap<K> tsioMap) {
		
		final StringBuilder sb = new StringBuilder();
		sb.append(timeFieldName);

		List<K> keys = tsioMap.getKeys();
		for (int j=0; j<keys.size(); j++) {
			
			K key = keys.get(j);
			String fieldName = tsioMap.getFieldName(key);

			sb.append(SEP);
			sb.append(fieldName.replaceAll(SEP, SEP_ESCAPED));
		}
		sb.append(NEW_LINE);
		
		final Set<T> timeSet = new HashSet<>();
		frame.extractTimes(timeSet);

		final TSAlignIterator<K, T> iterator = new TSAlignIterator<>(frame, timeSet, keys);
		while (iterator.hasNext()) {
			
			final Map<K, TItem<T>> currKeyItems = iterator.next();
			
			sb.append(timeDataType.toString(iterator.getCurrTime()));
			for (int j=0; j<keys.size(); j++) {
				
				sb.append(SEP);
				
				K key = keys.get(j);
				TItem<T> item = currKeyItems.get(key);
				
				if (item != null) {
					
					TType dataType = tsioMap.getDataType(key);
					String str = dataType.toString(item.getObject());
					
					if (str != null) {
						sb.append(str);
					}
				}
			}
			sb.append(NEW_LINE);
		}
		return sb.toString();
	}

	public static final <K, T extends Comparable<T>> TFrame<K,T> fromCSV(
			final String data,
			final String timeFieldName,
			final TType timeDataType, 
			final TSIOMap<K> tsioMap) throws IOException {
		
		TFrame<K, T> frame = new TFrame<>();

		int lineIndex = 0;
		try (Scanner sc = new Scanner(data)) {
			
			int timeFieldIdx = -1;
			Map<Integer, K> fieldIdxToKey = null;
			int fieldCount = -1;
			
			while (sc.hasNextLine()) {
				
				String line = sc.nextLine();
				
				if (lineIndex == 0) {
					
					// read the headers
					String[] parts = line.split(SEP_REGEX);
					fieldCount = parts.length;
					fieldIdxToKey = new HashMap<>();
					for (int i=0; i<parts.length; i++) {
						String fieldName = parts[i].trim().replaceAll(SEP_REGEX_ESCAPED, SEP);
						if (fieldName.equalsIgnoreCase(timeFieldName)) {
							timeFieldIdx = i;
						} else {
							K key = tsioMap.getKey(fieldName);
							if (key != null) {
								fieldIdxToKey.put(i, key);
							}
						}
					}
					if (timeFieldIdx < 0) {
						throw new IOException("Could not find time field '" + timeFieldName + "' in the headers");
					}
					
				} else {
					
					// read the data line
					String[] parts = line.split(SEP_REGEX);
					if (parts.length != fieldCount) {
						throw new IOException(
								"Number of fields (" + parts.length + ") at line index " + lineIndex + 
								" does not match the number of fields (" + fieldCount + ") in the headers");
					}
					
					final T time;
					try {
						@SuppressWarnings("unchecked")
						final T parsedTime = (T)timeDataType.fromString(parts[timeFieldIdx]);
						time = parsedTime;
					} catch (Exception ex) {
						throw new IOException("Could not parse time at line index " + lineIndex + ": '" + parts[timeFieldIdx] + "'");
					}
					
					for (Integer fieldIdx : fieldIdxToKey.keySet()) {
						
						final String part = parts[fieldIdx].trim();
						if (part.length() == 0) {
							continue;
						}
						K key = fieldIdxToKey.get(fieldIdx);
						TType valueDataType = tsioMap.getDataType(key);
						Object value = valueDataType.fromString(part);
						if (value != null) {
							frame.stage(key, time, value);
						}
					}
				}
				
				lineIndex++;
			}
		}
		
		frame.acceptStaged();
		return frame;
	}
}
