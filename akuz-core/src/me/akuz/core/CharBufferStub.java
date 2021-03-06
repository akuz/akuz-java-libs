package me.akuz.core;

/**
 * Circular character buffer.
 * @author andrey
 *
 */
public final class CharBufferStub {
	
	private final byte[] _tmp;
	private final byte[] _data;
	private int _cursor;
	
	public CharBufferStub(int size) {
		if (size < 3) {
			throw new IllegalArgumentException("Size must >= 3");
		}
		_tmp = new byte[3];
		_data = new byte[size];
		_cursor = -1;
	}
	
	public void add(char ch) {
		byte[] bytes = getBytes(ch);
		for (int i=bytes.length-1; i>=0; i--) {
			_cursor = (_cursor + 1) % _data.length;
			_data[_cursor] = bytes[i];
		}
	}
	
	public String get() {
		
		if (_cursor < 0) {
			return "";
		}
		
		int movingCursor = _cursor;
		final int startCursor = movingCursor;
		final StringBuffer sb = new StringBuffer();
		
		while (true) {
			
			final int len = ((_data[movingCursor] >> 6) & 3);
			
			if (len == 0) {
				break; // nothing more in buffer
			}

			boolean gotFullChar = true;
			for (int i=0; i<len; i++) {

				// copy byte for char creation
				_tmp[i] = _data[movingCursor];
				
				// move back
				movingCursor--;
				if (movingCursor < 0) {
					movingCursor = _data.length-1;
				}
				
				// if will need more bytes
				if (i < len-1) {
					
					// check if reached end
					if (movingCursor == startCursor) {
						gotFullChar = false;
					}
				}
			}
			
			if (gotFullChar) {
				
				// convert to char from bytes
				char ch = getChar(_tmp, len);
				sb.append(ch);
			}
			
			// check if reached end
			if (movingCursor == startCursor) {
				break;
			}
		}
		
		// reverse and to string
		return sb.reverse().toString();
	}
	
	/**
	 * Return bytes array from char.
	 * @param ch - character
	 * @return
	 */
	private static final byte[] getBytes(char ch) {
		return null; // TODO
	}
	
	/**
	 * Return char from bytes, given the length (bytes array can be longer than length).
	 * @param bs - bytes array
	 * @param len - number of bytes to convert to char
	 * @return
	 */
	private static final char getChar(byte[] bs, int len) {
		return 0; // TODO
	}

}
