package jam.io;

import jam.data.HistDouble1D;
import jam.data.HistDouble2D;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Histogram;
import jam.ui.ExtensionFileFilter;
import jam.util.FileUtilities;
import jam.util.NumberUtilities;
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
import java.util.List;
import java.util.logging.Level;

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
public class ImpExpORNL extends AbstractImpExp {// NOPMD

	/**
	 * sequence of ASCII encoded characters to begin <code>drr</code> file.
	 */
	private static final String SIGNATURE = "HHIRFDIR0001";

	private transient ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

	private static final NumberUtilities NUM_UTIL = NumberUtilities
			.getInstance();

	private transient int totalHist; // number of histograms

	private transient int numHalfWords;

	/* Histogram info in Drr file for each histogram */
	private transient int[] dim; // Histogram dimensionality

	private transient int[] chSize; // half words per channel

	private transient int[] lenParScal1; // Length scaled parameters

	private transient int[] lenParScal2;

	private transient int[] offSet;

	private transient String[] titleDrr; // title 40 bytes

	private transient int[] iDnumber; // ID list

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
	public boolean openFile(final File file) throws ImpExpException {
		return openFile(file, "Import ORNL file ");
	}

	/**
	 * Write out a particular histogram.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	public void saveFile(final Histogram hist) throws ImpExpException {
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
			Histogram.clearList(); // clear current list of histograms
			readDrr(buffin); // read the drr file
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
			throw new ImpExpException(ioe);
		}
	}

	/*
	 * non-javadoc: Read in ORNL drr file, which is the index to the his file.
	 */
	private void readDrr(final InputStream buffin) throws IOException,
			ImpExpException {
		final byte[] bsignature = new byte[SIGNATURE.length()];
		final byte[] parLabelb = new byte[12]; // paramater label
		final byte[] titleb = new byte[40]; // title
		final byte[] numHistByte = new byte[4];
		final DataInputStream disDrr = new DataInputStream(buffin);
		// read in header
		int numRead = disDrr.read(bsignature); // HHRIF signature
		if (numRead != bsignature.length) {
			throw new ImpExpException(
					"Unable to read in signature. Wrong # of characters: "
							+ numRead);
		}
		final String signature = String.valueOf(bsignature);
		if (!(signature.equals(SIGNATURE))) {
			throw new ImpExpException("Incorrect header, expected '"
					+ SIGNATURE + "', but got '" + signature + "'.");
		}
		numRead = disDrr.read(numHistByte); // number of histograms
		if (numRead < 0) {
			throw new ImpExpException("Unexpectedly reached end of file.");
		}
		byteOrder = ByteOrder.nativeOrder(); // assume file was created
		// locally
		final StringBuffer msg = new StringBuffer(40);
		msg.append("Native byte order: ");
		msg.append(byteOrder).append(", ");// NOPMD
		getCorrectByteOrder(numHistByte);
		msg.append("file byte order: ").append(byteOrder);
		LOGGER.info(msg.toString());
		totalHist = NUM_UTIL.bytesToInt(numHistByte, 0, byteOrder); // number of
		// histograms
		numHalfWords = readInt(disDrr); // total number of 16 bit words
		readIgnoredSection(disDrr);
		final byte bChilText[] = new byte[80]; // chill file text;
		numRead = disDrr.read(bChilText); // ASCII text from CHIL file
		if (numRead < bChilText.length) {
			throw new ImpExpException(
					"Couldn't read 80 characters expected in CHIL section.");
		}
		readDrrInfo(parLabelb, titleb, disDrr);
		/* read in id list */
		for (int i = 0; i < totalHist; i++) {
			iDnumber[i] = readInt(disDrr); // Id number
		}
		disDrr.close();
	}

