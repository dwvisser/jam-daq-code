package jam.io;
import jam.data.Histogram;
import jam.global.MessageHandler;
import java.awt.Frame;
import java.io.*;

/**
 * Imports and exports histograms in ASCII channel-space-counts-return format.
 * This is usefol for easy import into other applications.  For
 * 1-D histograms, the format looks like, e.g. <code><br>
 * 0 0<br>
 * 1 2<br>
 * 3 5<br>
 * ...</code>
 * <P>For 2-d histograms, the format all bins for a particular 
 * x-channel on one line, with the y channels listed sequentially
 * on the line. E.g., a 4x4 histogram where the bin counts are '2x+y'
 * looks as follows:<code><br>
 * 0 1 2 3<br>
 * 2 3 4 5<br>
 * 4 5 6 7<br>
 * 6 7 8 9<br></code>
 * </p>
 *
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 1.0
 */
public class ImpExpASCII extends ImpExp {

	/**
	 * maximum number of channels per histogram
	 */
	static final int MAX_SIZE_HIST = 8192;

	/**
	 * Class constructor.
	 *
	 * @param	frame	the <bold>jam</bold> display frame
	 * @param	msgHandler the <bold>jam</bold> msgHandler
	 *
	 * @see	jam.global.MessageHandler
	 */
	public ImpExpASCII(Frame frame, MessageHandler msgHandler) {
		super(frame, msgHandler);
	}

	public ImpExpASCII() {
		super();
	}

	public String getFileExtension() {
		return ".dat";
	}

	public String getFormatDescription() {
		return "Text file";
	}

	/**
	 * Open a file with ASCII data.
	 *
	 * @exception   ImpExpException	    all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	public boolean openFile() throws ImpExpException {
		return openFile("Import text file ", "dat");
	}

	/**
	 * Write out the data as a ASCII file.  XVGR can read the format directly.
	 *
	 * @param hist  the current <code>Histogram</code>
	 * @exception   ImpExpException all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	public void saveFile(Histogram hist) throws ImpExpException {
		saveFile("Export text file ", "dat", hist);
	}

	/**
	 * Read ASCII data from an <code>InputStream</code>.
	 * The first lines may be a header these lines are read in until a line with
	 * leading token that is a number is found.
	 * The first line of the header will be used as the title.
	 * (Its first token as above cannot be a number.)
	 *
	 * <p>Data after the header may be one or two numbers per line.
	 * For one number per line, these are assumed to be the counts
	 * starting at ch 0.
	 * For two numbers per line, the first number is the channel and
	 * the second number the counts in that channel.</p>
	 *
	 * @param	    inStream	    the stream to read the histogram from
	 * @exception   ImpExpException exception related to import/export
	 */
	protected void readData(InputStream inStream) throws ImpExpException {
		double[] counts;
		double[][] counts2d;

		try {
			String titleHist = this.getHistTitle();
			//also determines whether 1st line is title
			String nameHist = getFileName(lastFile);
			nameHist = nameHist.substring(0, nameHist.indexOf('.'));
			int rows = getNumberOfRows();
			int cols = getNumberOfColumns();
			switch (cols) {
				case 0 :
					return;
				case 1 :
					counts = new double[rows];
					readHistY(inStream, nameHist, titleHist, counts);
					break;
				case 2 :
					getMaxChannelsXY(rows);
					counts = new double[maxX];
					readHistXY(inStream, nameHist, titleHist, counts, rows);
					break;
				case 3 :
					getMaxChannelsXYZ(rows);
					counts2d = new double[maxX][maxY];
					readHistXYZ(inStream, nameHist, titleHist, counts2d, rows);
					break;
				default :// >=4 cases
					counts2d = new double[rows][cols];
					readHistMatrix(inStream, nameHist, titleHist, counts2d);
					break;
			}
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}

	private void readHistY(
		InputStream in,
		String name,
		String title,
		double[] counts)
		throws IOException {
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		StreamTokenizer st = new StreamTokenizer(lnr);
		st.eolIsSignificant(false);
		for (int i = 0; i < counts.length; i++) {
			st.nextToken();
			counts[i] = st.nval;
		}
		new Histogram(name, title, counts);
	}

	private int maxX, maxY;
	private void getMaxChannelsXY(int rows) throws IOException {
		maxX = 0;
		LineNumberReader lnr =
			new LineNumberReader(new FileReader(this.lastFile));
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		StreamTokenizer st = new StreamTokenizer(lnr);
		st.eolIsSignificant(false);
		for (int i = 0; i < rows; i++) {
			st.nextToken();
			if (st.nval > maxX) {
				maxX = (int) st.nval;
			}
			st.nextToken();
		}
	}

	private void getMaxChannelsXYZ(int rows) throws IOException {
		maxX = 0;
		maxY = 0;
		LineNumberReader lnr =
			new LineNumberReader(new FileReader(this.lastFile));
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		StreamTokenizer st = new StreamTokenizer(lnr);
		st.eolIsSignificant(false);
		for (int i = 0; i < rows; i++) {
			st.nextToken();
			if (st.nval > maxX) {
				maxX = (int) st.nval;
			}
			st.nextToken();
			if (st.nval > maxY) {
				maxY = (int) st.nval;
			}
			st.nextToken();
		}
	}

	private void readHistXY(
		InputStream in,
		String name,
		String title,
		double[] counts,
		int rows)
		throws IOException {
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		StreamTokenizer st = new StreamTokenizer(lnr);
		st.eolIsSignificant(false);
		for (int i = 0; i < rows; i++) {
			st.nextToken();
			int channel = (int) st.nval;
			st.nextToken();
			counts[channel] = st.nval;
		}
		new Histogram(name, title, counts);
	}

	private void readHistXYZ(
		InputStream in,
		String name,
		String title,
		double[][] counts,
		int rows)
		throws IOException {
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		StreamTokenizer st = new StreamTokenizer(lnr);
		st.eolIsSignificant(false);
		for (int i = 0; i < rows; i++) {
			st.nextToken();
			int channelX = (int) st.nval;
			st.nextToken();
			int channelY = (int) st.nval;
			st.nextToken();
			counts[channelX][channelY] = st.nval;
		}
		new Histogram(name, title, counts);
	}

	private void readHistMatrix(
		InputStream in,
		String name,
		String title,
		double[][] counts)
		throws IOException {
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(in));
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		StreamTokenizer st = new StreamTokenizer(lnr);
		st.eolIsSignificant(false);
		for (int i = 0; i < counts.length; i++) {
			for (int j = 0; j < counts[0].length; j++) {
				st.nextToken();
				counts[i][j] = st.nval;
			}
		}
		new Histogram(name, title, counts);
	}
	boolean firstLineIsTitle = false;
	private String getHistTitle() throws IOException {
		String rval = null;
		InputStreamReader isr =
			new InputStreamReader(new FileInputStream(lastFile));
		StreamTokenizer st = new StreamTokenizer(isr);
		//make a tokenizer for input stream
		st.eolIsSignificant(true); //Grab end of line markers
		//read in header lines, header are lines that start with a non-number token
		if (st.nextToken() == StreamTokenizer.TT_WORD) {
			rval = st.sval;
			firstLineIsTitle = true;
		} else {
			rval = getFileName(lastFile);
			rval = rval.substring(0, rval.indexOf("."));
		}
		isr.close();
		return rval;
	}

