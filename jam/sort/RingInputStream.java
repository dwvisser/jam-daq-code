package jam.sort;

/**
 * This class takes a buffer and creates from it a InputStream. The buffers are
 * not copied but referenced so you must make sure not to remove the buffer
 * while this class has a reference to it.
 * 
 * This class is not re-entrant (multi-thread ready) so that it can be fast,
 * synchronize locks take time.
 * 
 * Adapted from <code>java.io.ByteInputstream</code>. A difference is that
 * you don't construct a new class each time and methods are not synchronized.
 * 
 * @author Ken Swartz
 */
public final class RingInputStream extends java.io.InputStream {

	/**
	 * The buffer where data is stored.
	 */
	private byte buf[];

	/**
	 * The current position in the buffer.
	 */
	private int pos;

	/**
	 * The number of characters to use in the buffer.
	 */
	private int count;

	/**
	 * Load a buffer to read.
	 * 
	 * @param bufferIn
	 *            the input buffer (not copied)
	 */
	public void setBuffer(byte[] bufferIn) {
		buf = bufferIn;
		pos = 0;
		count = bufferIn.length;
	}

	/**
	 * Load a buffer to read but read only specified bytes
	 * 
	 * @param buf
	 *            The input buffer (not copied)
	 * @param offset
	 *            The offset of the first byte to read
	 * @param length
	 *            The number of bytes to read
	 */
	public void setBuffer(byte buf[], int offset, int length) {
		this.buf = buf;
		this.pos = offset;
		this.count = Math.min(offset + length, buf.length);
	}

	/**
	 * Reads a byte of data.
	 * 
	 * @return the byte read, or -1 if the end of the stream is reached.
	 */
	public int read() {
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}

	/**
	 * Reads into an array of bytes.
	 * 
	 * @param b
	 *            the buffer into which the data is read
	 * @param off
	 *            the start offset of the data
	 * @param len
	 *            the maximum number of bytes read
	 * @return the actual number of bytes read; -1 is returned when the end of
	 *         the stream is reached.
	 */
	public int read(byte b[], final int off, final int len) {
		int rval = 0; //by default, read nothing
		//check we are not passed the end of the buffer
		if (pos >= count) {
			rval = -1;
		} else {
			if (pos + len > count) {
				/*
				 * check we can read all the bytes asked otherwise read all we
				 * can
				 */
				rval = count - pos;
			}
		}
		if (rval > 0) {
			System.arraycopy(buf, pos, b, off, rval);
			pos += rval;
		}
		return rval;
	}

	/**
	 * Skips n bytes of input.
	 * 
	 * @param n
	 *            the number of bytes to be skipped
	 * @return the actual number of bytes skipped.
	 */
	public long skip(long n) {
		long rval = n;
		if (pos + rval > count) {
			rval = count - pos;
		}
		if (rval < 0) {
			return 0;
		}
		pos += rval;
		return rval;
	}

	/**
	 * Returns the number of available bytes in the buffer.
	 */
	public int available() {
		return count - pos;
	}

	/**
	 * Resets the buffer to the beginning.
	 */
	public void reset() {
		pos = 0;
	}
}