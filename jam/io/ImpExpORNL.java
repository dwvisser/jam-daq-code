package jam.io;
import jam.data.Histogram;
import jam.global.MessageHandler;
import jam.util.FileUtilities;
import jam.util.StringUtilities;

import java.awt.Frame;
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
import java.util.Iterator;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * Imports and exports Oak Ridge (Milner) formatted files, as used by <code>DAMM</code> and
 * <code>SORT</code>.
 * A set of histograms consists of 2 files: <UL>
 *  <LI>Data file--<code><i>filename</i>.his</code></li>
 *  <li>Directory file--<code><i>filename</i>.drr</code></li>
 * </ul>  Where <code><i>filename</i></code> is the same for both files.
 *
 * @author  Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 0.5
 */
public class ImpExpORNL extends ImpExp {

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
	 *  Stuff read in for every drr file
	 */
	private String signature;
	private int totalHist; //number of histograms
	private int totalHalfWords;
	private int temp;
	private int year;
	private int month;
	private int day;
	private String chilText;

	/* Histogram info in Drr file for each histogram */
	private int[] dim; // Histogram dimensionality 
	private int[] chSize; //half words per channel
	private int[] param1; // Histogram parameter
	private int[] param2;
	private int[] param3;
	private int[] param4;
	private int[] lenParRaw1; //Length raw parameters
	private int[] lenParRaw2;
	private int[] lenParRaw3;
	private int[] lenParRaw4;
	private int[] lenParScal1; //Length scaled parameters
	private int[] lenParScal2;
	private int[] lenParScal3;
	private int[] lenParScal4;
	private int[] minCh1; //Min channels
	private int[] minCh2;
	private int[] minCh3;
	private int[] minCh4;
	private int[] maxCh1; //Max channels 
	private int[] maxCh2;
	private int[] maxCh3;
	private int[] maxCh4;
	private int[] offSet;
	private String[] parLabelX; //x-parm label x 12 bytes
	private String[] parLabelY; //y parm label y 12 bytes		
	private float[] cal1; // Calibration constants
	private float[] cal2;
	private float[] cal3;
	private float[] cal4;
	private String[] titleDrr; //title 40 bytes

	private int[] iDnumber; //ID list

	//private byte[] tempInt = new byte[4];
	private byte[] tempShort = new byte[2];

	/**
	 * Constructor
	 */
	public ImpExpORNL(Frame frame, MessageHandler msgHandler) {
		super(frame, msgHandler);
	}

	public ImpExpORNL() {
		super();
	}

	private static final String [] exts={"his","drr"};
	private static final ExtensionFileFilter filter=new ExtensionFileFilter(exts, 
	"Oak Ridge DAMM");
	protected FileFilter getFileFilter() {
		return filter;
	}
	protected String getDefaultExtension(){
		return filter.getExtension(0);
	}

	public String getFormatDescription() {
		return filter.getDescription();
	}

	/**
	 * Open a file which was written using ORNL .drr and .his format. 
	 * Can't do the same as other ImpExp routines, since 2 files are needed.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the MessageHandler
	 */
	public boolean openFile(File f) throws ImpExpException {
		return openFile(f, "Import ORNL file ");
	}

	/**
	 * Write out a particular histogram.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the MessageHandler
	 */
	public void saveFile(Histogram hist) throws ImpExpException {
		saveFile("Export ORNL", hist);
	}

	/**
	 * Reads in a histogram from the event stream.
	 *
	 * @exception   ImpExpException	    thrown for errors
	 */
	public void readData(InputStream buffin) throws ImpExpException {
		try {
			Histogram.clearList(); //clear current list of histograms	
			readDrr(buffin); //read the drr file

			final String fileNameHis =
				FileUtilities.setExtension(
					getFileName(lastFile),
					"*.his",
					FileUtilities.FORCE);
			/* open .his file random access, read only */
			final RandomAccessFile fileHis =
				new RandomAccessFile(
					new File(lastFile.getParentFile(), fileNameHis),
					"r");
			/* read in his file and load spectra */
			for (int k = 0; k < totalHist; k++) {
				readHist(fileHis, k);
			}
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		} 
	}
	
