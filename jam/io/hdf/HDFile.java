package jam.io.hdf;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import jam.global.MessageHandler;
import jam.global.JamProperties;
import jam.data.Histogram;

/**
 * Class which represents the HDF file on disk.
 *
 * @version	0.5 November 98
 * @author 	Dale Visser
 * @since       JDK1.1
 */
public final class HDFile extends RandomAccessFile implements HDFconstants {

	/**
	 *  number types automatically thrown into the file
	 */
	NumberType intNT, doubleNT;

	/**
	 * List of objects in the file.
	 */
	public List DataObjects;

	/**
	 * set by calls to SDDexists that have a true result
	 */
	public static ScientificDataDimension currentSDD;

	/**
	 * The size of the DD block.
	 */
	int DDblockSize;

	/**
	 * The offset to the first data object.  This is always 4+DDblockSize.
	 */
	int initialOffset;

	/**
	 * variable for marking position in file
	 */
	long mark = 0;

	/**
	 * Size of the file set in setOffsets()
	 */
	int fileSize;

	private File file; //File object corresponding to this object

	/**
	 * Constructor called with a <code>File</code> object, and an access mode.
	 *
	 * @param	file	file to be accessed
	 * @param	mode	"r" or "rw"
	 * @exception HDFException error with hdf file
	 * @exception IOException error opening file
	 */
	public HDFile(File file, String mode) throws HDFException, IOException {
		super(file, mode);
		this.file = file;
		if (mode == "rw") { //Saving a file
			writeHeader();
			DataObjects = new Vector();
			addVersionNumber();
		} else { //should be "r" ,i.e., opening a file
			if (!checkMagicWord())
				throw new HDFException("Not a valid HDF File!");
			DataObjects = new Vector();
		}
	}

	public File getFile() {
		return file;
	}

	/**
	 * Adds data element giving version of HDF libraries to use (4.1r2).
	 */
	protected void addVersionNumber() {
		new LibVersion(this); //DataObjects add themselves
	}

	/**
	 * Writes the unique 4-byte pattern at the head of the file denoting that it is an HDF file.
	 * @exception HDFException error with writting hdf file
	 */

	public void writeHeader() throws HDFException {
		try {
			seek(0);
			writeInt(HDF_HEADER);
		} catch (IOException e) {
			throw new HDFException("Problem writing header: " + e.getMessage());
		}
	}

	/**
	 * Looks at the internal index of data elements and sets the offset fields of
	 * the <code>DataObject</code>'s. To be run when all data elements have been defined.
	 */
	public void setOffsets() {
		int counter;

		DDblockSize = 2 + 4 + 12 * DataObjects.size();
		// numDD's + offset to next (always 0 here) + size*12 for
		// tag/ref/offset/length info
		initialOffset = DDblockSize + 4; //add in HDF file header
		counter = initialOffset;
		for (Iterator temp = DataObjects.iterator(); temp.hasNext();) {
			DataObject ob = (DataObject) (temp.next());
			ob.setOffset(counter);
			//System.out.println("Tag/Ref "+ob.getTag()+"/"+ob.getRef()+" set offset to "
			//+ob.getOffset());
			counter = counter + ob.getLength();
		}
		fileSize = counter;
	}

	/**
	 * Given a data object, writes out the appropriate bytes to the file on disk.
	 *
	 * @param	data	HDF data element
	 * @exception   HDFException	    thrown if unrecoverable error occurs
	 */
	public void writeDataObject(DataObject data) throws HDFException {
		try {
			seek(data.getOffset());
			write(data.getBytes(), 0, data.getLength());
		} catch (IOException e) {
			throw new HDFException(
				"Problem writing HDF data object" + e.getMessage());
		}
	}

	/**
	 * Almost all of Jam's number storage needs are satisfied by the type hard-coded into the class
	 * <code>NumberType</code>.  This method creates the <code>NumberType</code> object in the file
	 * that gets referred to repeatedly by the other data elements.
	 *
	 * @exception HDFException thrown if there is an error creating the number types
	 * @see NumberType
	 */
	public void addNumberTypes() throws HDFException {
		intNT = new NumberType(this, NumberType.INT);
		doubleNT = new NumberType(this, NumberType.DOUBLE);
	}

	/**
	 * Add element which tells other hdf software the encoding used in 
	 * Vdatas, etc.
	 *
	 * @see JavaMachineType
	 */
	public void addMachineType() {
		new JavaMachineType(this);
	}

	/**
	 * Easy access to the double number type.
	 */
	public NumberType getDoubleType() {
		return doubleNT;
	}

	/**
	 * Easy access to the int number type.
	 */
	public NumberType getIntType() {
		return intNT;
	}

	/**
	 * <p>Adds the data object to the file.  The reference number is implicitly assigned at this time
	 * as the index number in the internal <code>Vector DataObjects</code>.  A typical call should look
	 * like:</p>
	 * <blockquote><code>hdf = new hdfFile(outfile, "rw");<br>
	 * hdf.addDataObject(new DataObject(this));</code></blockquote>
	 * <p>Each call causes setOffsets to be called.</p>
	 *
	 * @param data data object
	 * @param useFileDefault	if true, automatically assigns ref number, else lets object decide
	 * @see	#getUniqueRef(DataObject)
	 * @see	#setOffsets()
	 */
	public void addDataObject(DataObject data, boolean useFileDefault) {
		DataObjects.add(data);
		if (useFileDefault) {
			data.setRef(getUniqueRef(data));
		}
	}

