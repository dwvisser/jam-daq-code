package jam.io;

import jam.data.Gate;
import jam.data.Histogram;

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
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 13, 2004
 */
public class ImportBanGates extends ImpExp {

	public ImportBanGates(){
		super();
	}

	/**
	 * @see jam.io.ImpExp#openFile(File, String)
	 */
	public boolean openFile(File f) throws ImpExpException {
		return openFile(f, "Open BAN file");
	}

	/**
	 * @see jam.io.ImpExp#saveFile(jam.data.Histogram)
	 */
	public void saveFile(Histogram hist) throws ImpExpException {
		msgHandler.warningOutln("Save BAN not implemented.");
	}

	private static final ExtensionFileFilter filter=new ExtensionFileFilter("ban", 
	"ORNL Banana Gates");
	protected FileFilter getFileFilter() {
		return filter;
	}
	
	protected String getDefaultExtension(){
		return filter.getExtension(0);
	}

	/**
	 * @see jam.io.ImpExp#getFormatDescription()
	 */
	public String getFormatDescription() {
		return filter.getDescription();
	}

	/**
	 * @see jam.io.ImpExp#readData(java.io.InputStream)
	 */
	protected void readData(InputStream inStream) throws ImpExpException {
		final int[] gates;
		final Reader reader = new InputStreamReader(inStream);
		final StreamTokenizer tokens = new StreamTokenizer(reader);
		try {
			gates = readHeader(tokens);
			for (int i = 0; i < gates.length; i++) {
				readGate(tokens);
			}
		} catch (IOException e) {
			throw new ImpExpException(e.toString());
		}
	}

	private int[] readHeader(StreamTokenizer st) throws IOException {
		int i = 0;
		final int[] gates = new int[80];
		while (st.nextToken() == StreamTokenizer.TT_NUMBER) {
			final int n = (int) st.nval;
			if (n > 0) {
				gates[i] = n;
				i++;
			}
		} //fall out when we've read "INP"
		st.pushBack();
		final int[] rval = new int[i];
		System.arraycopy(gates, 0, rval, 0, i);
		return rval;
	}

	private void readGate(StreamTokenizer st)
		throws IOException {
		final String CXY="CXY";
		final String INP="INP";
		/* first advance to start of record */
		do {
			st.nextToken();
		} while (!INP.equals(st.sval));
		st.nextToken(); //hisfilename
		st.nextToken(); //hist #
		final int hisNum = (int) st.nval;
		final Histogram his = Histogram.getHistogram(hisNum);
		st.nextToken(); //gate number
		final int gateNum = (int) st.nval;
		st.nextToken(); //0
		st.nextToken(); //# points
		final int numPoints = (int) st.nval;
		while (!CXY.equals(st.sval)) {
			st.nextToken();
		}
		final Polygon p = new Polygon();
		for (int i = 0; i < numPoints; i++) {
			st.nextToken(); //x
			if (st.ttype == StreamTokenizer.TT_WORD) {
				st.nextToken(); //skip "CXY" 's
			}
			final int x = (int) st.nval;
			st.nextToken(); //y
			final int y = (int) st.nval;
			p.addPoint(x, y);
		}
		final String name = String.valueOf(gateNum);
		if (his != null) {
			Gate g = Gate.getGate(name);
			if (g == null || his.getDimensionality() != 2) {
				g = new Gate(name, his);
			}
			g.setLimits(p);
		} else {
			this.msgHandler.warningOutln(
				"Couldn't load gate "
					+ name
					+ ". Expected histogram # "
					+ hisNum
					+ ".");
		}
	}

	/**
	 * @see jam.io.ImpExp#writeHist(java.io.OutputStream, jam.data.Histogram)
	 */
	protected void writeHist(OutputStream outStream, Histogram hist)
		throws ImpExpException {
			//not implementing
	}
	
	public boolean canExport(){
		return false;
	}

	boolean batchExportAllowed(){
		return false;
	}
}
