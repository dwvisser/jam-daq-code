package jam.sort;

import java.io.InputStream;

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
final class RingInputStream extends InputStream {

	/**
	 * The buffer where data is stored.
	 */
	private transient byte buf[];

	/**
	 * The current position in the buffer.
	 */
	private transient int pos;

	/**
	 * The number of characters to use in the buffer.
	 */
	private transient int count;

	RingInputStream() {
		super();
	}

	/**
	 * Load a buffer to read.
	 * 
	 * @param bufferIn
	 *            the input buffer (not copied)
	 */
	public void setBuffer(final byte[] bufferIn) {
		buf = bufferIn.clone();
		pos = 0;
		count = bufferIn.length;
	}

	/**
	 * Load a buffer to read but read only specified bytes
	 * 
	 * @param bufferIn
	 *            The input buffer (not copied)
	 * @param offset
	 *            The offset of the first byte to read
	 * @param length
	 *            The number of bytes to read
	 */
	public void setBuffer(final byte[] bufferIn, final int offset,
			final int length) {
		buf = bufferIn.clone();
		pos = offset;
		count = Math.min(offset + length, bufferIn.length);
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
	 * @param out
	 *            the buffer into which the data is read
	 * @param off
	 *            the start offset of the data
	 * @param len
	 *            the maximum number of bytes read
	 * @return the actual number of bytes read; -1 is returned when the end of
	 *         the stream is reached.
	 */
	public int read(final byte[] out, final int off, final int len) {
		int rval = 0; // by default, read nothing
		// check we are not passed the end of the buffer
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
			System.arraycopy(buf, pos, out, off, rval);
			pos += rval;
		}
		return rval;
	}

	/**
	 * Skips n bytes of input.
	 * 
	 * @param nSkip
	 *            the number of bytes to be skipped
	 * @return the actual number of bytes skipped.
	 */
	public long skip(final long nSkip) {
		if (nSkip > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(nSkip + " > Integer.MAX_VALUE");
		}
		int rval = (int)nSkip;
		if (pos + rval > count) {
			rval = count - pos;
		}
		if (rval < 0) {
			rval = 0;
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