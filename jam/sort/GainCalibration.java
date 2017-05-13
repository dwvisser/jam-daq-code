/*
 * Created on Dec 15, 2004
 */
package jam.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for writers of sort routines to easily read in calibrations and
 * use them.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 */
public final class GainCalibration {

	private transient final Map<Integer, Double> gains = new HashMap<Integer, Double>();

	private transient final Map<Integer, Double> offsets = new HashMap<Integer, Double>();

	private transient boolean suppress = false;

	/**
	 * Creates a new gain calibration.
	 */
	public GainCalibration() {// NOPMD
		super();
	}

	/**
	 * Returns an integer approximation to the gain-adjusted value for the given
	 * parameter id and channel. The formula is:
	 * <code>rval=adjustFP(param,value)+random(-gain/2,+gain/2)</code>, where
	 * random(min,max) is a uniformly distributed random number between min and
	 * max.
	 * 
	 * @param param
	 *            parameter id
	 * @param value
	 *            channel datum
	 * @return exact gain-adjusted value
	 * @see #adjustExact(int, int)
	 */
	public int adjust(final int param, final int value) {
		int rval = suppress ? 0 : value;
		if (gains.containsKey(param) && offsets.containsKey(param)) {
			final double gain = gains.get(param).doubleValue();
			final double offset = gains.get(param).doubleValue();
			final double randomOffset = (Math.random() - 0.5) * gain;
			final double dval = gain * value + offset + randomOffset;
			rval = (int) Math.round(dval);
		}
		return rval;
	}

	/**
	 * Returns the exact floating point gain-adjusted value for the given
	 * parameter id and channel. The formula is:
	 * <code>rval=gain*value+offset</code>
	 * 
	 * @param param
	 *            parameter id
	 * @param value
	 *            channel datum
	 * @return exact gain-adjusted value
	 */
	public double adjustExact(final int param, final int value) {
		double rval = suppress ? 0 : value;
		if (gains.containsKey(param) && offsets.containsKey(param)) {
			final double gain = gains.get(param);
			final double offset = offsets.get(param);
			rval = gain * value + offset;
		}
		return rval;
	}

	/**
	 * Reads in a gain file. The expected format is a text file where every line
	 * is lists a parameter id, a gain coefficient and an offset value.
	 * Alternately, the offset column may be ommited.
	 * 
	 * @param name
	 *            of the gain file
	 * @param autoSuppress
	 *            <code>true</code> adjust to zero any value for which no gain
	 *            coeffiecients exist, <code>false</code> to simply return the
	 *            original value back to the user
	 * @throws SortException
	 *             if there's a problem reading the gain file
	 */
	public void gainFile(final String name, final boolean autoSuppress)
			throws SortException {
		try {
			suppress = autoSuppress;
			FileInputStream input = new FileInputStream(new File(name));
			final int rows = getNumberOfRows(input);
			input = new FileInputStream(new File(name));
			final int columns = getNumberOfColumns(input);
			if (columns < 2 || columns > 3) {
				throw new SortException("File, \"" + name
						+ "\", must be 2 or 3 columns.");
			}
			input = new FileInputStream(new File(name));
			readGains(input, rows, columns);
		} catch (IOException ioe) {
			throw new SortException(
					"There was an error while reading the gain file, \"" + name
							+ "\".", ioe);
		}
	}

	private int getNumberOfColumns(final InputStream input) throws IOException {
		int rval = 0;
		final LineNumberReader lnr = new LineNumberReader(
				new InputStreamReader(input));
		final String line = lnr.readLine();
		lnr.close();
		if (line != null) {
			final StreamTokenizer tokenizer = new StreamTokenizer(
					new StringReader(line));
			while (tokenizer.nextToken() == StreamTokenizer.TT_NUMBER) {
				rval++;
			}
		}
		return rval;
	}

	private int getNumberOfRows(final InputStream input) throws IOException {
		final LineNumberReader lnr = new LineNumberReader(
				new InputStreamReader(input));
		lnr.setLineNumber(1);// start counting at 1
		// read in header lines, header are lines that start with a non-number
		// token
		final CharBuffer buffer = CharBuffer.allocate(input.available());
		int numRead = 0;
		do {
			numRead = lnr.read(buffer);
			buffer.clear();
		} while (numRead >= 0);
		final int rval = lnr.getLineNumber();
		lnr.close();
		return rval;
	}

	private void readGains(final InputStream input, final int rows,
			final int columns) throws IOException {
		final LineNumberReader lnr = new LineNumberReader(
				new InputStreamReader(input));
		final StreamTokenizer tokenizer = new StreamTokenizer(lnr);
		tokenizer.eolIsSignificant(false);
		for (int i = 0; i < rows; i++) {
			tokenizer.nextToken();
			final int parameter = (int) tokenizer.nval;
			tokenizer.nextToken();
			final double gain = tokenizer.nval;
			final double offset;
			if (columns == 3) {
				tokenizer.nextToken();
				offset = tokenizer.nval;
			} else {
				offset = 0.0;
			}
			gains.put(parameter, gain);
			offsets.put(parameter, offset);
		}
	}

}