	/**
	 * @param parLabelb
	 * @param titleb
	 * @param disDrr
	 * @throws IOException
	 */
	private void readDrrInfo(final byte[] parLabelb, final byte[] titleb,
			final DataInputStream disDrr) throws IOException {
		/* Histogram info in Drr file */
		dim = new int[totalHist]; // Histogram dimensionality
		chSize = new int[totalHist]; // half words per channel
		final int[] param1 = new int[totalHist]; // Histogram parameter
		final int[] param2 = new int[totalHist];
		final int[] param3 = new int[totalHist];
		final int[] param4 = new int[totalHist];
		final int[] lenParRaw1 = new int[totalHist]; // Length raw parameters
		final int[] lenParRaw2 = new int[totalHist];
		final int[] lenParRaw3 = new int[totalHist];
		final int[] lenParRaw4 = new int[totalHist];
		lenParScal1 = new int[totalHist]; // Length scaled parameters
		lenParScal2 = new int[totalHist];
		final int[] lenParScal3 = new int[totalHist];
		final int[] lenParScal4 = new int[totalHist];
		final int[] minCh1 = new int[totalHist]; // Min channels
		final int[] minCh2 = new int[totalHist];
		final int[] minCh3 = new int[totalHist];
		final int[] minCh4 = new int[totalHist];
		final int[] maxCh1 = new int[totalHist]; // Max channels
		final int[] maxCh2 = new int[totalHist];
		final int[] maxCh3 = new int[totalHist];
		final int[] maxCh4 = new int[totalHist];
		offSet = new int[totalHist];
		final String[] parLabelX = new String[totalHist];
		final String[] parLabelY = new String[totalHist];
		final float[] cal1 = new float[totalHist]; // Calibration constants
		final float[] cal2 = new float[totalHist];
		final float[] cal3 = new float[totalHist];
		final float[] cal4 = new float[totalHist];
		titleDrr = new String[totalHist]; // title 40 bytes
		/* ID list */
		iDnumber = new int[totalHist];
		/* loop for all histograms reading directory entries */
		for (int i = 0; i < totalHist; i++) {
			dim[i] = readShort(disDrr); // Histogram dimensionality
			chSize[i] = readShort(disDrr); // half-words per channel
			param1[i] = readShort(disDrr); // dummy param #
			param2[i] = readShort(disDrr); // dummy param #
			param3[i] = readShort(disDrr); // dummy param #
			param4[i] = readShort(disDrr); // dummy param #
			lenParRaw1[i] = readShort(disDrr); // length Raw parameter
			lenParRaw2[i] = readShort(disDrr); // length Raw parameter
			lenParRaw3[i] = readShort(disDrr); // length Raw parameter
			lenParRaw4[i] = readShort(disDrr); // length Raw parameter
			lenParScal1[i] = readShort(disDrr);
			/* length Scaled parameters */
			lenParScal2[i] = readShort(disDrr);
			lenParScal3[i] = readShort(disDrr);
			lenParScal4[i] = readShort(disDrr);
			minCh1[i] = readShort(disDrr); // min channel 1
			minCh2[i] = readShort(disDrr); // min channel 2
			minCh3[i] = readShort(disDrr); // min channel 3
			minCh4[i] = readShort(disDrr); // min channel 4
			maxCh1[i] = readShort(disDrr); // max channel 1
			maxCh2[i] = readShort(disDrr); // max channel 2
			maxCh3[i] = readShort(disDrr); // max channel 3
			maxCh4[i] = readShort(disDrr); // max channle 4
			offSet[i] = readInt(disDrr); // offset in 16 bit words
			int numRead = disDrr.read(parLabelb); // x param label
			if (numRead < parLabelb.length) {
				throw new IOException(
						"Wasn't able to read expected number of bytes for parameter label.");
			}
			parLabelX[i] = String.valueOf(parLabelb);
			numRead = disDrr.read(parLabelb); // y param label
			if (numRead < parLabelb.length) {
				throw new IOException(
						"Wasn't able to read expected number of bytes for parameter label.");
			}
			parLabelY[i] = String.valueOf(parLabelb);
			cal1[i] = disDrr.readFloat(); // calibaration const
			cal2[i] = disDrr.readFloat(); // calibaration const
			cal3[i] = disDrr.readFloat(); // calibaration const
			cal4[i] = disDrr.readFloat(); // calibaration const
			numRead = disDrr.read(titleb); // sub-Title
			if (numRead < titleb.length) {
				throw new IOException(
						"Wasn't able to read expected number of bytes for title.");
			}
			titleDrr[i] = String.valueOf(titleb);
		}
	}

	/**
	 * @param disDrr
	 * @throws IOException
	 */
	private void readIgnoredSection(final DataInputStream disDrr)
			throws IOException {
		readInt(disDrr); // space nothing defined
		readInt(disDrr); // year
		readInt(disDrr); // month
		readInt(disDrr); // day of month
		readInt(disDrr); // hour
		readInt(disDrr); // minutes
		readInt(disDrr); // seconds
	}