	/** 
	 * 
	 * read in ORNL drr file
	 */
	private void readDrr(InputStream buffin)
		throws IOException, ImpExpException {

		// byte arrays need	          
		byte[] bsignature = new byte[SIGNATURE.length()];
		//header in Drr file	
		byte bChilText[] = new byte[80]; //chill file text;
		byte[] parLabelb = new byte[12]; //paramater label
		byte[] titleb = new byte[40]; //title
		byte[] totalHistByte = new byte[4];

		disDrr = new DataInputStream(buffin);

		//read in header
		disDrr.read(bsignature); //HHRIF signature
		signature = new String(bsignature);
		if (!(signature.equals(SIGNATURE))) {
			throw new ImpExpException(
				"Incorrect header, expected '"
					+ SIGNATURE
					+ "', but got '"
					+ signature
					+ "'.");
		}
		disDrr.read(totalHistByte); //number of histograms
		byteOrder = ByteOrder.nativeOrder(); //assume file was created locally
		msgHandler.messageOut(", native byte order: " + byteOrder + ", ");
		if (!isCorrectByteOrder(byteArrayToInt(totalHistByte, 0))) {
			if (byteOrder == ByteOrder.BIG_ENDIAN) {
				byteOrder = ByteOrder.LITTLE_ENDIAN;
			} else {
				byteOrder = ByteOrder.BIG_ENDIAN;
			}
		}
		msgHandler.messageOut("file byte order: " + byteOrder + ", ");
		totalHist = byteArrayToInt(totalHistByte, 0); //number of histograms
		totalHalfWords = readInt(disDrr); //total number of 16 bit words
		temp = readInt(disDrr); //space nothing defined
		year = readInt(disDrr); //date year
		month = readInt(disDrr); //date month
		day = readInt(disDrr); //date day
		temp = readInt(disDrr); //time hour
		temp = readInt(disDrr); //time minutes
		temp = readInt(disDrr); //time seconds
		disDrr.read(bChilText); //text from chill file
		chilText = new String(bChilText);
		/* Histogram info in Drr file */
		dim = new int[totalHist]; // Histogram dimensionality 
		chSize = new int[totalHist]; //half words per channel
		param1 = new int[totalHist]; // Histogram parameter
		param2 = new int[totalHist];
		param3 = new int[totalHist];
		param4 = new int[totalHist];
		lenParRaw1 = new int[totalHist]; //Length raw parameters
		lenParRaw2 = new int[totalHist];
		lenParRaw3 = new int[totalHist];
		lenParRaw4 = new int[totalHist];
		lenParScal1 = new int[totalHist]; //Length scaled parameters
		lenParScal2 = new int[totalHist];
		lenParScal3 = new int[totalHist];
		lenParScal4 = new int[totalHist];
		minCh1 = new int[totalHist]; //Min channels
		minCh2 = new int[totalHist];
		minCh3 = new int[totalHist];
		minCh4 = new int[totalHist];
		maxCh1 = new int[totalHist]; //Max channels 
		maxCh2 = new int[totalHist];
		maxCh3 = new int[totalHist];
		maxCh4 = new int[totalHist];
		offSet = new int[totalHist];
		parLabelX = new String[totalHist];
		parLabelY = new String[totalHist];
		cal1 = new float[totalHist]; // Calibration constants
		cal2 = new float[totalHist];
		cal3 = new float[totalHist];
		cal4 = new float[totalHist];
		titleDrr = new String[totalHist]; //title 40 bytes	

		//ID list
		iDnumber = new int[totalHist];

		//loop for all histograms reading directory entries
		for (int i = 0; i < totalHist; i++) {
			dim[i] = (int) readShort(disDrr); //Histogram dimensionality
			chSize[i] = (int) readShort(disDrr); //half-words per channel
			param1[i] = (int) readShort(disDrr); //dummy param #
			param2[i] = (int) readShort(disDrr); //dummy param #
			param3[i] = (int) readShort(disDrr); //dummy param #
			param4[i] = (int) readShort(disDrr); //dummy param #
			lenParRaw1[i] = (int) readShort(disDrr); //length Raw parameter 
			lenParRaw2[i] = (int) readShort(disDrr); //length Raw parameter 
			lenParRaw3[i] = (int) readShort(disDrr); //length Raw parameter 
			lenParRaw4[i] = (int) readShort(disDrr); //length Raw parameter 
			lenParScal1[i] = (int) readShort(disDrr);
			/* length Scaled parameters */
			lenParScal2[i] = (int) readShort(disDrr);
			lenParScal3[i] = (int) readShort(disDrr);
			lenParScal4[i] = (int) readShort(disDrr);
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
			parLabelX[i] = new String(parLabelb);
			disDrr.read(parLabelb); //y param label		
			parLabelY[i] = new String(parLabelb);
			cal1[i] = disDrr.readFloat(); //calibaration const
			cal2[i] = disDrr.readFloat(); //calibaration const
			cal3[i] = disDrr.readFloat(); //calibaration const
			cal4[i] = disDrr.readFloat(); //calibaration const
			disDrr.read(titleb); //sub-Title 
			titleDrr[i] = new String(titleb);
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

	/**
	 * Read in a histogram.
	 */
	private void readHist(RandomAccessFile fileHis, int k) throws IOException {
		int offset; //offset set in byte array
		int numByteToRead;
		byte[] inBuffer;

		/* copy to histogram variables */
		final String name = titleDrr[k].trim();
		int number = iDnumber[k];
		int type = dim[k];
		int wordCh = chSize[k];
		int sizeX = lenParScal1[k];
		int sizeY = lenParScal2[k];
		if (type == 2) { //Read in 2D histogram	
			int[][] counts2d = new int[sizeX][sizeY];
			fileHis.seek((long) offSet[k] * 2);
			numByteToRead = sizeX * sizeY * 4;
			inBuffer = new byte[numByteToRead];
			fileHis.read(inBuffer); //read in byte array
			if (wordCh == 2) { //four byte data 
				offset = 0;
				for (int j = 0; j < sizeY; j++) {
					for (int i = 0; i < sizeX; i++) {
						counts2d[i][j] = byteArrayToInt(inBuffer, offset);
						offset += 4;
					}
				}
			} else if (wordCh == 1) { //two byte data	
				offset = 0;
				for (int j = 0; j < sizeY; j++) {
					for (int i = 0; i < sizeX; i++) {
						counts2d[i][j] = byteArrayToShort(inBuffer, offset);
						offset += 2;
					}
				}
			} else { //not able to handle data	
				throw new IOException(
					"File uses "
						+ wordCh
						+ " words/channel, which I don't know how to read.");
			}
			final Histogram hist = new Histogram(name, name, counts2d);
			hist.setNumber(number);
			if (msgHandler != null) {
				msgHandler.messageOut(" .");
			}
		} else { //Read in 1D Histogram		
			int[] counts = new int[sizeX];
			fileHis.seek((long) offSet[k] * 2);
			numByteToRead = sizeX * 4;
			inBuffer = new byte[numByteToRead];
			fileHis.read(inBuffer); //read in byte array
			if (wordCh == 2) { //four byte data
				offset = 0;
				for (int i = 0; i < sizeX; i++) {
					counts[i] = byteArrayToInt(inBuffer, offset);
					offset += 4;
				}
			} else if (wordCh == 1) { //two byte data		
				offset = 0;
				for (int i = 0; i < sizeX; i++) {
					counts[i] = byteArrayToShort(inBuffer, offset);
					offset += 2;
				}
			} else { //unable to handle data type
				throw new IOException(
					"File uses "
						+ wordCh
						+ " words/channel, which I don't know how to read.");
			}
			final Histogram hist = new Histogram(name, name, counts);
			hist.setNumber(number);
			if (msgHandler != null) {
				msgHandler.messageOut(" .");
			}
		}
	}

	/**
	 * Writes a histogram to the file.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the msgHandler
	 */
	public void writeHist(OutputStream buffout, Histogram hist)
		throws ImpExpException {
		try {
			String fileNameHis =
				FileUtilities.setExtension(
					getLastFile().getName(),
					".his",
					FileUtilities.FORCE);
			File parent = lastFile.getParentFile();
			File fileHis = new File(parent, fileNameHis);
			FileOutputStream fosHis = new FileOutputStream(fileHis);
			BufferedOutputStream buffoutHis = new BufferedOutputStream(fosHis);
			msgHandler.messageOut("...");
			writeDrr(buffout); //write out drr file
			msgHandler.messageOut(fileHis.getName());
			writeHis(buffoutHis); //write out his file
			msgHandler.messageOut(" to " + parent + " ");
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		} 
	}

	/**
	 * write out a ORNL drr file
	 */
	private void writeDrr(OutputStream buffout) throws IOException {
		Histogram hist;

		final StringUtilities su = StringUtilities.instance();
		int diskOffSet = 0;
		DataOutputStream dosDrr = new DataOutputStream(buffout);
		List allHists = Histogram.getHistogramList();
		/* number of histograms */
		totalHist = Histogram.getHistogramList().size(); //number of histograms
		/* total number of 1/2 words need in file 
		 * size of file needed? in 16 bit words */
		totalHalfWords = 0;
		for (int i = 0; i < allHists.size(); i++) {
			hist = ((Histogram) allHists.get(i));
			if (hist.getType() == Histogram.ONE_DIM_INT) {
				totalHalfWords = totalHalfWords + 2 * hist.getSizeX();
			} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
				totalHalfWords = totalHalfWords + 2 * hist.getSizeX();
			} else if (hist.getType() == Histogram.TWO_DIM_INT) {
				totalHalfWords =
					totalHalfWords + 2 * hist.getSizeX() * hist.getSizeY();
			} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
				totalHalfWords =
					totalHalfWords + 2 * hist.getSizeX() * hist.getSizeY();
			} else {
				throw new IOException("Unrecognized histogram type [ImpExpORNL]");
			}
		}
		/* write header */
		dosDrr.writeBytes(SIGNATURE); //HHRIF signature, ASCII encoded
		dosDrr.writeInt(totalHist); //number of histograms
		dosDrr.writeInt(totalHalfWords); //total number of 16 bit words
		dosDrr.writeInt(0); //space nothing defined
		dosDrr.writeInt(98); //date year FIXME (date and time)
		dosDrr.writeInt(5); //date month
		dosDrr.writeInt(11); //date day
		dosDrr.writeInt(0); //time
		dosDrr.writeInt(0); //time
		dosDrr.writeInt(0); //time
		dosDrr.writeBytes(su.makeLength("File Created by Jam", 80));
		/* text from chill file */
		for (int i = 0; i < allHists.size(); i++) {
			hist = ((Histogram) allHists.get(i));
			short sizeX = (short) (hist.getSizeX());
			short sizeY = (short) (hist.getSizeY()); //will be zero for 1-d

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
			//parameter-scaled  for us								   
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
			dosDrr.writeBytes(su.makeLength(hist.getTitle(), 40));
			//sub-Title
			if (hist.getType() == Histogram.ONE_DIM_INT) {
				// increment disk offset for .his file
				diskOffSet = diskOffSet + 2 * sizeX;
			} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
				diskOffSet = diskOffSet + 2 * sizeX;
			} else if (hist.getType() == Histogram.TWO_DIM_INT) {
				diskOffSet = diskOffSet + 2 * sizeX * sizeY;
			} else if (hist.getType() == Histogram.TWO_DIM_DOUBLE) {
				diskOffSet = diskOffSet + 2 * sizeX * sizeY;
			} else {
				throw new IOException("Unrecognized histogram type [ImpExpORNL]");
			}
		}
		/* write out id numbers */
		for (int i = 0; i < allHists.size(); i++) {
			dosDrr.writeInt(((Histogram) allHists.get(i)).getNumber());
		}
		dosDrr.flush();
		dosDrr.close();
	}

	/**
	 * Write out the .his file.
	 */
	private void writeHis(OutputStream outputStream) throws IOException {
		DataOutputStream dosHis = new DataOutputStream(outputStream);
		Iterator allHistograms = Histogram.getHistogramList().iterator();
		while (allHistograms.hasNext()) {
			Histogram hist = ((Histogram) allHistograms.next());
			int sizeX = hist.getSizeX();
			int sizeY = hist.getSizeY();
			/* write as determined by type */
			if (hist.getType() == Histogram.ONE_DIM_INT) {
				int[] countsInt = (int[]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					dosHis.writeInt(countsInt[i]);
				}
			} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
				double[] countsDbl = (double[]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					dosHis.writeInt((int) (countsDbl[i] + 0.5));
				}
			} else if (hist.getType() == Histogram.TWO_DIM_INT) {
				int[][] counts2dInt = (int[][]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						dosHis.writeInt(counts2dInt[j][i]);
					}
				}
			} else if (hist.getType() == Histogram.TWO_DIM_DOUBLE) {
				double[][] counts2dDbl = (double[][]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						dosHis.writeInt((int) (counts2dDbl[j][i] + 0.5));
					}
				}
			} else {
				msgHandler.errorOutln(
					"Unrecognized histogram type [ImpExpORNL]");
			}
		}
		msgHandler.messageOut(" (" + dosHis.size() + " bytes)");
		dosHis.flush();
		dosHis.close();
	}

	/**
	 * Get a int from an array of byes
	 */
	private int byteArrayToInt(byte[] array, int offset) {
		final byte a = array[offset];
		final byte b = array[offset + 1];
		final byte c = array[offset + 2];
		final byte d = array[offset + 3];
		final int rval = byteOrder == ByteOrder.BIG_ENDIAN ?
		constructInt(a,b,c,d) : constructInt(d,c,b,a);
		return rval;
	}
	
	private final int constructInt(byte highest, byte high, byte low, 
	byte lowest){
		return	((highest & 0xFF) << 24)
				| ((high & 0xFF) << 16)
				| ((low & 0xFF) << 8)
				| (lowest & 0xFF);
	}

	/**
	 * Get a short from an array of byes
	 */
	private short byteArrayToShort(byte[] array, int offset) {
		short rval; //return value
		if (byteOrder == ByteOrder.BIG_ENDIAN) {
			rval =
				(short) (((array[offset] & 0xFF) << 8)
					+ ((array[offset + 1] & 0xFF)));
		} else { //byteOrder is LITTLE_ENDIAN
			rval =
				(short) (((array[offset] & 0xFF))
					+ ((array[offset + 1] & 0xFF) << 8));
		}
		return rval;
	}

	private int readInt(DataInput di) throws IOException {
		byte[] tempInt = new byte[4];
		di.readFully(tempInt);
		return byteArrayToInt(tempInt, 0);
	}

	private short readShort(DataInput di) throws IOException {
		di.readFully(tempShort);
		return byteArrayToShort(tempShort, 0);
	}

	public boolean canExport() {
		return true;
	}

	boolean batchExportAllowed() {
		return false;
	}
	
	/**
	 * Opens a file with a specified dialog box title bar and file extension.
	 * It is usually called by <code>openFile</code> in subclasses of <code>ImpExp</code>.
	 *
	 * @param	    msg		    text to go on title bar of dialog box
	 * @param	    extension	    file extension to suggest to user
	 * @return	whether file was successfully read
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> go to the msgHandler
	 */
	protected boolean openFile(File in, String msg) {
		File inFile=in;
		boolean rval=false; //default return value
		try {
			if (in==null){
				inFile=getFileOpen(msg);
			}
			if (inFile != null) { // if Open file was  not canceled
				lastFile = inFile;
				final File f= (inFile.getName().endsWith("his")) ?
				new File(inFile.getParent(),FileUtilities.setExtension(inFile.getName(),
				"drr", FileUtilities.FORCE)) : inFile;
				FileInputStream inStream = new FileInputStream(f);
				BufferedInputStream inBuffStream = new BufferedInputStream(inStream, BUFFER_SIZE);
				if (msgHandler != null) msgHandler.messageOut(
					msg + " " + getFileName(inFile),
					MessageHandler.NEW);
				/* implementing class implement following method */
				readData(inBuffStream);
				if (msgHandler != null) msgHandler.messageOut(" done!", MessageHandler.END);
				inBuffStream.close();
				rval = true;
			}
		} catch (IOException ioe) {
			msgHandler.errorOutln("Problem handling file \""+inFile.getPath()+"\": "+ioe.getMessage());
		} catch (ImpExpException iee) {
			msgHandler.errorOutln("Problem while importing or exporting: "+iee.getMessage());
		}
		return rval;
	}

}
