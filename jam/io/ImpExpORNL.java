package jam.io;
import jam.data.DataException;
import jam.data.Histogram;
import jam.global.MessageHandler;
import jam.util.*;
import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.*;

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
	/** big endian order */
	private final int BIG_ENDIAN = 1;
	/** little endian order */
	private final int LITTLE_ENDIAN = 0;
	/** byte order either BIG_ENDIAN or LITTLE_ENDIAN */
	private int byteOrder = BIG_ENDIAN;

	/**
	 *
	 */
//	List HistogramRecords;
	
	/** 
	 * input steam for drr file 
	 */
	private DataInputStream disDrr;
	
	/**
	 *  Stuff read in for every drr file
	 */
	String signature;
	int totalHist; //number of histograms
	int totalHalfWords;
	int temp;
	int year;
	int month;
	int day;
	int hour;
	int minute;
	int second;
	String chilText;

	//Histogram imfo in Drr file for each histogram
	//
	int[] dim; // Histogram dimensionality 
	int[] chSize; //half words per channel
	int[] param1; // Histogram parameter
	int[] param2;
	int[] param3;
	int[] param4;
	int[] lenParRaw1; //Length raw parameters
	int[] lenParRaw2;
	int[] lenParRaw3;
	int[] lenParRaw4;
	int[] lenParScal1; //Length scaled parameters
	int[] lenParScal2;
	int[] lenParScal3;
	int[] lenParScal4;
	int[] minCh1; //Min channels
	int[] minCh2;
	int[] minCh3;
	int[] minCh4;
	int[] maxCh1; //Max channels 
	int[] maxCh2;
	int[] maxCh3;
	int[] maxCh4;
	int[] offSet;
	String[] parLabelX; //x-parm label x 12 bytes
	String[] parLabelY; //y parm label y 12 bytes		
	float[] cal1; // Calibration constants
	float[] cal2;
	float[] cal3;
	float[] cal4;
	String[] titleDrr; //title 40 bytes

	int[] iDnumber; //ID list

	private byte[] tempInt = new byte[4];
	private byte[] tempShort = new byte[2];

	/**
	 * Constructor
	 */
	public ImpExpORNL(Frame frame, MessageHandler msgHandler) {
		super(frame, msgHandler);
//		HistogramRecords = new Vector();
	}

	public ImpExpORNL(){
		super();
	}

    public String getFileExtension(){
    	return ".drr";
    }
    
    public String getFormatDescription(){
    	return "Oak Ridge DAMM";
    }

	/**
	 * Open a file which was written using ORNL .drr and .his format. 
	 * Can't do the same as other ImpExp routines, since 2 files are needed.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the MessageHandler
	 */
	public boolean openFile() throws ImpExpException {
		return openFile("Import ORNL file ", "drr");
	}

	/**
	 * Write out a particular histogram.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the MessageHandler
	 */
	public void saveFile(Histogram hist) throws ImpExpException {
		saveFile("Export ORNL file (use .drr explicitly)", "drr", hist);
	}

	/**
	 * Reads in a histogram from the event stream.
	 *
	 * @exception   ImpExpException	    thrown for errors
	 */
	public void readHist(InputStream buffin) throws ImpExpException {
		String fileNameHis;
		RandomAccessFile fileHis;

		try {
			Histogram.clearList(); //clear current list of histograms	
			readDrr(buffin); //read the drr file

			fileNameHis =
				FileUtilities.setExtension(
					getFileName(lastFile),
					"*.his",
					FileUtilities.FORCE);
			fileHis = new RandomAccessFile(fileNameHis, "r");
			//open .his file random access, read only	

			//read in his file and load spectra	    
			for (int k = 0; k < totalHist; k++) {
				//FIXMEif()){
				readHist(fileHis, k);
				//}		    			
			}
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		} catch (UtilException je) {
			throw new ImpExpException(je.toString());
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
		checkByteOrder(totalHistByte);
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

		// Histogram info in Drr file
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
			//length Scaled parameters
			lenParScal2[i] = (int) readShort(disDrr);
			//length Scaled parameters
			lenParScal3[i] = (int) readShort(disDrr);
			//length Scaled parameters
			lenParScal4[i] = (int) readShort(disDrr);
			//length Scaled parameters
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

		//read in id list
		for (int i = 0; i < totalHist; i++) {
			iDnumber[i] = readInt(disDrr); // Id number 
		}

		disDrr.close();
	}
	/**
	 * check the byte order by checking total number of histogram int
	 */
	private void checkByteOrder(byte[] i) {
		if ((i[0] == 0) && (i[1] == 0)) {
			byteOrder = BIG_ENDIAN;
		} else {
			byteOrder = LITTLE_ENDIAN;
		}
	}
	/**
	 * Read in a histogram
	 *
	 */
	private void readHist(RandomAccessFile fileHis, int k) throws IOException {

		Histogram hist;
		int offset; //offset set in byte array
		int numByteToRead;
		byte[] inBuffer;

		// stuff for each histogram
		String name;
		int number;
		String title;
		int type;
		int wordCh;
		int sizeX;
		int sizeY;
		int[] counts;
		int[][] counts2d;

		//copy to histogram variables
		name = "" + iDnumber[k] + " " + titleDrr[k].trim();
		number = iDnumber[k];
		type = dim[k];
		wordCh = chSize[k];
		sizeX = lenParScal1[k];
		sizeY = lenParScal2[k];
		title = titleDrr[k].trim();

		try {
			//Read in 2D histogram	
			if (dim[k] == 2) {
				//FIXMEdebugDump(k);

				counts2d = new int[sizeX][sizeY];
				fileHis.seek((long) offSet[k] * 2);
				numByteToRead = sizeX * sizeY * 4;
				inBuffer = new byte[numByteToRead];
				fileHis.read(inBuffer); //read in byte array
				//four byte data 
				if (wordCh == 2) {
					offset = 0;
					for (int j = 0; j < sizeY; j++) {
						for (int i = 0; i < sizeX; i++) {
							counts2d[i][j] = byteArrayToInt(inBuffer, offset);
							offset += 4;
						}
					}
					//two byte data		    
				} else if (wordCh == 1) {
					offset = 0;
					for (int j = 0; j < sizeY; j++) {
						for (int i = 0; i < sizeX; i++) {
							counts2d[i][j] = byteArrayToShort(inBuffer, offset);
							offset += 2;
						}
					}
					//not able to handle data		    
				} else {
					System.err.println(
						"Error cannot handle channel size " + wordCh);
				}

				hist = new Histogram(name, title, counts2d);
				hist.setNumber(number);
				if (msgHandler != null) msgHandler.messageOut(" .");

				//Read in 1D Histogram		
			} else {
				counts = new int[sizeX];
				fileHis.seek((long) offSet[k] * 2);
				numByteToRead = sizeX * 4;
				inBuffer = new byte[numByteToRead];
				fileHis.read(inBuffer); //read in byte array
				//four byte data
				if (wordCh == 2) {
					offset = 0;
					for (int i = 0; i < sizeX; i++) {
						counts[i] = byteArrayToInt(inBuffer, offset);
						offset += 4;
					}
					//two byte data		    
				} else if (wordCh == 1) {
					offset = 0;
					for (int i = 0; i < sizeX; i++) {
						counts[i] = byteArrayToShort(inBuffer, offset);
						offset += 2;
					}
					//unable to handle data type		    
				} else {
					System.err.println(
						"Error cannot handle channel size "
							+ wordCh
							+ "[ImpExpORNL]");
				}
				hist = new Histogram(name, title, counts);
				hist.setNumber(number);
				if (msgHandler != null) msgHandler.messageOut(" .");

			}

			//error creating histogram	    
		} catch (DataException de) {
			if (msgHandler != null) msgHandler.errorOutln(name + ": " + de.getMessage());
		}
	}

	/**
	 * Writes a histogram to the file.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the msgHandler
	 */
	public void writeHist(OutputStream buffout, Histogram hist)
		throws ImpExpException {
		String fileNameHis;
		File fileHis;
		FileOutputStream fosHis;
		BufferedOutputStream buffoutHis;
		try {
			fileNameHis =
				FileUtilities.setExtension(
					getLastFileName(),
					".his",
					FileUtilities.FORCE);
			fileHis = new File(fileNameHis);
			fosHis = new FileOutputStream(fileHis);
			buffoutHis = new BufferedOutputStream(fosHis);

			//write out drr file		
			writeDrr(buffout);

			//write out his file
			writeHis(buffoutHis);
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		} catch (UtilException je) {
			throw new ImpExpException(je.toString());
		}

	}
	/**
	 * write out a ORNL drr file
	 */
	private void writeDrr(OutputStream buffout) throws IOException {

		DataOutputStream dosDrr;
		List allHists;
		Histogram hist;

		short sizeX;
		short sizeY;
		int diskOffSet = 0;

		dosDrr = new DataOutputStream(buffout);
		allHists = Histogram.getHistogramList();

		//number of histograms
		totalHist = Histogram.getHistogramList().size(); //number of histograms

		//total number of 1/2 words need in file //size of file needed? in 16 bit words
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
				System.err.println("Unrecognized histogram type [ImpExpORNL]");
			}
		}

		// write header		
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
		dosDrr.writeBytes(
			StringUtilities.makeLength("File Created by Jam", 80));
		//text from chill file

		for (int i = 0; i < allHists.size(); i++) {
			hist = ((Histogram) allHists.get(i));
			sizeX = (short) (hist.getSizeX());
			sizeY = (short) (hist.getSizeY()); //will be zero for 1-d

			// use data output stream name only 15 char long title 50 char long
			dosDrr.writeShort((short) (hist.getType()));
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
			dosDrr.writeBytes(StringUtilities.makeLength(hist.getTitle(), 40));
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
				System.err.println("Unrecognized histogram type [ImpExpORNL]");
			}
		}

		//write out numbers
		for (int i = 0; i < allHists.size(); i++) {
			dosDrr.writeInt(((Histogram) allHists.get(i)).getNumber());
			// Id number
		}

		dosDrr.flush();
		dosDrr.close();

	}
	/**
	 * Write out the .his file
	 *
	 */
	private void writeHis(OutputStream outputStream) throws IOException {

		DataOutputStream dosHis;
		Iterator allHistograms;
		Histogram hist;

		int sizeX;
		int sizeY;
		int countsInt[];
		int counts2dInt[][];
		double countsDbl[];
		double counts2dDbl[][];

		dosHis = new DataOutputStream(outputStream);

		allHistograms = Histogram.getHistogramList().iterator();
		while (allHistograms.hasNext()) {
			hist = ((Histogram) allHistograms.next());
			sizeX = hist.getSizeX();
			sizeY = hist.getSizeY();

			//write as determined by type
			if (hist.getType() == Histogram.ONE_DIM_INT) {
				countsInt = (int[]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					dosHis.writeInt(countsInt[i]);
				}

			} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
				countsDbl = (double[]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					dosHis.writeInt((int) (countsDbl[i] + 0.5));
				}

			} else if (hist.getType() == Histogram.TWO_DIM_INT) {
				counts2dInt = (int[][]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						dosHis.writeInt(counts2dInt[j][i]);
					}
				}

			} else if (hist.getType() == Histogram.TWO_DIM_DOUBLE) {
				counts2dDbl = (double[][]) hist.getCounts();
				for (int i = 0; i < sizeX; i++) {
					for (int j = 0; j < sizeY; j++) {
						dosHis.writeInt((int) (counts2dDbl[j][i] + 0.5));
					}
				}

			} else {
				System.err.println("Unrecognized histogram type [ImpExpORNL]");
			}
			if (msgHandler != null) msgHandler.messageOut(". ");
		}

		dosHis.flush();
		dosHis.close();

	}
	/**
	 * Get a int from an array of byes
	 */
	private int byteArrayToInt(byte[] array, int offset) {
		int rval;//return value
		if (byteOrder == BIG_ENDIAN) {
			rval = (
				((array[offset] & 0xFF) << 24)
					+ ((array[offset + 1] & 0xFF) << 16)
					+ ((array[offset + 2] & 0xFF) << 8)
					+ ((array[offset + 3] & 0xFF)));
		} else {
			rval = (
				((array[offset] & 0xFF) << 0)
					+ ((array[offset + 1] & 0xFF) << 8)
					+ ((array[offset + 2] & 0xFF) << 16)
					+ ((array[offset + 3] & 0xFF) << 24));
		}
		return rval;
	}

	/**
	 * Get a short from an array of byes
	 */
	private short byteArrayToShort(byte[] array, int offset) {
		short rval;//return value
		if (byteOrder == BIG_ENDIAN) {
			rval = (short)
				(((array[offset] & 0xFF) << 8) + ((array[offset + 1] & 0xFF)));
		} else {
			rval = (short)
				(((array[offset] & 0xFF)) + ((array[offset + 1] & 0xFF) << 8));
		}
		return rval;
	}

	private int readInt(DataInput di) throws IOException {
		di.readFully(tempInt);
		return byteArrayToInt(tempInt, 0);
	}

	private short readShort(DataInput di) throws IOException {
		di.readFully(tempShort);
		return byteArrayToShort(tempShort, 0);
	}
}
