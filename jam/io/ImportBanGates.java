package jam.io;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ExtensionFileFilter;

import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StreamTokenizer;

import javax.swing.filechooser.FileFilter;

/**
 * Imports banana gate files used by the ORPHLIB software at the HRIBF at ORNL.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 13, 2004
 */
public final class ImportBanGates extends AbstractImpExp {
	
	public ImportBanGates(){//NOPMD
		super();
	}

	/**
	 * @see jam.io.AbstractImpExp#openFile(File, String)
	 */
	public boolean openFile(final File file) throws ImpExpException {
		return openFile(file, "Open BAN file");
	}

	/**
	 * @see jam.io.AbstractImpExp#saveFile(jam.data.Histogram)
	 */
	public void saveFile(final Histogram hist) throws ImpExpException {
		LOGGER.warning("Save BAN not implemented.");
	}

	private static final ExtensionFileFilter FILTER = new ExtensionFileFilter(
			"ban", "ORNL Banana Gates");

	protected FileFilter getFileFilter() {
		return FILTER;
	}

	protected String getDefaultExtension() {
		return FILTER.getExtension(0);
	}

	/**
	 * @see jam.io.AbstractImpExp#getFormatDescription()
	 */
	public String getFormatDescription() {
		return FILTER.getDescription();
	}

	/**
	 * @see jam.io.AbstractImpExp#readData(java.io.InputStream)
	 */
	protected void readData(final InputStream inStream) throws ImpExpException {
		final int[] gates;
		final Reader reader = new InputStreamReader(inStream);
		final StreamTokenizer tokens = new StreamTokenizer(reader);
		try {
			gates = readHeader(tokens);
			for (int i = 0; i < gates.length; i++) {
				readGate(tokens);
			}
		} catch (IOException e) {
			throw new ImpExpException("Problem reading data.", e);
		}
	}

	private int[] readHeader(final StreamTokenizer parser) throws IOException {
		int index = 0;
		final int[] gates = new int[80];
		while (parser.nextToken() == StreamTokenizer.TT_NUMBER) {
			final int number = (int) parser.nval;
			if (number > 0) {
				gates[index] = number;
				index++;
			}
		} // fall out when we've read "INP"
		parser.pushBack();
		final int[] rval = new int[index];
		System.arraycopy(gates, 0, rval, 0, index);
		return rval;
	}

	private void readGate(final StreamTokenizer parser) throws IOException {
		final String CXY = "CXY";
		final String INP = "INP";
		/* first advance to start of record */
		do {
			parser.nextToken();
		} while (!INP.equals(parser.sval));
		parser.nextToken(); // hisfilename
		parser.nextToken(); // hist #
		final int hisNum = (int) parser.nval;
		final Histogram his = Histogram.getHistogram(hisNum);
		parser.nextToken(); // gate number
		final int gateNum = (int) parser.nval;
		parser.nextToken(); // 0
		parser.nextToken(); // # points
		final int numPoints = (int) parser.nval;
		while (!CXY.equals(parser.sval)) {
			parser.nextToken();
		}
		final Polygon shape = new Polygon();
		for (int i = 0; i < numPoints; i++) {
			parser.nextToken(); // x
			if (parser.ttype == StreamTokenizer.TT_WORD) {
				parser.nextToken(); // skip "CXY" 's
			}
			final int xval = (int) parser.nval;
			parser.nextToken(); // y
			final int yval = (int) parser.nval;
			shape.addPoint(xval, yval);
		}
		final String name = String.valueOf(gateNum);
		if (his == null) {
			LOGGER.warning("Couldn't load gate " + name
					+ ". Expected histogram # " + hisNum + ".");
		} else {
			Gate gate = Gate.getGate(name);
			if (gate == null || his.getDimensionality() != 2) {
				gate = new Gate(name, his);
			}
			gate.setLimits(shape);
		}
	}

	/**
	 * @see jam.io.AbstractImpExp#writeHist(java.io.OutputStream,
	 *      jam.data.Histogram)
	 */
	protected void writeHist(final OutputStream outStream, final Histogram hist)
			throws ImpExpException {
		// not implementing
	}

	public boolean canExport() {
		return false;
	}

	boolean batchExportAllowed() {
		return false;
	}
}