	private int getNumberOfRows() throws IOException {
		int rval = 0;
		LineNumberReader lnr = new LineNumberReader(new FileReader(lastFile));
		//read in header lines, header are lines that start with a non-number token
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		while (lnr.readLine() != null) {
			rval++;
		}
		lnr.close();
		return rval;
	}

	private int getNumberOfColumns() throws IOException {
		int rval = 0;
		LineNumberReader lnr = new LineNumberReader(new FileReader(lastFile));
		//read in header lines, header are lines that start with a non-number token
		if (this.firstLineIsTitle) {
			lnr.readLine();
		}
		String line = lnr.readLine();
		lnr.close();
		if (line != null) {
			StreamTokenizer st = new StreamTokenizer(new StringReader(line));
			while (st.nextToken() == StreamTokenizer.TT_NUMBER) {
				rval++;
			}
		}
		return rval;
	}

	/**
	 * Write out a data into a ascii text file.
	 *
	 * @param	    buffout	    the stream to write the histogram to
	 * @param hist the histogram to write
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	protected void writeHist(OutputStream buffout, Histogram hist)
		throws ImpExpException {
		try {
			PrintWriter pw = new PrintWriter(buffout);
			if (hist.getType() == Histogram.ONE_DIM_INT) {
				int[] counts = (int[]) hist.getCounts();
				for (int i = 0; i < hist.getSizeX(); i++) {
					//output a row of data  channel counts
					pw.print(i);
					pw.print("   ");
					pw.println(counts[i]);
				}
			} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
				double[] countsD = (double[]) hist.getCounts();
				for (int i = 0; i < hist.getSizeX(); i++) {
					//output a row of data  channel counts
					pw.print(i);
					pw.print("   ");
					pw.println(countsD[i]);
				}
			} else if (hist.getType() == Histogram.TWO_DIM_INT) {
				int[][] counts = (int[][]) hist.getCounts();
				for (int x = 0; x < hist.getSizeX(); x++) {
					for (int y = 0; y < hist.getSizeY(); y++) {
						pw.print(counts[x][y]);
						pw.print("\t");
					}
					pw.println();
				}
			} else if (hist.getType() == Histogram.TWO_DIM_DOUBLE) {
				double[][] counts = (double[][]) hist.getCounts();
				for (int x = 0; x < hist.getSizeX(); x++) {
					for (int y = 0; y < hist.getSizeY(); y++) {
						pw.print(counts[x][y]);
						pw.print("\t");
					}
					pw.println();
				}
			}
			pw.flush();
			buffout.flush();
			if (msgHandler != null)
				msgHandler.messageOut(" . ");
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}
	
	public boolean canExport(){
		return true;
	}
	
	boolean batchExportAllowed(){
		return true;
	}
}
