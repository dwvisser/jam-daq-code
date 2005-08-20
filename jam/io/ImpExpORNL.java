package jam.io;

import jam.data.Histogram;
import jam.global.MessageHandler;
import jam.util.FileUtilities;
import jam.util.StringUtilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * Imports and exports Oak Ridge (Milner) formatted files, as used by
 * <code>DAMM</code> and <code>SORT</code>. A set of histograms consists of
 * 2 files:
 * <UL>
 * <LI>Data file-- <code><i>filename</i>.his</code></li>
 * <li>Directory file-- <code><i>filename</i>.drr</code></li>
 * </ul>
 * Where <code><i>filename</i></code> is the same for both files.
 * 
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @version 0.5
 */
public class ImpExpORNL extends AbstractImpExp {

	/**
	 * sequence of ASCII encoded characters to begin <code>drr</code> file.
	 */
	static final String SIGNATURE = "HHIRFDIR0001";

	private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

	/**
	 * input steam for drr file
	 */
	private DataInputStream disDrr;

	/**
	 * Stuff read in for every drr file
	 */
	private String signature;

	private int totalHist; //number of histograms

	private int numHalfWords;

	/* Histogram info in Drr file for each histogram */
	private int[] dim; // Histogram dimensionality

	private int[] chSize; //half words per channel

	private int[] lenParScal1; //Length scaled parameters

	private int[] lenParScal2;

	private int[] offSet;

	private String[] titleDrr; //title 40 bytes

	private int[] iDnumber; //ID list

	private static final String[] EXTS = { "his", "drr" };

	private static final ExtensionFileFilter FILTER = new ExtensionFileFilter(
			EXTS, "Oak Ridge DAMM");

	protected FileFilter getFileFilter() {
		return FILTER;
	}

	protected String getDefaultExtension() {
		return FILTER.getExtension(0);
	}

	public String getFormatDescription() {
		return FILTER.getDescription();
	}