	/**
	 * @param numHistByte
	 */
	private void getCorrectByteOrder(final byte[] numHistByte) {
		if (!isCorrectByteOrder(NUM_UTIL.bytesToInt(numHistByte, 0, byteOrder))) {
			if (byteOrder == ByteOrder.BIG_ENDIAN) {
				byteOrder = ByteOrder.LITTLE_ENDIAN;
			} else {
				byteOrder = ByteOrder.BIG_ENDIAN;
			}
		}
	}

	private boolean isCorrectByteOrder(final int numHists) {
		return numHists >= 0 && numHists <= 8000;
	}

	/*
	 * non-javadoc: Read in a histogram.
	 */
	private void readHist(final RandomAccessFile fileHis, final int index)
			throws IOException {
		/* copy to histogram variables */
		final String name = titleDrr[index].trim();
		final int number = iDnumber[index];
		final int type = dim[index];
		final int wordCh = chSize[index];
		final int sizeX = lenParScal1[index];
		final int sizeY = lenParScal2[index];
		if (type == 2) { // Read in 2D histogram
			read2dHistogram(fileHis, index, name, number, wordCh, sizeX, sizeY);
		} else { // Read in 1D Histogram
			read1dHistogram(fileHis, index, name, number, wordCh, sizeX);
		}
	}

	/**
	 * @param fileHis
	 * @param index
	 * @param name
	 * @param number
	 * @param wordCh
	 * @param sizeX
	 * @throws IOException
	 */
	private void read1dHistogram(final RandomAccessFile fileHis,
			final int index, final String name, final int number,
			final int wordCh, final int sizeX) throws IOException {
		int[] counts = new int[sizeX];
		fileHis.seek((long) offSet[index] * 2);
		final int bytesToRead = sizeX * 4;
		final byte[] inBuffer = new byte[bytesToRead];
		final int numRead = fileHis.read(inBuffer); // read in byte array
		if (numRead < bytesToRead) {
			throw new IllegalStateException("Expected " + bytesToRead
					+ " bytes, but only got " + numRead);
		}
		if (wordCh == 2) { // four byte data
			int offset = 0;
			for (int i = 0; i < sizeX; i++) {
				counts[i] = NUM_UTIL.bytesToInt(inBuffer, offset, byteOrder);
				offset += 4;
			}
		} else if (wordCh == 1) { // two byte data
			int offset = 0;
			for (int i = 0; i < sizeX; i++) {
				counts[i] = NUM_UTIL.bytesToShort(inBuffer, offset, byteOrder);
				offset += 2;
			}
		} else { // unable to handle data type
			throw new IOException("File uses " + wordCh
					+ " words/channel, which can't be read.");
		}
		final Histogram hist = importGroup.createHistogram(counts, name);
		hist.setNumber(number);
	}

