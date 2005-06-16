package jam.io;

import jam.data.AbstractHist1D;
import jam.data.DataBase;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.data.func.AbstractCalibrationFunction;
import jam.data.func.PolynomialFunction;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.filechooser.FileFilter;

/**
 * Imports and Exports Histograms files using the XSYS format. XSYS is a data
 * Acquistion program written at TUNL, and is used at NPL in Seattle. Only
 * import implement as of June 98. A page for VMS is 128x4=512 bytes long.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
public class ImpExpXSYS extends AbstractImpExp implements XsysHeader {

	int[] buffer = new int[L_BUFFER];

	// XSYS header information
	String header;

	int runNumber;

	String runTitle;

	int[] scalers = new int[NUMBER_SCALERS];

	String[] scalerTitles = new String[NUMBER_SCALERS];

	// XSYS dir information for each data area
	int areaNumber;

	String areaName;

	int areaDataType;

	int areaLengthLongWords;

	int areaLengthPage; // length of area in pages 128 words?

	char[] areaNameChar = new char[L_AREA_NAME];

	int areaSizeX;

	int areaSizeY;

	int calibFlag;

	int[] calibCoef = new int[3];

	int mbdChan;

	// arrays for counts of spectra
	int[] counts;

	double[] countsDble;

	int[][] counts2d;

	boolean firstHeader;

	private static final ExtensionFileFilter FILTER = new ExtensionFileFilter(
			"dat", "TUNL's XSYS");

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
	 * Prompt for and open a file.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	public boolean openFile(File f) throws ImpExpException {
		return openFile(f, "Import XSYS file ");
	}

	/**
	 * Still needs to be implemented. This would write out a XSYS format file.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	public void saveFile(Histogram hist) {
		throw new UnsupportedOperationException("Still not implemented.");
	}

	/**
	 * Reads in a XSYS data file
	 * 
	 * @exception ImpExpException
	 *                thrown for general problems importing this format
	 */
	public void readData(InputStream buffin) throws ImpExpException {
		final DataInputStream dis = new DataInputStream(buffin);
		firstHeader = true;
		DataBase.getInstance().clearAllLists();// clear the data base
		Broadcaster broadcaster = Broadcaster.getSingletonInstance();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);

		try {
			// read in data until end of file
			while (unPackHeaderXSYS(buffin)) {
				// histogram 1d int 4 words
				if (areaDataType == XSYS1DI4) {
					String areaTitle = "" + areaNumber + "  " + areaName + " ";
					counts = unPackData1d(dis, areaSizeX, areaLengthPage);
					AbstractHist1D hist = (AbstractHist1D) Histogram
							.createHistogram(importGroup, counts, areaName,
									areaTitle);
					hist.setNumber(areaNumber);

					// calibrate histogram if flag set
					if (calibFlag == CALIB_ENERGY) {
						calibHist(calibCoef, hist);
					}
					if (msgHandler != null)
						msgHandler.messageOut(" .");
					// dot indicating a spectrum read

					// histogram 2d int 4 words
				} else if (areaDataType == XSYS2DI4) {
					String areaTitle = areaNumber + "  " + areaName + " ";
					counts2d = unPackData2d(dis, areaSizeX, areaSizeY,
							areaLengthPage);
					Histogram hist = Histogram.createHistogram(importGroup,
							counts2d, areaName, areaTitle);
					hist.setNumber(areaNumber);
					if (msgHandler != null) {
						msgHandler.messageOut(" .");
					}
				} else if (areaDataType == XSYS1DR4) {
					unPackUnknown(buffin, areaLengthPage);
					if (msgHandler != null) {
						msgHandler.messageOut(" X");// cross indicating a
													// spectrum NOT read
					}
				} else if (areaDataType == XSYSEVAL) {
					unPackEVAL(buffin, areaLengthPage);
				} else {
					unPackUnknown(buffin, areaLengthPage);
					if (msgHandler != null)
						msgHandler.messageOut("X");
				}
			}
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}

	/**
	 * Write the specified histogram out in XSYS format. Not currently
	 * implemented.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	protected void writeHist(OutputStream outStream, Histogram hist)
			throws ImpExpException {
		/* someday maybe if someone asks */
	}

	/*
	 * non-javadoc: Unpacks a Xsys histogram header.
	 * 
	 * @return true if it read a header, false if end of file found
	 */
	private boolean unPackHeaderXSYS(InputStream buffin) throws IOException,
			ImpExpException {
		boolean endOfFile = false;
		/*
		 * read in full buffer, because there is stuff in buffer we want to skip
		 */
		readBuffers: for (int i = 0; i < L_BUFFER; i++) {
			if ((buffer[i] = buffin.read()) == (-1)) {
				endOfFile = true;
				break readBuffers;
			}
		}
		if (!endOfFile) {
			/* read header word */
			header = bufferToString(buffer, P_HEADER, L_INT);
			if (!(header.equals(XSYSHEADER))) {
				throw new ImpExpException(
						"Not an XSYS file, no SPEC key word [ImpExpXsys]");
			}
			/* read in header info */
			runNumber = bufferToBigEndian(buffer, P_RUN_NUMBER);
			runTitle = bufferToString(buffer, P_TITLE, L_TITLE);
			areaNumber = bufferToBigEndian(buffer, P_AREA_NUMBER);
			areaDataType = bufferToBigEndian(buffer, P_AREA_DATA_TYPE);
			areaName = bufferToString(buffer, P_AREA_NAME, L_AREA_NAME);
			areaLengthPage = bufferToBigEndian(buffer, P_AREA_LENGTH_PAGE);
			areaSizeX = bufferToBigEndian(buffer, P_AREA_SIZE_X);
			areaSizeY = bufferToBigEndian(buffer, P_AREA_SIZE_Y);
			calibFlag = bufferToBigEndian(buffer, P_AREA_CALIB_FLAG);
			mbdChan = bufferToBigEndian(buffer, P_AREA_MBD_CHAN);
			if (calibFlag == CALIB_ENERGY) {
				for (int i = 0; i < L_AREA_CALIB_COEF; i++) {
					calibCoef[i] = bufferToBigEndian(buffer, P_AREA_CALIB_COEF
							+ i * L_INT);
				}
			}
			/* if header before first histogram */
			if (firstHeader) {
				if (msgHandler != null)
					msgHandler.messageOutln("  Run number: " + runNumber
							+ " Title: " + runTitle.trim() + " ");
				/* read in scalers values */
				for (int i = 0; i < NUMBER_SCALERS; i++) {
					scalers[i] = bufferToBigEndian(buffer, P_SCALERS + L_INT
							* i);
				}
				/* read in scaler titles */
				for (int i = 0; i < NUMBER_SCALERS; i++) {
					scalerTitles[i] = bufferToString(buffer, P_SCALER_TITLES
							+ L_SCALER_TITLES * i, L_SCALER_TITLES);
					new Scaler(importGroup, scalerTitles[i], i);
				}
			}
			firstHeader = false;
		}
		return !endOfFile;
	}

	/**
	 * Unpacks a XSYS histogram 1d type INTEGER.
	 * 
	 * @param buffin
	 *            inputdata stream
	 * @param length
	 *            size of histogram
	 * @param pages
	 *            length of area in pages a page is XSYS_BUFFER_SIZE long
	 * @return histogram
	 */
	private int[] unPackData1d(DataInputStream buffin, int length, int pages)
			throws IOException {
		int numberLongWords;
		int[] rval = new int[length];
		byte[] tempBuff; // array to hold byte data
		numberLongWords = pages * XSYS_BUFFER_SIZE;
		tempBuff = new byte[numberLongWords * L_INT];
		/* read in data to a tempory buffer */
		buffin.readFully(tempBuff, 0, numberLongWords * L_INT);
		for (int i = 0; i < length; i++) {
			/* litte endian read from buffer */
			rval[i] = bufferToBigEndian(tempBuff, i * L_INT);
		}
		return rval;
	}

	/*
	 * non-javadoc: Unpack the data of a XSYS 2d spectum type INTEGER We make a
	 * square specturm using the larger of the two dimension x and y
	 */
	private int[][] unPackData2d(DataInputStream buffin, int lengthX,
			int lengthY, int pages) throws IOException {
		int numberLongWords;
		int channelX = 0;
		int channelY = 0;
		int areaSize;
		int[][] rval;
		byte tempBuff[];
		int i;

		numberLongWords = pages * XSYS_BUFFER_SIZE;
		areaSize = Math.max(lengthX, lengthY); // maximum of 2 values
		rval = new int[areaSize][areaSize];
		final int bytesToRead = numberLongWords * L_INT;
		tempBuff = new byte[bytesToRead];
		// read in data to a tempory buffer
		buffin.readFully(tempBuff, 0, bytesToRead);
		i = 0;
		for (channelX = 0; channelX < lengthX; channelX++) {
			for (channelY = 0; channelY < lengthY; channelY++) {
				// little endian read from buffer
				rval[channelX][channelY] = bufferToBigEndian(tempBuff, i
						* L_INT);
				i++;
			}
		}
		return rval;
	}

	/*
	 * non-javadoc: Unpack unknown data area Just skips over it.
	 */
	private void unPackUnknown(InputStream buffin, int areaLengthpage)
			throws IOException {
		for (int i = 0; i < areaLengthpage * L_BUFFER; i++) {
			buffin.read();
		}
	}

	/*
	 * non-javadoc: Unpack the sort routine used For now just skips over it.
	 */
	private void unPackEVAL(InputStream buffin, int areaLengthpage)
			throws IOException {
		for (int i = 0; i < areaLengthpage * L_BUFFER; i++) {
			buffin.read();
		}
	}

	/*
	 * non-javadoc: calibrate a historam
	 */
	private void calibHist(int[] coeffs, AbstractHist1D hist) {
		final double[] calibDble = new double[3];
		calibDble[0] = coeffs[0] * 0.0001;
		calibDble[1] = coeffs[1] * 0.000001;
		calibDble[2] = coeffs[2] * 0.00000001;
		final AbstractCalibrationFunction calibFunc = new PolynomialFunction(3);
		calibFunc.setCoeff(calibDble);
		hist.setCalibration(calibFunc);
	}

	/*
	 * non-javadoc: Extract an int from a array of bytes in little endian order,
	 * 
	 * little endian byte order 4321 big endian byte order 1234 @param inByte
	 * array of ints in little endian order @param postition position in array
	 * of int to extract
	 */
	private int bufferToBigEndian(byte[] inByte, int position) {
		int outInt;
		// bytes sign extended in converting to in
		// so we need to mask the higher order bits
		outInt = ((inByte[position + 0] & 0xff) << 0)
				| ((inByte[position + 1] & 0xff) << 8)
				| ((inByte[position + 2] & 0xff) << 16)
				| ((inByte[position + 3] & 0xff) << 24);

		return outInt;
	}

	/*
	 * non-javadoc: Converts a array of integers which represent bytes (values
	 * up to 256 in little endian order to big endian int little endian byte
	 * order 4321 big endian byte order 1234
	 * 
	 * @param inByte array of int each representing a byte @param position
	 * position in array of bytes
	 */
	private int bufferToBigEndian(int[] inByte, int position) {

		int outInt;
		// for unsigned integers clear not in loop
		outInt = (inByte[position + 0] << 0) | (inByte[position + 1] << 8)
				| (inByte[position + 2] << 16) | (inByte[position + 3] << 24);

		return outInt;
	}

	/*
	 * non-javadoc: to turn a byte array into a string
	 */
	private String bufferToString(int[] inByteArray, int position, int length) {
		String outString;
		char[] charData = new char[length];
		for (int i = 0; i <= length - 1; i++) {
			charData[i] = (char) inByteArray[position + i];
		}
		outString = String.valueOf(charData);
		return (outString);

	}

	/**
	 * Will set to true when somebody asks for it and it gets implemented.
	 * 
	 * @return false
	 */
	public boolean canExport() {
		return false;
	}

	boolean batchExportAllowed() {
		return false;
	}
}
