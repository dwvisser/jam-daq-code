package jam.io;
import jam.data.Histogram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;

import javax.swing.filechooser.FileFilter;

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
public class ImpExpASCII extends AbstractImpExp {

	/**
	 * maximum number of channels per histogram
	 */
	static final int MAX_CHANNELS = 8192;
	
	private boolean line1isTitle=false;

	private static final String [] EXTS={"dat","txt"};
	private static final ExtensionFileFilter FILTER=new ExtensionFileFilter(EXTS, 
	"Text file");
	
	protected FileFilter getFileFilter() {
		return FILTER;
	}
	
	protected String getDefaultExtension(){
		return FILTER.getExtension(0);
	}

	public String getFormatDescription() {
		return FILTER.getDescription();
	}

	public boolean openFile(File file) throws ImpExpException {
		return openFile(file, "Import text file ");
	}

	/**
	 * Write out the data as a ASCII file.  XVGR can read the format directly.
	 *
	 * @param hist  the current <code>Histogram</code>
	 * @exception   ImpExpException all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	public void saveFile(Histogram hist) throws ImpExpException {
		saveFile("Export text file ", hist);
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
			final String titleHist = getHistTitle();
			//also determines whether 1st line is title
			String nameHist = getFileName(getLastFile());
			nameHist = nameHist.substring(0, nameHist.indexOf('.'));
			final int rows = getNumberOfRows();
			final int cols = getNumberOfColumns();
			switch (cols) {
				case 0 :
					return;
				case 1 :
					counts = new double[rows];
					readHistY(inStream, nameHist, titleHist, counts);
					break;
				case 2 :
					getMaxChannelsXY(rows);
					counts = new double[maxX+1];
					readHistXY(inStream, nameHist, titleHist, counts, rows);
					break;
				case 3 :
					getMaxChannelsXYZ(rows);
					counts2d = new double[maxX+1][maxY+1];
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

	private void readHistY(InputStream inputStream, String name, String title,
            double[] counts) throws IOException {
        final LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(inputStream));
        if (this.line1isTitle) {
            lnr.readLine();
        }
        final StreamTokenizer tokens = new StreamTokenizer(lnr);
        tokens.eolIsSignificant(false);
        for (int i = 0; i < counts.length; i++) {
            tokens.nextToken();
            counts[i] = tokens.nval;
        }
        Histogram.createHistogram(counts, name, title);
    }

	private int maxX, maxY;

    private void getMaxChannelsXY(int rows) throws IOException {
        maxX = 0;
        final LineNumberReader lnr = new LineNumberReader(new FileReader(
                getLastFile()));
        if (this.line1isTitle) {
            lnr.readLine();
        }
        final StreamTokenizer tokens = new StreamTokenizer(lnr);
        tokens.eolIsSignificant(false);
        for (int i = 0; i < rows; i++) {
            tokens.nextToken();
            if (tokens.nval > maxX) {
                maxX = (int) tokens.nval;
            }
            tokens.nextToken();
        }
    }

	private void getMaxChannelsXYZ(int rows) throws IOException {
        maxX = 0;
        maxY = 0;
        final LineNumberReader lnr = new LineNumberReader(new FileReader(
                getLastFile()));
        if (this.line1isTitle) {
            lnr.readLine();
        }
        final StreamTokenizer tokens = new StreamTokenizer(lnr);
        tokens.eolIsSignificant(false);
        for (int i = 0; i < rows; i++) {
            tokens.nextToken();
            if (tokens.nval > maxX) {
                maxX = (int) tokens.nval;
            }
            tokens.nextToken();
            if (tokens.nval > maxY) {
                maxY = (int) tokens.nval;
            }
            tokens.nextToken();
        }
    }

	private void readHistXY(InputStream inputStream, String name, String title,
            double[] counts, int rows) throws IOException {
        final LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(inputStream));
        if (this.line1isTitle) {
            lnr.readLine();
        }
        final StreamTokenizer tokens = new StreamTokenizer(lnr);
        tokens.eolIsSignificant(false);
        for (int i = 0; i < rows; i++) {
            tokens.nextToken();
            final int channel = (int) tokens.nval;
            tokens.nextToken();
            counts[channel] = tokens.nval;
        }
        Histogram.createHistogram(counts, name, title);
    }

	private void readHistXYZ(InputStream inputStream, String name,
            String title, double[][] counts, int rows) throws IOException {
        final LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(inputStream));
        if (this.line1isTitle) {
            lnr.readLine();
        }
        final StreamTokenizer tokens = new StreamTokenizer(lnr);
        tokens.eolIsSignificant(false);
        for (int i = 0; i < rows; i++) {
            tokens.nextToken();
            final int channelX = (int) tokens.nval;
            tokens.nextToken();
            final int channelY = (int) tokens.nval;
            tokens.nextToken();
            counts[channelX][channelY] = tokens.nval;
        }
        Histogram.createHistogram(counts, name, title);
    }

	private void readHistMatrix(InputStream inputStream, String name,
            String title, double[][] counts) throws IOException {
        final LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader(inputStream));
        if (this.line1isTitle) {
            lnr.readLine();
        }
        final StreamTokenizer tokens = new StreamTokenizer(lnr);
        tokens.eolIsSignificant(false);
        for (int i = 0; i < counts.length; i++) {
            for (int j = 0; j < counts[0].length; j++) {
                tokens.nextToken();
                counts[i][j] = tokens.nval;
            }
        }
        Histogram.createHistogram(counts, name, title);
    }
	

    private String getHistTitle() throws IOException {
        String rval = null;
        final InputStreamReader isr = new InputStreamReader(
                new FileInputStream(getLastFile()));
        /* Make a tokenizer for input stream. */
        final StreamTokenizer tokens = new StreamTokenizer(isr);
        tokens.eolIsSignificant(true); //Grab end of line markers
        /*
         * Read in header lines, header are lines that start with a non-number
         * token.
         */
        if (tokens.nextToken() == StreamTokenizer.TT_WORD) {
            rval = tokens.sval;
            line1isTitle = true;
        } else {
            rval = getFileName(getLastFile());
            rval = rval.substring(0, rval.indexOf("."));
        }
        isr.close();
        return rval;
    }

	private int getNumberOfRows() throws IOException {
        int rval = 0;
        final LineNumberReader lnr = new LineNumberReader(new FileReader(
                getLastFile()));
        /*
         * Read in header lines. Headers are lines that start with a non-number
         * token.
         */
        if (line1isTitle) {
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
        final LineNumberReader lnr = new LineNumberReader(new FileReader(
                getLastFile()));
        /*
         * Read in header lines. Headers are lines that start with a non-number
         * token.
         */
        if (this.line1isTitle) {
            lnr.readLine();
        }
        final String line = lnr.readLine();
        lnr.close();
        if (line != null) {
            final StreamTokenizer tokens = new StreamTokenizer(
                    new StringReader(line));
            while (tokens.nextToken() == StreamTokenizer.TT_NUMBER) {
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
			final PrintWriter writer = new PrintWriter(buffout);
			if (hist.getType() == Histogram.Type.ONE_DIM_INT) {
				final int[] counts = (int[]) hist.getCounts();
				writeHist(writer,counts,hist.getSizeX());
			} else if (hist.getType() == Histogram.Type.ONE_D_DOUBLE) {
				final double[] countsD = (double[]) hist.getCounts();
				for (int i = 0; i < hist.getSizeX(); i++) {
					//output a row of data  channel counts
					writer.print(i);
					writer.print("   ");
					writer.println(countsD[i]);
				}
			} else if (hist.getType() == Histogram.Type.TWO_DIM_INT) {
				final int[][] counts = (int[][]) hist.getCounts();
				writeHist(writer,counts,hist.getSizeX(), hist.getSizeY());
			} else if (hist.getType() == Histogram.Type.TWO_D_DOUBLE) {
				final double[][] counts = (double[][]) hist.getCounts();
				for (int x = 0; x < hist.getSizeX(); x++) {
					for (int y = 0; y < hist.getSizeY(); y++) {
						writer.print(counts[x][y]);
						writer.print("\t");
					}
					writer.println();
				}
			}
			writer.flush();
			buffout.flush();
			if (msgHandler != null){
				msgHandler.messageOut(" . ");
			}
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}
	
	private void writeHist(PrintWriter writer, int [] counts, int sizeX){
		for (int i = 0; i < sizeX; i++) {
			//output a row of data  channel counts
			writer.print(i);
			writer.print("   ");
			writer.println(counts[i]);
		}
	}

	private void writeHist(PrintWriter writer, int [][] counts, int sizeX,
	        int sizeY){
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				writer.print(counts[x][y]);
				writer.print("\t");
			}
			writer.println();
		}
	}
	
	public boolean canExport(){
		return true;
	}
	
	boolean batchExportAllowed(){
		return true;
	}
}