	/**
	 * @param fileHis
	 * @param index
	 * @param name
	 * @param number
	 * @param wordCh
	 * @param sizeX
	 * @param sizeY
	 * @throws IOException
	 */
	private void read2dHistogram(final RandomAccessFile fileHis,
			final int index, final String name, final int number,
			final int wordCh, final int sizeX, final int sizeY)
			throws IOException {
		int[][] counts2d = new int[sizeX][sizeY];
		fileHis.seek((long) offSet[index] * 2);
		final int bytesToRead = sizeX * sizeY * 4;
		final byte[] inBuffer = new byte[bytesToRead];
		final int numRead = fileHis.read(inBuffer); // read in byte array
		if (numRead < bytesToRead) {
			throw new IllegalStateException("Expected " + bytesToRead
					+ " bytes, but only got " + numRead);
		}
		if (wordCh == 2) { // four byte data
			int offset = 0;
			for (int j = 0; j < sizeY; j++) {
				for (int i = 0; i < sizeX; i++) {
					counts2d[i][j] = NUM_UTIL.bytesToInt(inBuffer, offset,
							byteOrder);
					offset += 4;
				}
			}
		} else if (wordCh == 1) { // two byte data
			int offset = 0;
			for (int j = 0; j < sizeY; j++) {
				for (int i = 0; i < sizeX; i++) {
					counts2d[i][j] = NUM_UTIL.bytesToShort(inBuffer, offset,
							byteOrder);
					offset += 2;
				}
			}
		} else { // not able to handle data
			throw new IOException("File uses " + wordCh
					+ " words/channel, which I don't know how to read.");
		}
		final Histogram hist = importGroup.createHistogram(counts2d, name);
		hist.setNumber(number);
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

			final File fileDRR = new File(parent, fileNameDRR);
			final FileOutputStream fosDRR = new FileOutputStream(fileDRR);
			final BufferedOutputStream buffoutDRR = new BufferedOutputStream(
					fosDRR);
			LOGGER.info("Writing to " + parent);
			LOGGER.info("Writing " + fileDRR.getName());
			writeDrr(buffoutDRR); // write out drr file
			LOGGER.info("Writing " + fileHis.getName());
			writeHis(buffoutHis); // write out his file
		} catch (IOException ioe) {
			throw new ImpExpException(ioe);
		}
	}

	/*
	 * non-javadoc: write out a ORNL drr file
	 */
	private void writeDrr(final OutputStream buffout) throws IOException {
		final StringUtilities util = StringUtilities.getInstance();
		int diskOffSet = 0;
		final DataOutputStream dosDrr = new DataOutputStream(buffout);
		final List<Histogram> allHists = Histogram.getHistogramList();
		/* number of histograms */
		totalHist = Histogram.getHistogramList().size(); // number of
		// histograms
		/*
		 * total number of 1/2 words need in file size of file needed? in 16 bit
		 * words
		 */
		numHalfWords = 0;
		for (int i = 0; i < allHists.size(); i++) {
			final Histogram hist = allHists.get(i);
			final int sizeX = hist.getSizeX();
			final int sizeY = hist.getSizeY(); // will be zero for 1-d
			final int histDim = hist.getDimensionality();
			if (histDim == 1) {
				numHalfWords = numHalfWords + 2 * sizeX;
			} else if (histDim == 2) {
				numHalfWords = numHalfWords + 2 * sizeX * sizeY;
			} else {
				throw new IOException(
						"Unrecognized histogram type [ImpExpORNL]");
			}
		}
		/* write header */
		dosDrr.writeBytes(SIGNATURE); // HHRIF signature, ASCII encoded
		dosDrr.writeInt(totalHist); // number of histograms
		dosDrr.writeInt(numHalfWords); // total number of 16 bit words
		dosDrr.writeInt(0); // space nothing defined
		final Calendar calendar = Calendar.getInstance();
		dosDrr.writeInt(calendar.get(Calendar.YEAR)); // date year (date and
		// time)
		dosDrr.writeInt(calendar.get(Calendar.MONTH + 1)); // month
		dosDrr.writeInt(calendar.get(Calendar.DAY_OF_MONTH)); // date day
		dosDrr.writeInt(calendar.get(Calendar.HOUR_OF_DAY)); // time
		dosDrr.writeInt(calendar.get(Calendar.MINUTE)); // time
		dosDrr.writeInt(calendar.get(Calendar.SECOND)); // time
		dosDrr.writeBytes(util.makeLength("File Created by Jam", 80));
		/* text from chill file */
		for (Histogram hist : allHists) {
			final short sizeX = (short) (hist.getSizeX());
			final short sizeY = (short) (hist.getSizeY()); // will be zero for
			// 1-d
			// use data output stream name only 15 char long title 50 char long
			dosDrr.writeShort((short) (hist.getDimensionality()));
			dosDrr.writeShort(2); // half-words per channel
			dosDrr.writeShort(0); // dummy param #
			dosDrr.writeShort(0); // dummy param #
			dosDrr.writeShort(0); // dummy param #
			dosDrr.writeShort(0); // dummy param #
			dosDrr.writeShort(sizeX); // first raw parameter
			dosDrr.writeShort(sizeY); // second raw parameter
			dosDrr.writeShort(0); // third raw parameter
			dosDrr.writeShort(0); // fourth parameter
			dosDrr.writeShort(sizeX);
			// parameter-scaled for us
			dosDrr.writeShort(sizeY); // same as above
			dosDrr.writeShort(0); // third parameter-scaled
			dosDrr.writeShort(0); // fourth parameter-scaled
			dosDrr.writeShort(0); // min channel 1
			dosDrr.writeShort(0); // min channel 2
			dosDrr.writeShort(0); // min channel 3
			dosDrr.writeShort(0); // min channel 4
			dosDrr.writeShort(sizeX - 1); // max channel 1
			dosDrr.writeShort((short) (Math.max((sizeY - 1), 0)));
			// max channel 2 - 0 if 1-d
			dosDrr.writeShort(0); // max channel 3
			dosDrr.writeShort(0); // max channle 4
			dosDrr.writeInt(diskOffSet); // offset in 16 bit words
			dosDrr.writeBytes("            "); // x param label
			dosDrr.writeBytes("            "); // y param label
			dosDrr.writeFloat(0.0f); // dummy calibration
			dosDrr.writeFloat(0.0f); // dummy calibration
			dosDrr.writeFloat(0.0f); // dummy calibration
			dosDrr.writeFloat(0.0f); // dummy calibration
			/* subtitle */
			dosDrr.writeBytes(util.makeLength(hist.getTitle(), 40));
			final int histDim = hist.getDimensionality();
			/* increment disk offset for .his file */
			if (histDim == 1) {
				diskOffSet += 2 * sizeX;
			} else if (histDim == 2) {
				diskOffSet += 2 * sizeX * sizeY;
			} else {
				throw new IOException(
						"Unrecognized histogram type [ImpExpORNL]");
			}
		}
		/* write out id numbers */
		for (Histogram hist : allHists) {
			dosDrr.writeInt(hist.getNumber());
		}
		dosDrr.flush();
		dosDrr.close();
	}

	/*
	 * non-javadoc: Write out the .his file.
	 */
	private void writeHis(final OutputStream outputStream) throws IOException {
		final DataOutputStream dosHis = new DataOutputStream(outputStream);
		for (Histogram hist : Histogram.getHistogramList()) {
			final Histogram.Type type = hist.getType();
			/* write as determined by type */
			if (type == Histogram.Type.ONE_DIM_INT) {
				writeHist1dInt(dosHis, (HistInt1D) hist);
			} else if (type == Histogram.Type.ONE_D_DOUBLE) {
				writeHist1dDouble(dosHis, (HistDouble1D) hist);
			} else if (type == Histogram.Type.TWO_DIM_INT) {
				writeHist2dInt(dosHis, (HistInt2D) hist);
			} else if (type == Histogram.Type.TWO_D_DOUBLE) {
				writeHist2dDouble(dosHis, (HistDouble2D) hist);
			} else {
				LOGGER.severe("Unrecognized histogram type [ImpExpORNL]");
			}
		}
		LOGGER.info("File Size: " + dosHis.size() / 1024 + " kB");
		dosHis.flush();
		dosHis.close();
	}

	private void writeHist1dInt(final DataOutputStream dosHis,
			final HistInt1D hist) throws IOException {
		final int[] countsInt = hist.getCounts();
		final int len = countsInt.length;
		for (int i = 0; i < len; i++) {
			dosHis.writeInt(countsInt[i]);
		}
	}

	private void writeHist1dDouble(final DataOutputStream dosHis,
			final HistDouble1D hist) throws IOException {
		final double[] countsDbl = hist.getCounts();
		final int len = countsDbl.length;
		for (int i = 0; i < len; i++) {
			dosHis.writeInt((int) (countsDbl[i] + 0.5));
		}
	}

	private void writeHist2dInt(final DataOutputStream dosHis,
			final HistInt2D hist) throws IOException {
		final int[][] counts2dInt = hist.getCounts();
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
			final HistDouble2D hist) throws IOException {
		final double[][] counts2dDbl = hist.getCounts();
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

	private int readInt(final DataInput dataInput) throws IOException {
		final byte[] rval = new byte[4];
		dataInput.readFully(rval);
		return NUM_UTIL.bytesToInt(rval, 0, byteOrder);
	}

	private short readShort(final DataInput dataInput) throws IOException {
		final byte[] rval = new byte[2];
		dataInput.readFully(rval);
		return NUM_UTIL.bytesToShort(rval, 0, byteOrder);
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
		boolean rval = false; // default return value
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
				InputStream inBuffStream = null;
				try {
					inBuffStream = new BufferedInputStream(inStream,
							BUFFER_SIZE);
					if (!silent) {
						LOGGER.info(msg + " " + getFileName(inFile));
					}
					/* implementing class implement following method */
					readData(inBuffStream);
					if (!silent) {
						LOGGER.info("File import done.");
					}
				} finally {
					if (inBuffStream != null) {
						inBuffStream.close();
					}
				}
				rval = true;
			}
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Problem handling file \""
					+ inFile.getPath() + "\": " + ioe.getMessage(), ioe);
		} catch (ImpExpException iee) {
			LOGGER.log(Level.SEVERE, "Problem while importing or exporting: "
					+ iee.getMessage(), iee);
		}
		return rval;
	}

}