	/**
	 * Open a file which was written using ORNL .drr and .his format. Can't do
	 * the same as other ImpExp routines, since 2 files are needed.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	public boolean openFile(File file) throws ImpExpException {
		return openFile(file, "Import ORNL file ");
	}

	/**
	 * Write out a particular histogram.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	public void saveFile(Histogram hist) throws ImpExpException {
		saveFile("Export ORNL", hist);
	}

	/**
	 * Reads in a histogram from the event stream.
	 * 
	 * @exception ImpExpException
	 *                thrown for errors
	 */
	public void readData(final InputStream buffin) throws ImpExpException {
		try {
			Histogram.clearList(); //clear current list of histograms
			readDrr(buffin); //read the drr file
			final FileUtilities fileUtil = FileUtilities.getInstance();
			final String fileNameHis = fileUtil.changeExtension(
					getFileName(getLastFile()), "*.his", FileUtilities.FORCE);
			/* open .his file random access, read only */
			final RandomAccessFile fileHis = new RandomAccessFile(new File(
					getLastFile().getParentFile(), fileNameHis), "r");
			/* read in his file and load spectra */
			for (int k = 0; k < totalHist; k++) {
				readHist(fileHis, k);
			}
			fileHis.close();
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}

	/*
	 * non-javadoc: Read in ORNL drr file, which is the index to the his file.
	 */
	private void readDrr(InputStream buffin) throws IOException,
			ImpExpException {
		final byte[] bsignature = new byte[SIGNATURE.length()];
		final byte bChilText[] = new byte[80]; //chill file text;
		final byte[] parLabelb = new byte[12]; //paramater label
		final byte[] titleb = new byte[40]; //title
		final byte[] numHistByte = new byte[4];
		disDrr = new DataInputStream(buffin);
		//read in header
		disDrr.read(bsignature); //HHRIF signature
		signature = String.valueOf(bsignature);
		if (!(signature.equals(SIGNATURE))) {
			throw new ImpExpException("Incorrect header, expected '"
					+ SIGNATURE + "', but got '" + signature + "'.");
		}
		disDrr.read(numHistByte); //number of histograms
		byteOrder = ByteOrder.nativeOrder(); //assume file was created locally
		msgHandler.messageOut(", native byte order: " + byteOrder + ", ");
		if (!isCorrectByteOrder(byteArrayToInt(numHistByte, 0))) {
			if (byteOrder == ByteOrder.BIG_ENDIAN) {
				byteOrder = ByteOrder.LITTLE_ENDIAN;
			} else {
				byteOrder = ByteOrder.BIG_ENDIAN;
			}
		}
		msgHandler.messageOut("file byte order: " + byteOrder + ", ");
		totalHist = byteArrayToInt(numHistByte, 0); //number of histograms
		numHalfWords = readInt(disDrr); //total number of 16 bit words
		readInt(disDrr); //space nothing defined
		readInt(disDrr); //year
		readInt(disDrr); //month
		readInt(disDrr); //day of month
		readInt(disDrr); //hour
		readInt(disDrr); //minutes
		readInt(disDrr); //seconds
		disDrr.read(bChilText); //ASCII text from CHIL file
		/* Histogram info in Drr file */
		dim = new int[totalHist]; // Histogram dimensionality
		chSize = new int[totalHist]; //half words per channel
		final int [] param1 = new int[totalHist]; // Histogram parameter
		final int [] param2 = new int[totalHist];
		final int [] param3 = new int[totalHist];
		final int [] param4 = new int[totalHist];
		final int [] lenParRaw1 = new int[totalHist]; //Length raw parameters
		final int [] lenParRaw2 = new int[totalHist];
		final int [] lenParRaw3 = new int[totalHist];
		final int [] lenParRaw4 = new int[totalHist];
		lenParScal1 = new int[totalHist]; //Length scaled parameters
		lenParScal2 = new int[totalHist];
		final int [] lenParScal3 = new int[totalHist];
		final int [] lenParScal4 = new int[totalHist];
		final int [] minCh1 = new int[totalHist]; //Min channels
		final int [] minCh2 = new int[totalHist];
		final int [] minCh3 = new int[totalHist];
		final int [] minCh4 = new int[totalHist];
		final int [] maxCh1 = new int[totalHist]; //Max channels
		final int [] maxCh2 = new int[totalHist];
		final int [] maxCh3 = new int[totalHist];
		final int [] maxCh4 = new int[totalHist];
		offSet = new int[totalHist];
		final String [] parLabelX = new String[totalHist];
		final String [] parLabelY = new String[totalHist];
		final float [] cal1 = new float[totalHist]; // Calibration constants
		final float [] cal2 = new float[totalHist];
		final float [] cal3 = new float[totalHist];
		final float [] cal4 = new float[totalHist];
		titleDrr = new String[totalHist]; //title 40 bytes
		/* ID list */
		iDnumber = new int[totalHist];
		/* loop for all histograms reading directory entries */
		for (int i = 0; i < totalHist; i++) {
			dim[i] = readShort(disDrr); //Histogram dimensionality
			chSize[i] = readShort(disDrr); //half-words per channel
			param1[i] = readShort(disDrr); //dummy param #
			param2[i] = readShort(disDrr); //dummy param #
			param3[i] = readShort(disDrr); //dummy param #
			param4[i] = readShort(disDrr); //dummy param #
			lenParRaw1[i] = readShort(disDrr); //length Raw parameter
			lenParRaw2[i] = readShort(disDrr); //length Raw parameter
			lenParRaw3[i] = readShort(disDrr); //length Raw parameter
			lenParRaw4[i] = readShort(disDrr); //length Raw parameter
			lenParScal1[i] = readShort(disDrr);
			/* length Scaled parameters */
			lenParScal2[i] = readShort(disDrr);
			lenParScal3[i] = readShort(disDrr);
			lenParScal4[i] = readShort(disDrr);
			minCh1[i] = readShort(disDrr); //min channel 1
			minCh2[i] = readShort(disDrr); //min channel 2
			minCh3[i] = readShort(disDrr); //min channel 3
			minCh4[i] = readShort(disDrr); //min channel 4
			maxCh1[i] = readShort(disDrr); //max channel 1
			maxCh2[i] = readShort(disDrr); //max channel 2
			maxCh3[i] = readShort(disDrr); //max channel 3
			maxCh4[i] = readShort(disDrr); //max channle 4
			offSet[i] = readInt(disDrr); //offset in 16 bit words
			disDrr.read(parLabelb); //x param label
			parLabelX[i] = String.valueOf(parLabelb);
			disDrr.read(parLabelb); //y param label
			parLabelY[i] = String.valueOf(parLabelb);
			cal1[i] = disDrr.readFloat(); //calibaration const
			cal2[i] = disDrr.readFloat(); //calibaration const
			cal3[i] = disDrr.readFloat(); //calibaration const
			cal4[i] = disDrr.readFloat(); //calibaration const
			disDrr.read(titleb); //sub-Title
			titleDrr[i] = String.valueOf(titleb);
		}
		/* read in id list */
		for (int i = 0; i < totalHist; i++) {
			iDnumber[i] = readInt(disDrr); // Id number
		}
		disDrr.close();
	}

	private boolean isCorrectByteOrder(int numHists) {
		return numHists >= 0 && numHists <= 8000;
	}

	/* non-javadoc:
	 * Read in a histogram.
	 */
	private void readHist(RandomAccessFile fileHis, int index) throws IOException {
		/* copy to histogram variables */
		final String name = titleDrr[index].trim();
		final int number = iDnumber[index];
		final int type = dim[index];
		final int wordCh = chSize[index];
		final int sizeX = lenParScal1[index];
		final int sizeY = lenParScal2[index];
		if (type == 2) { //Read in 2D histogram
			int[][] counts2d = new int[sizeX][sizeY];
			fileHis.seek((long) offSet[index] * 2);
			final int bytesToRead = sizeX * sizeY * 4;
			final byte [] inBuffer = new byte[bytesToRead];
			fileHis.read(inBuffer); //read in byte array
			if (wordCh == 2) { //four byte data
				int offset = 0;
				for (int j = 0; j < sizeY; j++) {
					for (int i = 0; i < sizeX; i++) {
						counts2d[i][j] = byteArrayToInt(inBuffer, offset);
						offset += 4;
					}
				}
			} else if (wordCh == 1) { //two byte data
				int offset = 0;
				for (int j = 0; j < sizeY; j++) {
					for (int i = 0; i < sizeX; i++) {
						counts2d[i][j] = byteArrayToShort(inBuffer, offset);
						offset += 2;
					}
				}
			} else { //not able to handle data
				throw new IOException("File uses " + wordCh
						+ " words/channel, which I don't know how to read.");
			}
			final Histogram hist = Histogram.createHistogram(importGroup, counts2d, name);
			hist.setNumber(number);
			if (msgHandler != null) {
				msgHandler.messageOut(" .");
			}
		} else { //Read in 1D Histogram
			int[] counts = new int[sizeX];
			fileHis.seek((long) offSet[index] * 2);
			final int bytesToRead = sizeX * 4;
			final byte [] inBuffer = new byte[bytesToRead];
			fileHis.read(inBuffer); //read in byte array
			if (wordCh == 2) { //four byte data
				int offset = 0;
				for (int i = 0; i < sizeX; i++) {
					counts[i] = byteArrayToInt(inBuffer, offset);
					offset += 4;
				}
			} else if (wordCh == 1) { //two byte data
				int offset = 0;
				for (int i = 0; i < sizeX; i++) {
					counts[i] = byteArrayToShort(inBuffer, offset);
					offset += 2;
				}
			} else { //unable to handle data type
				throw new IOException("File uses " + wordCh
						+ " words/channel, which can't be read.");
			}
			final Histogram hist = Histogram.createHistogram(importGroup, counts, name);
			hist.setNumber(number);
			if (msgHandler != null) {
				msgHandler.messageOut(" .");
			}
		}
	}

	/**
	 * Writes a histogram to the file.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the msgHandler
	 */
	public void writeHist(final OutputStream ignored, final Histogram hist)
			throws ImpExpException {
		try {
			final FileUtilities fileUtil = FileUtilities.getInstance();
			final String fileNameHis = fileUtil.changeExtension(getLastFile()
					.getName(), ".his", FileUtilities.FORCE);
			final String fileNameDRR = fileUtil.changeExtension(getLastFile()
					.getName(), ".drr", FileUtilities.FORCE);
			final File parent = getLastFile().getParentFile();
			final File fileHis = new File(parent, fileNameHis);
			final FileOutputStream fosHis = new FileOutputStream(fileHis);
			final BufferedOutputStream buffoutHis = new BufferedOutputStream(
					fosHis);
			msgHandler.messageOut("\u2026");
			final File fileDRR = new File(parent, fileNameDRR);
			final FileOutputStream fosDRR = new FileOutputStream(fileDRR);
			final BufferedOutputStream buffoutDRR = new BufferedOutputStream(
					fosDRR);
			writeDrr(buffoutDRR); //write out drr file
			msgHandler.messageOut(fileHis.getName());
			writeHis(buffoutHis); //write out his file
			msgHandler.messageOut(" to " + parent + " ");
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}

	/*
	 * non-javadoc: write out a ORNL drr file
	 */
	private void writeDrr(OutputStream buffout) throws IOException {
		final StringUtilities util = StringUtilities.getInstance();
		int diskOffSet = 0;
		final DataOutputStream dosDrr = new DataOutputStream(buffout);
		final List allHists = Histogram.getHistogramList();
		/* number of histograms */
		totalHist = Histogram.getHistogramList().size(); //number of histograms
		/*
		 * total number of 1/2 words need in file size of file needed? in 16 bit
		 * words
		 */
		numHalfWords = 0;
		for (int i = 0; i < allHists.size(); i++) {
			final Histogram hist = ((Histogram) allHists.get(i));
			final int sizeX = hist.getSizeX();
			final int sizeY = hist.getSizeY(); //will be zero for 1-d
			final int histDim=hist.getDimensionality();
			if (histDim==1) {
				numHalfWords = numHalfWords + 2 * sizeX;
			} else if (histDim == 2) {
				numHalfWords = numHalfWords + 2 * sizeX
						* sizeY;
			} else {
				throw new IOException(
						"Unrecognized histogram type [ImpExpORNL]");
			}
		}
		/* write header */
		dosDrr.writeBytes(SIGNATURE); //HHRIF signature, ASCII encoded
		dosDrr.writeInt(totalHist); //number of histograms
		dosDrr.writeInt(numHalfWords); //total number of 16 bit words
		dosDrr.writeInt(0); //space nothing defined
		final Calendar calendar = Calendar.getInstance();
		dosDrr.writeInt(calendar.get(Calendar.YEAR)); //date year (date and
													  // time)
		dosDrr.writeInt(calendar.get(Calendar.MONTH + 1)); // month
		dosDrr.writeInt(calendar.get(Calendar.DAY_OF_MONTH)); //date day
		dosDrr.writeInt(calendar.get(Calendar.HOUR_OF_DAY)); //time
		dosDrr.writeInt(calendar.get(Calendar.MINUTE)); //time
		dosDrr.writeInt(calendar.get(Calendar.SECOND)); //time
		dosDrr.writeBytes(util.makeLength("File Created by Jam", 80));
		/* text from chill file */
		for (int i = 0; i < allHists.size(); i++) {
			final Histogram hist = ((Histogram) allHists.get(i));
			final short sizeX = (short) (hist.getSizeX());
			final short sizeY = (short) (hist.getSizeY()); //will be zero for 1-d
			// use data output stream name only 15 char long title 50 char long
			dosDrr.writeShort((short) (hist.getDimensionality()));
			dosDrr.writeShort(2); //half-words per channel
			dosDrr.writeShort(0); //dummy param #
			dosDrr.writeShort(0); //dummy param #
			dosDrr.writeShort(0); //dummy param #
			dosDrr.writeShort(0); //dummy param #
			dosDrr.writeShort(sizeX); //first raw parameter
			dosDrr.writeShort(sizeY); //second raw parameter
			dosDrr.writeShort(0); //third raw parameter
			dosDrr.writeShort(0); //fourth parameter
			dosDrr.writeShort(sizeX);
			//parameter-scaled for us
			dosDrr.writeShort(sizeY); //same as above
			dosDrr.writeShort(0); //third parameter-scaled
			dosDrr.writeShort(0); //fourth parameter-scaled
			dosDrr.writeShort(0); //min channel 1
			dosDrr.writeShort(0); //min channel 2
			dosDrr.writeShort(0); //min channel 3
			dosDrr.writeShort(0); //min channel 4
			dosDrr.writeShort(sizeX - 1); //max channel 1
			dosDrr.writeShort((short) (Math.max((sizeY - 1), 0)));
			//max channel 2 - 0 if 1-d
			dosDrr.writeShort(0); //max channel 3
			dosDrr.writeShort(0); //max channle 4
			dosDrr.writeInt(diskOffSet); //offset in 16 bit words
			dosDrr.writeBytes("            "); //x param label
			dosDrr.writeBytes("            "); //y param label
			dosDrr.writeFloat(0.0f); //dummy calibration
			dosDrr.writeFloat(0.0f); //dummy calibration
			dosDrr.writeFloat(0.0f); //dummy calibration
			dosDrr.writeFloat(0.0f); //dummy calibration
			/* subtitle */
			dosDrr.writeBytes(util.makeLength(hist.getTitle(), 40));
			final int histDim=hist.getDimensionality();
			/* increment disk offset for .his file */
			if (histDim==1) {
				diskOffSet += 2 * sizeX;
			} else if (histDim==2) {
				diskOffSet += 2 * sizeX * sizeY;
			} else {
				throw new IOException(
						"Unrecognized histogram type [ImpExpORNL]");
			}
		}
		/* write out id numbers */
		for (int i = 0; i < allHists.size(); i++) {
			dosDrr.writeInt(((Histogram) allHists.get(i)).getNumber());
		}
		dosDrr.flush();
		dosDrr.close();
	}

	/*
     * non-javadoc: Write out the .his file.
     */
    private void writeHis(final OutputStream outputStream) throws IOException {
        final DataOutputStream dosHis = new DataOutputStream(outputStream);
        final Iterator histIterator = Histogram.getHistogramList().iterator();
        while (histIterator.hasNext()) {
            final Histogram hist = ((Histogram) histIterator.next());
            final Histogram.Type type = hist.getType();
            /* write as determined by type */
            if (type == Histogram.Type.ONE_DIM_INT) {
                writeHist1dInt(dosHis, hist);
            } else if (type == Histogram.Type.ONE_D_DOUBLE) {
                writeHist1dDouble(dosHis, hist);
            } else if (type == Histogram.Type.TWO_DIM_INT) {
                writeHist2dInt(dosHis, hist);
            } else if (type == Histogram.Type.TWO_D_DOUBLE) {
                writeHist2dDouble(dosHis, hist);
            } else {
                msgHandler
                        .errorOutln("Unrecognized histogram type [ImpExpORNL]");
            }
        }
        msgHandler.messageOut(" (" + dosHis.size() + " bytes)");
        dosHis.flush();
        dosHis.close();
    }

    private void writeHist1dInt(final DataOutputStream dosHis,
			final Histogram hist) throws IOException {
		final int[] countsInt = (int[]) hist.getCounts();
		final int len = countsInt.length;
		for (int i = 0; i < len; i++) {
			dosHis.writeInt(countsInt[i]);
		}
	}

    private void writeHist1dDouble(final DataOutputStream dosHis,
			final Histogram hist) throws IOException {
		final double[] countsDbl = (double[]) hist.getCounts();
		final int len = countsDbl.length;
		for (int i = 0; i < len; i++) {
			dosHis.writeInt((int) (countsDbl[i] + 0.5));
		}
	}

    private void writeHist2dInt(final DataOutputStream dosHis,
			final Histogram hist) throws IOException {
		final int[][] counts2dInt = (int[][]) hist.getCounts();
		final int sizeX = hist.getSizeX();
		final int sizeY = hist.getSizeY();
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				/*
				 * Next line was [j][i] which caused array out of bounds. Was
				 * the order that way for some reason?
				 */
				dosHis.writeInt(counts2dInt[i][j]);
			}
		}
	}
    