	/**
	 * The HDF standard
	 * only requires that for a particular tag type, each instance have a unique ref.  Since our files
	 * are not expected to contain more than several dozen objects, I take the simplest approach of simply
	 * assigning the index number + 1 from the DataObjects Vector.
	 */
	public short getUniqueRef(DataObject data) {
		return (short) (DataObjects.indexOf(data) + 1);
	}

	/**
	 * Returns the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.
	 */
	public ScientificDataDimension getSDD(Histogram h) {
		byte type;
		
		if (h.getType() == Histogram.ONE_DIM_INT
			|| h.getType() == Histogram.TWO_DIM_INT) {
			type = NumberType.INT;
		} else {
			type = NumberType.DOUBLE;
		}
		return getSDD(h,type);
	}

	/**
	 * Returns the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.  DOUBLE type
	 * is explicitly requested, for error bars.
	 */
	public ScientificDataDimension getSDD(Histogram h, byte numtype) {
		ScientificDataDimension sdd;
		int rank, sizeX, sizeY;
		ScientificDataDimension rval=null;//return value

		rank = h.getDimensionality();
		sizeX = h.getSizeX();
		if (rank == 2) {
			sizeY = h.getSizeY();
		} else {
			sizeY = 0;
		}
		for (Iterator temp = ofType(DataObject.DFTAG_SDD).iterator();
			temp.hasNext();
			) {
			sdd = (ScientificDataDimension) temp.next();
			if (sdd.getType() == numtype && sdd.getSizeX() == sizeX) {
				if ((rank == 1 && rank == sdd.getRank())
					|| (rank == 2
						&& rank == sdd.getRank()
						&& sdd.getSizeY() == sizeY)) {
					rval = sdd;
					break;//for quicker execution
				}
			}
		}
		if (rval==null){
			rval = new ScientificDataDimension(this, h);
		}
		return rval;
	}

