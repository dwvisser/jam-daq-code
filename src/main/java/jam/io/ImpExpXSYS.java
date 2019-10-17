package jam.io;//NOPMD

import com.google.inject.Inject;
import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.DataBase;
import jam.data.Factory;
import jam.data.func.AbstractCalibrationFunction;
import jam.data.func.PolynomialFunction;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.ui.ExtensionFileFilter;
import jam.util.NumberUtilities;

import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.*;
import java.nio.ByteOrder;

import static jam.io.XsysHeader.*;

/**
 * Imports and Exports Histograms files using the XSYS format. XSYS is a data
 * Acquistion program written at TUNL, and is used at NPL in Seattle. Only
 * import implement as of June 98. A page for VMS is 128x4=512 bytes long.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
public class ImpExpXSYS extends AbstractImpExp {// NOPMD

	// XSYS dir information for each data area
	private transient int areaNumber;

	private transient String areaName;

	private transient int areaDataType;

	private transient int areaLengthPage; // length of area in pages 128

	// words?

	private transient int areaSizeX;

	private transient int areaSizeY;

	private transient int calibFlag;

	private transient final int[] calibCoef = new int[3];

	// arrays for counts of spectra

	private transient boolean firstHeader;

	private transient final Broadcaster broadcaster;

	private transient final NumberUtilities numberUtilities;

	private static final ExtensionFileFilter FILTER = new ExtensionFileFilter(
			"dat", "TUNL's XSYS");

	/**
	 * @param frame
	 *            application frame
	 * @param broadcaster
	 *            broadcasts state changes
	 * @param numberUtilities
	 *            number utility object
	 */
	@Inject
	public ImpExpXSYS(final Frame frame, final Broadcaster broadcaster,
			final NumberUtilities numberUtilities) {
		super(frame);
		this.broadcaster = broadcaster;
		this.numberUtilities = numberUtilities;
	}

	@Override
	protected FileFilter getFileFilter() {
		return FILTER;
	}

	@Override
	protected String getDefaultExtension() {
		return FILTER.getExtension(0);
	}

	@Override
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
	@Override
	public boolean openFile(final File file) throws ImpExpException {
		return openFile(file, "Import XSYS file ");
	}

	/**
	 * Still needs to be implemented. This would write out a XSYS format file.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void saveFile(final AbstractHistogram hist) {
		throw new UnsupportedOperationException("Still not implemented.");
	}

	/**
	 * Reads in a XSYS data file
	 * 
	 * @exception ImpExpException
	 *                thrown for general problems importing this format
	 */
	@Override
	public void readData(final InputStream buffin) throws ImpExpException {
		final DataInputStream dis = new DataInputStream(buffin);
		firstHeader = true;
		DataBase.getInstance().clearAllLists();// clear the data base
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);

		try {
			// read in data until end of file
			int specRead = 0;
			int specNotRead = 0;
			while (unPackHeaderXSYS(buffin)) {
				// histogram 1d int 4 words
				if (areaDataType == XSYS1DI4) {
					final String areaTitle = Integer.toString(areaNumber)
							+ "  " + areaName + " ";
					final int[] counts = unPackData1d(dis, areaSizeX,
							areaLengthPage);
					final AbstractHist1D hist = (AbstractHist1D) Factory
							.createHistogram(importGroup, counts, areaName,
									areaTitle);
					hist.setNumber(areaNumber);

					// calibrate histogram if flag set
					if (calibFlag == CALIB_ENERGY) {
						calibHist(calibCoef, hist);
					}
					specRead++;

					// histogram 2d int 4 words
				} else if (areaDataType == XSYS2DI4) {
					final String areaTitle = areaNumber + "  " + areaName + " ";
					final int[][] counts2d = unPackData2d(dis, areaSizeX,
							areaSizeY, areaLengthPage);
					final AbstractHistogram hist = Factory.createHistogram(
							importGroup, counts2d, areaName, areaTitle);
					hist.setNumber(areaNumber);
					specRead++;
				} else if (areaDataType == XSYS1DR4) {
					unPackUnknown(buffin, areaLengthPage);
					specNotRead++;
				} else if (areaDataType == XSYSEVAL) {
					unPackEVAL(buffin, areaLengthPage);
				} else {
					unPackUnknown(buffin, areaLengthPage);
					specNotRead++;
				}
			}
			final StringBuilder msg = new StringBuilder();
			msg.append(specRead).append(" spectra read");
			if (specNotRead > 0) {
				msg.append(", ").append(specNotRead)
						.append(" spectra not read");
			}
			msg.append('.');
			LOGGER.info(msg.toString());
		} catch (IOException ioe) {
			throw new ImpExpException(ioe);
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
	@Override
	protected void writeHist(final OutputStream outStream,
			final AbstractHistogram hist) throws ImpExpException {
		/* someday maybe if someone asks */
	}

	/*
	 * non-javadoc: Unpacks a Xsys histogram header.
	 * 
	 * @return true if it read a header, false if end of file found
	 */
	private boolean unPackHeaderXSYS(final InputStream buffin)
			throws IOException, ImpExpException {
		final int[] buffer = new int[L_BUFFER];
		final boolean endOfFile = readBuffers(buffin, buffer);
		if (!endOfFile) {
			/* read header word */
			final String header = bufferToString(buffer, P_HEADER, L_INT);
			if (!(header.equals(HEADER))) {
				throw new ImpExpException(
						"Not an XSYS file, no SPEC key word [ImpExpXsys]");
			}
			/* read in header info */
			final int runNumber = bufferToBigEndian(buffer, P_RUN_NUMBER);
			final String runTitle = bufferToString(buffer, P_TITLE, L_TITLE);
			areaNumber = bufferToBigEndian(buffer, P_AREA_NUMBER);
			areaDataType = bufferToBigEndian(buffer, P_AREA_DATA_TYPE);
			areaName = bufferToString(buffer, P_AREA_NAME, L_AREA_NAME);
			areaLengthPage = bufferToBigEndian(buffer, P_AREA_LEN_PAGE);
			areaSizeX = bufferToBigEndian(buffer, P_AREA_SIZE_X);
			areaSizeY = bufferToBigEndian(buffer, P_AREA_SIZE_Y);
			calibFlag = bufferToBigEndian(buffer, P_AREA_CALIB_FLAG);
			if (calibFlag == CALIB_ENERGY) {
				for (int i = 0; i < L_AREA_CALIB_COEF; i++) {
					calibCoef[i] = bufferToBigEndian(buffer, P_AREA_CALIB_COEF
							+ i * L_INT);
				}
			}
			/* if header before first histogram */
			if (firstHeader) {
				if (!silent) {
					LOGGER.info("  Run number: " + runNumber + " Title: "
							+ runTitle.trim() + " ");
				}

				/* read in scalers values */
				for (int i = 0; i < NUMBER_SCALERS; i++) {
					bufferToBigEndian(buffer, P_SCALERS + L_INT * i);
				}
				/* read in scaler titles */
				final String[] scalerTitles = new String[NUMBER_SCALERS];
				for (int i = 0; i < NUMBER_SCALERS; i++) {
					scalerTitles[i] = bufferToString(buffer, P_SCALER_TITLES
							+ L_SCALER_TITLES * i, L_SCALER_TITLES);
					createScaler(scalerTitles[i], i);
				}
			}
			firstHeader = false;
		}
		return !endOfFile;
	}

	private void createScaler(final String scalerTitle, final int scalerId) {
		Factory.createScaler(importGroup, scalerTitle, scalerId);
	}

	private boolean readBuffers(final InputStream buffin, final int[] buffer)
			throws IOException {
		/*
		 * read in full buffer, because there is stuff in buffer we want to skip
		 */
		boolean rval = false;
		readBuffers: for (int i = 0; i < L_BUFFER; i++) {
			buffer[i] = buffin.read();
			if (buffer[i] == -1) {
				rval = true;
				break readBuffers;
			}
		}
		return rval;
	}

	/*
	 * non-javadoc: Unpacks a XSYS histogram 1d type INTEGER.
	 * 
	 * @param buffin inputdata stream @param length size of histogram @param
	 * pages length of area in pages a page is XSYS_BUFFER_SIZE long @return
	 * histogram
	 */
	private int[] unPackData1d(final DataInputStream buffin, final int length,
			final int pages) throws IOException {
		int numberLongWords;
		int[] rval = new int[length];
		byte[] tempBuff; // array to hold byte data
		numberLongWords = pages * XSYS_BUFFER_SIZE;
		tempBuff = new byte[numberLongWords * L_INT];
		/* read in data to a tempory buffer */
		buffin.readFully(tempBuff, 0, numberLongWords * L_INT);
		for (int i = 0; i < length; i++) {
			/* litte endian read from buffer */
			rval[i] = this.numberUtilities.bytesToInt(tempBuff, i * L_INT,
					ByteOrder.LITTLE_ENDIAN);
		}
		return rval;
	}

	/*
	 * non-javadoc: Unpack the data of a XSYS 2d spectum type INTEGER We make a
	 * square specturm using the larger of the two dimension x and y
	 */
	private int[][] unPackData2d(final DataInputStream buffin,
			final int lengthX, final int lengthY, final int pages)
			throws IOException {
		int numberLongWords = pages * XSYS_BUFFER_SIZE;
		int areaSize = Math.max(lengthX, lengthY); // maximum of 2 values
		int[][] result = new int[areaSize][areaSize];
		final int bytesToRead = numberLongWords * L_INT;
		byte[] tempBuff = new byte[bytesToRead];

		// read in data to a temporary buffer
		buffin.readFully(tempBuff, 0, bytesToRead);
		int index = 0;
		for (int channelX = 0; channelX < lengthX; channelX++) {
			for (int channelY = 0; channelY < lengthY; channelY++) {
				// little endian read from buffer
				result[channelX][channelY] = this.numberUtilities.bytesToInt(
						tempBuff, index * L_INT, ByteOrder.LITTLE_ENDIAN);
				index++;
			}
		}
		return result;
	}

	/*
	 * non-javadoc: Unpack unknown data area Just skips over it.
	 */
	private void unPackUnknown(final InputStream buffin,
			final int areaLengthpage) throws IOException {
		for (int i = 0; i < areaLengthpage * L_BUFFER; i++) {
			buffin.read();
		}
	}

	/*
	 * non-javadoc: Unpack the sort routine used For now just skips over it.
	 */
	private void unPackEVAL(final InputStream buffin, final int areaLengthpage)
			throws IOException {
		for (int i = 0; i < areaLengthpage * L_BUFFER; i++) {
			buffin.read();
		}
	}

	/*
	 * non-javadoc: calibrate a historam
	 */
	private void calibHist(final int[] coeffs, final AbstractHist1D hist) {
		final double[] calibDble = new double[3];
		calibDble[0] = coeffs[0] * 0.0001;
		calibDble[1] = coeffs[1] * 0.000001;
		calibDble[2] = coeffs[2] * 0.00000001;
		final AbstractCalibrationFunction calibFunc = new PolynomialFunction(3);
		calibFunc.setCoeff(calibDble);
		hist.setCalibration(calibFunc);
	}

	/*
	 * non-javadoc: Converts a array of integers which represent bytes (values
	 * up to 256 in little endian order to big endian int little endian byte
	 * order 4321 big endian byte order 1234
	 * 
	 * @param inByte array of int each representing a byte @param position
	 * position in array of bytes
	 */
	private int bufferToBigEndian(final int[] inByte, final int position) {

		int outInt;
		// for unsigned integers clear not in loop
		outInt = (inByte[position]) | (inByte[position + 1] << 8)
				| (inByte[position + 2] << 16) | (inByte[position + 3] << 24);

		return outInt;
	}

	/*
	 * non-javadoc: to turn a byte array into a string
	 */
	private String bufferToString(final int[] inByteArray, final int position,
			final int length) {
		char[] charData = new char[length];
		for (int i = 0; i <= length - 1; i++) {
			charData[i] = (char) inByteArray[position + i];
		}
		return String.valueOf(charData);
	}

	/**
	 * Will set to true when somebody asks for it and it gets implemented.
	 * 
	 * @return false
	 */
	@Override
	public boolean canExport() {
		return false;
	}

	@Override
	protected boolean batchExportAllowed() {
		return false;
	}
}
