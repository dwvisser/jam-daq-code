package jam.io;
import jam.data.DataException;
import jam.data.Histogram;
import jam.global.MessageHandler;
import java.awt.Frame;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamTokenizer;

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
 * @author <a href="mailto:dale@visser.name">Dale W. Visser</a>
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
	protected void readHist(InputStream inStream) throws ImpExpException {
		InputStreamReader isr;
		StreamTokenizer st;
		int channel;

		int size = 0;
		String title = null;
		int tokenLine, temp, index;
		boolean firstLine;
		double[] counts = new double[MAX_SIZE_HIST];

		//histogram properties
		String nameHist;
		int typeHist, sizeHist;
		String titleHist;
		double[] countsHistDbl;

		try {
			isr = new InputStreamReader(inStream);
			st = new StreamTokenizer(isr); //make a tokenizer for input stream
			st.eolIsSignificant(true); //Grab end of line markers
			//read in header lines, header are lines that start with a non-number token
			firstLine = true;
			while (st.nextToken() != StreamTokenizer.TT_EOF
				&& st.ttype != StreamTokenizer.TT_NUMBER) {
				//execute loop code only if not number or EOF read
				if (firstLine) {
					title = st.sval;
				}
				//read in rest of line and ignore
				while (st.nextToken() != StreamTokenizer.TT_EOL) {
					if (firstLine) {
						title = title + " " + st.sval;
					}
				}
				firstLine = false;
			}
			//read in first line that starts with a number find if we have one or two number per line
			tokenLine = 0;
			temp = (int) st.nval;
			//check for second token on line            
			if (st.nextToken() != StreamTokenizer.TT_EOF
				&& st.ttype == StreamTokenizer.TT_EOL) { //no second number token
				tokenLine = 1;
				counts[0] = temp;
				size = 1;
				//second number token
			} else if (st.ttype == StreamTokenizer.TT_NUMBER) {
				tokenLine = 2;
				channel = temp;
				counts[temp] = st.nval;
				size = 1;
				if (st.nextToken() != StreamTokenizer.TT_EOF
					&& st.ttype != StreamTokenizer.TT_EOL) {
					throw new ImpExpException("Error file, does not have one or two numbers per line after header (1) [ImpExpASCII]");
				}
			} else {
				throw new ImpExpException("Error file, does not have one or two numbers per line after header (2) [ImpExpASCII]");
			}
			//one number per line
			if (tokenLine == 1) {
				channel = 0;
				//first token counts
				while (st.nextToken() != StreamTokenizer.TT_EOF
					&& st.ttype == StreamTokenizer.TT_NUMBER) {
					channel++;
					counts[channel] = st.nval;
					//second token eol
					if (st.nextToken() != StreamTokenizer.TT_EOF
						&& st.ttype != StreamTokenizer.TT_EOL) {
						throw new ImpExpException(
							"Data line not ended with EOL Line: "
								+ channel
								+ "  [ImpExpASCII]");
					}
					size = channel + 1;
				}

				//two numbers per line
			} else if (tokenLine == 2) {
				//first token channel
				while (st.nextToken() != StreamTokenizer.TT_EOF
					&& st.ttype == StreamTokenizer.TT_NUMBER) {
					channel = (int) st.nval;
					//second channel counts
					if (st.nextToken() == StreamTokenizer.TT_NUMBER) {
						counts[channel] = st.nval;
					} else {
						throw new ImpExpException(
							"Data line does not have two values  Channel: "
								+ channel
								+ "  [ImpExpASCII]");
					}
					//third token eol
					if (st.nextToken() != StreamTokenizer.TT_EOF
						&& st.ttype != StreamTokenizer.TT_EOL) {
						throw new ImpExpException(
							"Data line not ended with EOL Channel: "
								+ channel
								+ "  [ImpExpASCII]");
					}
					if (channel > size) {
						size = channel + 1;
					}
				}

			}

			//Create histogram properties
			//make histogram name, type, title, counts
			index = getFileName(lastFile).indexOf(".");
			nameHist = getFileName(lastFile).substring(0, index);
			typeHist = Histogram.ONE_DIM_DOUBLE;
			sizeHist = size + 1;
			//title of histogram
			if (title != null) {
				titleHist = title;
			} else {
				titleHist = getFileName(lastFile);
			}
			countsHistDbl = new double[size];
			System.arraycopy(counts, 0, countsHistDbl, 0, size);
			//create histogram
			new Histogram(nameHist, titleHist, countsHistDbl);
			if (msgHandler != null)
				msgHandler.messageOut(" . ");

		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		} catch (DataException de) {
			throw new ImpExpException(de.toString());
		}
	}

	/**
	 * Write out a data into a ascii text file.
	 *
	 * @param	    outStream	    the stream to write the histogram to
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
}