	/**
	 *
	 * @exception HDFException unrecoverable errror
	 */
	public void writeDataDescriptorBlock() throws HDFException {
		int DDblockSize;

		try {
			DDblockSize = 2 + 4 + 12 * DataObjects.size();
			/* numDD's + offset to next (always 0 here) + size*12 for
			 * tag/ref/offset/length info */
			seek(4); //skip header
			writeShort(DataObjects.size()); //number of DD's
			writeInt(0); //no additional descriptor block
			for (Iterator temp = DataObjects.iterator(); temp.hasNext();) {
				DataObject ob = (DataObject) (temp.next());
				writeShort(ob.getTag());
				writeShort(ob.getRef());
				writeInt(ob.getOffset());
				writeInt(ob.getLength());
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem writing DD block: " + e.getMessage());
		}
	}

	/**
	 * Called after all <code>DataObject</code> objects have been created.
	 *
	 * @exception HDFException thrown if err occurs during file write
	 */
	public void writeAllObjects(MessageHandler msg) throws HDFException {
		int size, counter, outbar;
		double bar, remaining;
		DataObject ob;
		
		size = fileSize;
		counter = fileSize;
		outbar = 9;
		for (Iterator temp = DataObjects.iterator();
			temp.hasNext();
			counter -= ob.getLength()) {
			ob = (DataObject) (temp.next());
			if (ob.getLength() == 0)
				throw new HDFException("DataObject with no length encountered, halted writing HDF File");
			writeDataObject(ob);
			remaining = (double) counter / (double) size;
			bar = (double) outbar / 10.0;
			if (remaining < bar) {
				msg.messageOut(" " + outbar, MessageHandler.CONTINUE);
				outbar--;
			}
		}
	}
	/**
	 *
	 *  @exception HDFException unrecoverable error
	 */
	public void printDDblock() throws HDFException {
		int numDD;
		int nextBlock;
		try {
			boolean doAgain = true;
			seek(4);
			do {
				numDD = readShort(); //number of DD's
				nextBlock = readInt();
				if (nextBlock == 0) {
					doAgain = false;
				} else {
					seek(nextBlock);
				}
			} while (doAgain);
		} catch (IOException e) {
			throw new HDFException(
				"Problem printing DD block: " + e.getMessage());
		}
	}

	/**
	 *  @exception HDFException unrecoverable error
	 */
	public void readObjects() throws HDFException {
		int i;
		int numDD;
		int nextBlock;
		boolean doAgain;
		short tag;
		short ref;
		int offset;
		int length;
		byte[] bytes;
		DataObject ob;
		try {
			seek(4);
			doAgain = true;
			do {
				numDD = readShort(); //number of DD's
				nextBlock = readInt();
				//System.out.println(numDD+" data elements, next block: "+nextBlock);
				//System.out.println("Num. Tag  Ref  Offset  Length");
				for (i = 1; i <= numDD; i++) {
					tag = readShort();
					ref = readShort();
					offset = readInt();
					length = readInt();
					//System.out.println(i+".  "+tag+"  "+ref+"  "+offset+"  "+length);
					bytes = new byte[length];
					mark();
					seek(offset);
					//System.out.println("hdfFile reading at position: "+getFilePointer());
					read(bytes);
					reset();
					//System.out.println("hdfFile.readObjects(): Adding tag/ref: "+tag+"/"+ref);
					switch (tag) {
						case DataObject.DFTAG_DIA :
							new DataIDAnnotation(this, bytes, ref);
							break;
						case DataObject.DFTAG_DIL :
							new DataIDLabel(this, bytes, ref);
							break;
						case DataObject.DFTAG_VERSION :
							new LibVersion(this, bytes, ref);
							break;
						case DataObject.DFTAG_NT :
							new NumberType(this, bytes, ref);
							break;
						case DataObject.DFTAG_NDG :
							new NumericalDataGroup(this, bytes, ref);
							break;
						case DataObject.DFTAG_SD :
							new ScientificData(this, offset, length, ref);
							break;
						case DataObject.DFTAG_SDD :
							new ScientificDataDimension(this, bytes, ref);
							break;
						case DataObject.DFTAG_SDL :
							new ScientificDataLabel(this, bytes, ref);
							break;
						case DataObject.DFTAG_VG :
							new VirtualGroup(this, bytes, ref);
							break;
						case DataObject.DFTAG_VH :
							new VdataDescription(this, bytes, ref);
							break;
						case DataObject.DFTAG_VS :
							new Vdata(this, bytes, ref);
							break;
						case DataObject.DFTAG_MT :
							break; //do nothing
						case DataObject.DFTAG_FID :
							break; //do nothing
						case DataObject.DFTAG_FD :
							break; //do nothing
						case DataObject.DFTAG_SDS :
							break; //do nothing
						default :
							System.err.println("Unrecognized tag: " + tag);
					}
				}
				if (nextBlock == 0) {
					doAgain = false;
				} else {
					seek(nextBlock);
				}
			} while (doAgain);
			for (Iterator temp = DataObjects.iterator(); temp.hasNext();) {
				ob = (DataObject) (temp.next());
				ob.interpretBytes();
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem reading HDF objects: " + e.getMessage());
		}
	}

	/**
	 * Goes through index and returns object with the matching tag and ref.
	 */
	public DataObject getObject(short tag, short ref) {
		DataObject match = null;
		for (Iterator temp = DataObjects.iterator();
			temp.hasNext();
			) {
			DataObject ob = (DataObject) (temp.next());
			if (tag == ob.getTag() && ref == ob.getRef()) {
				match = ob;
			}
		}
		return match;
	}
	
	/**
	 *  @exception HDFException unrecoverable error
	 */
	protected void mark() throws HDFException {
		try {
			mark = getFilePointer();
		} catch (IOException e) {
			throw new HDFException(e.getMessage());
		}
	}
	/**
	 *  @exception HDFException unrecoverable error
	 */
	protected void reset() throws HDFException {
		try {
			seek(mark);
		} catch (IOException e) {
			throw new HDFException(e.getMessage());
		}
	}

	/**
	 * Returns a List of <code>DataObject</code>'s of the
	 * type specified by <code>tag</code>.
	 */
	public List ofType(List in, short tagType) {
		List output = new ArrayList();
		for (Iterator temp = in.iterator(); temp.hasNext();) {
			DataObject ob = (DataObject) (temp.next());
			if (ob.getTag() == tagType) {
				output.add(ob);
			}
		}
		return output;
	}

	public List ofType(DataObject in) {
		return ofType(DataObjects, (short) in.getTag());
	}

	public List ofType(short tagType) {
		return ofType(DataObjects, tagType);
	}

	public void addFileID(String ID) {
		new FileIdentifier(this, ID);
	}

	public void addFileNote() throws IOException {
		String notation;
		ByteArrayOutputStream baos;

		baos = new ByteArrayOutputStream();
		JamProperties.getProperties().store(
			baos,
			"Jam Properties at time of save:");
		notation = new String(baos.toByteArray());
		notation =
			"All error bars on histogram counts should be considered Poisson, unless a\n"
				+ "Numerical Data Group labelled 'Errors' is present, in which case the contents\n"
				+ "of that should be taken as the error bars."
				+ notation;
		notation =
			"\n\nThe histograms when loaded into jam are displayed starting at channel zero up\n"
				+ "to dimension-1.  Two-dimensional data are properly displayed with increasing channel\n"
				+ "number from the lower left to the lower right for, and from the lower left to the upper\n"
				+ "left."
				+ notation;
		//The data label stored for 1-d histograms consists of the x-axis label, a carriage\n"+
		//"return, then the y-axis label."+notation;
		new FileDescription(this, notation);
	}

	/**
	 * Checks whether this file contains the HDF magic word 
	 * at the beginning.
	 *
	 * @return <code>true</code> if this file has a valid HDF magic word
	 */
	public boolean checkMagicWord() {
		HDFileFilter filter=new HDFileFilter(false);
		return filter.accept(this.getFile());
	}
}