    private void writeHist2dDouble(final DataOutputStream dosHis,
			final Histogram hist) throws IOException {
        final double[][] counts2dDbl = (double[][]) hist.getCounts();
		final int sizeX = hist.getSizeX();
		final int sizeY = hist.getSizeY();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
				/*
				 * Next line was [j][i] which caused array out of bounds. Was
				 * the order that way for some reason?
				 */
                dosHis.writeInt((int) (counts2dDbl[i][j] + 0.5));
            }
        }
	}

	/*
	 * non-javadoc: Get a int from an array of byes
	 */
	private int byteArrayToInt(byte[] array, int offset) {
        final byte byte0 = array[offset];
        final byte byte1 = array[offset + 1];
        final byte byte2 = array[offset + 2];
        final byte byte3 = array[offset + 3];
        final int rval = byteOrder == ByteOrder.BIG_ENDIAN ? constructInt(
                byte0, byte1, byte2, byte3) : constructInt(byte3, byte2, byte1,
                byte0);
        return rval;
    }

	private final int constructInt(byte highest, byte high, byte low,
			byte lowest) {
		return ((highest & 0xFF) << 24) | ((high & 0xFF) << 16)
				| ((low & 0xFF) << 8) | (lowest & 0xFF);
	}

	/* non-javadoc:
	 * Get a short from an array of byes
	 */
	private short byteArrayToShort(byte[] array, int offset) {
		short rval; //return value
		if (byteOrder == ByteOrder.BIG_ENDIAN) {
			rval = (short) (((array[offset] & 0xFF) << 8) + ((array[offset + 1] & 0xFF)));
		} else { //byteOrder is LITTLE_ENDIAN
			rval = (short) (((array[offset] & 0xFF)) + ((array[offset + 1] & 0xFF) << 8));
		}
		return rval;
	}

	private int readInt(DataInput dataInput) throws IOException {
		final byte[] rval = new byte[4];
		dataInput.readFully(rval);
		return byteArrayToInt(rval, 0);
	}

	private short readShort(DataInput dataInput) throws IOException {
	    final byte [] rval=new byte[2];
		dataInput.readFully(rval);
		return byteArrayToShort(rval, 0);
	}

	public boolean canExport() {
		return true;
	}

	boolean batchExportAllowed() {
		return false;
	}

	/**
	 * Opens a file with a specified dialog box title bar and file extension. It
	 * is usually called by <code>openFile</code> in subclasses of
	 * <code>ImpExp</code>.
	 * 
	 * @param msg
	 *            text to go on title bar of dialog box
	 * @return whether file was successfully read
	 */
	protected boolean openFile(final File file, final String msg) {
		File inFile = file;
		boolean rval = false; //default return value
		try {
			if (file == null) {
				inFile = getFileOpen(msg);
			}
			if (inFile != null) { // if Open file was not canceled
				setLastFile(inFile);
				final FileUtilities fileUtil = FileUtilities.getInstance();
				final File drrFile = (inFile.getName().endsWith("his")) ? new File(
						inFile.getParent(), fileUtil.changeExtension(inFile
								.getName(), "drr", FileUtilities.FORCE))
						: inFile;
				final FileInputStream inStream = new FileInputStream(drrFile);
				final BufferedInputStream inBuffStream = new BufferedInputStream(
						inStream, BUFFER_SIZE);
				if (msgHandler != null) {
					msgHandler.messageOut(msg + " " + getFileName(inFile),
							MessageHandler.NEW);
				}
				/* implementing class implement following method */
				readData(inBuffStream);
				if (msgHandler != null) {
					msgHandler.messageOut(" done!", MessageHandler.END);
				}
				inBuffStream.close();
				rval = true;
			}
		} catch (IOException ioe) {
			msgHandler.errorOutln("Problem handling file \"" + inFile.getPath()
					+ "\": " + ioe.getMessage());
		} catch (ImpExpException iee) {
			msgHandler.errorOutln("Problem while importing or exporting: "
					+ iee.getMessage());
		}
		return rval;
	}

}