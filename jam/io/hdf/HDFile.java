package jam.io.hdf;

import jam.data.Histogram;
import jam.global.JamProperties;
import jam.global.MessageHandler;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
	private NumberType intNT, doubleNT;

	/**
	 * Size of the file set in setOffsets()
	 */
	private int fileSize;

	/**
	 * List of objects in the file.
	 */
	private List objectList=new ArrayList();
	private Map tagMap=new HashMap();

	/**
	 * The size of the DD block.
	 */
	private int DDblockSize;

	/**
	 * variable for marking position in file
	 */
	private long mark = 0;

	private File file; //File object corresponding to this object

	/**
	 * Constructor called with a <code>File</code> object, and an access
	 * mode.
	 *
	 * @param f file to be accessed
	 * @param mode "r" or "rw"
	 * @exception HDFException error with hdf file
	 * @exception IOException error opening file
	 */
	public HDFile(File f, String mode) throws HDFException, IOException {
		super(f, mode);
		this.file = f;
		if ("rw".equals(mode)) { //Saving a file
			writeHeader();
			addVersionNumber();
		} else { //should be "r" ,i.e., opening a file
			if (!checkMagicWord()){
				throw new HDFException(f+"is not a valid HDF File!");
			}
		}
	}

	/**
	 * @return the file on disk being accessed
	 */
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
	 * Writes the unique 4-byte pattern at the head of the file denoting
	 * that it is an HDF file.
	 *
	 * @throws HDFException error with writing hdf file
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
	 * Looks at the internal index of data elements and sets the offset
	 * fields of the <code>DataObject</code>'s. To be run when all data
	 * elements have been defined.
	 */
	public void setOffsets() {
		synchronized (this) {
			DDblockSize = 2 + 4 + 12 * objectList.size();
			/* numDD's + offset to next (always 0 here) + size*12 for
			 * tag/ref/offset/length info */
		}
		final int initialOffset = DDblockSize + 4; //add in HDF file header
		int counter = initialOffset;
		final Iterator temp = objectList.iterator();
		while (temp.hasNext()) {
			final DataObject ob = (DataObject) (temp.next());
			ob.setOffset(counter);
			counter += ob.getLength();
		}
		synchronized (this) {
			fileSize = counter;
		}
	}

	/**
	 * Given a data object, writes out the appropriate bytes to the file
	 * on disk.
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
	 * Almost all of Jam's number storage needs are satisfied by the type
	 * hard-coded into the class <code>NumberType</code>.  This method
	 * creates the <code>NumberType</code> object in the file
	 * that gets referred to repeatedly by the other data elements.
	 *
	 * @throws HDFException if the types cannot be created
	 * @see jam.io.hdf.NumberType
	 */
	public void addNumberTypes() throws HDFException {
		synchronized (this){
			intNT = new NumberType(this, NumberType.INT);
			doubleNT = new NumberType(this, NumberType.DOUBLE);
		}
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
	 * @return the double number type.
	 */
	public NumberType getDoubleType() {
		return doubleNT;
	}

	/**
	 * @return the int number type.
	 */
	public NumberType getIntType() {
		return intNT;
	}

	/**
	 * <p>Adds the data object to the file.  The reference number is 
	 * implicitly assigned at this time as the index number in the 
	 * internal <code>Vector objectList</code>.  A typical call should 
	 * look like:</p>
	 * <blockquote><code>hdf = new hdfFile(outfile, "rw");<br>
	 * hdf.addDataObject(new DataObject(this));</code></blockquote>
	 * <p>Each call causes setOffsets to be called.</p>
	 *
	 * @param data data object
	 * @param useFileDefault	if true, automatically assigns ref number, 
	 * else lets object assign its own
	 * @see	#setOffsets()
	 */
	public void addDataObject(DataObject data, boolean useFileDefault) {
		objectList.add(data);
		final Short tag=data.getTagKey();
		if (!tagMap.containsKey(tag)){
			tagMap.put(tag,new HashMap());
		}
		final Map refMap=(Map)tagMap.get(tag);
		if (useFileDefault) {
			data.setRef(getUniqueRef(refMap));
		}
		refMap.put(data.getRefKey(),data);
	}

	/**
	 * The HDF standard
	 * only requires that for a particular tag type, each instance have a
	 * unique ref.  Since our files are not expected to contain more than
	 * several dozen objects, I take the simplest approach of simply
	 * assigning the index number + 1 from the objectList.
	 * 
	 * @return a reference number for the given HDF object
	 * @param refs the map for a given tag type
	 */
	private short getUniqueRef(Map refs) {
		/* a good guess is the size, almost always new */
		short rval=(short)refs.size();
		while (refs.containsKey(new Short(rval))){
			rval++;
		}
		return rval;
	}
	
	void changeRefKey(DataObject d, Short old) {
		final Short tag=d.getTagKey();
		final Map refs=(Map)tagMap.get(tag);
		if (refs.containsKey(old)){
			refs.remove(old);
		}
		/* if old not there, we were just called as the object was being
		 * added to the file...no worries */
		final Short ref=d.getRefKey();
		if (!refs.containsKey(ref)){
			refs.put(ref,d);
		} else {
			throw new IllegalStateException("Trying to put: "+ref+
			"for tag:"+tag+" when one already exists.");
		}
	} 

	/**
	 * @return the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.
	 * @param h that type is needed for
	 */
	ScientificDataDimension getSDD(Histogram h) {
		byte type=NumberType.DOUBLE;
		if (h.getType() == Histogram.ONE_DIM_INT
			|| h.getType() == Histogram.TWO_DIM_INT) {
			type = NumberType.INT;
		}
		return getSDD(h,type);
	}

	/**
	 * Returns the existing valid SDD type for the histogram, 
	 * creating a new one if necessary.  DOUBLE type
	 * is explicitly requested, for error bars.
	 *
	 * @param h which type is needed for
	 * @param numtype the number HDF uses to indicate the type
	 * @return the SDD object representing the histogram size and number 
	 * type
	 */
	ScientificDataDimension getSDD(Histogram h, byte numtype) {
		ScientificDataDimension rval=null;//return value
		final int rank = h.getDimensionality();
		final int sizeX = h.getSizeX();
		int sizeY=0;
		if (rank == 2) {//otherwise rank == 1
			sizeY = h.getSizeY();
		} 
		final Iterator temp = ofType(DataObject.DFTAG_SDD).iterator();
		while (temp.hasNext()) {
			final ScientificDataDimension sdd = (ScientificDataDimension) temp.next();
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
		try {
			synchronized(this){
				DDblockSize = 2 + 4 + 12 * objectList.size();
			}
			/* numDD's + offset to next (always 0 here) + size*12 for
			 * tag/ref/offset/length info */
			seek(4); //skip header
			writeShort(objectList.size()); //number of DD's
			writeInt(0); //no additional descriptor block
			final Iterator temp = objectList.iterator();
			while (temp.hasNext()) {
				final DataObject ob = (DataObject) (temp.next());
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
	 * Called after all <code>DataObject</code> objects have been 
	 * created.
	 *
	 * @exception HDFException thrown if err occurs during file write
	 * @param msg output text area
	 */
	public void writeAllObjects(MessageHandler msg) throws HDFException {
		final int size = fileSize;
		int counter = fileSize;
		int outbar = 9;
		final Iterator temp = objectList.iterator();
		while (temp.hasNext()) {
			final DataObject ob = (DataObject) (temp.next());
			if (ob.getLength() == 0){
				throw new HDFException("DataObject with no length encountered, halted writing HDF File");
			}
			writeDataObject(ob);
			final double remaining = (double) counter / (double) size;
			final double bar = (double) outbar / 10.0;
			if (remaining < bar) {
				msg.messageOut(" " + outbar, MessageHandler.CONTINUE);
				outbar--;
			}
			counter -= ob.getLength();
		}
	}
	/**
	 *
	 *  @exception HDFException unrecoverable error
	 */
	public void printDDblock() throws HDFException {
		try {
			boolean doAgain = true;
			seek(HDFconstants.HDF_HEADER_NUMBYTES);
			do {
				readShort(); //skip number of DD's
				final int nextBlock = readInt();
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
		try {
			seek(4);
			boolean doAgain = true;
			do {
				final int numDD = readShort(); //number of DD's
				final int nextBlock = readInt();
				for (int i = 1; i <= numDD; i++) {
					final short tag = readShort();
					final short ref = readShort();
					final int offset = readInt();
					final int length = readInt();
					final byte [] bytes = new byte[length];
					mark();
					seek(offset);
					read(bytes);
					reset();
					switch (tag) {
						case DataObject.DFTAG_DIA :
							new DataIDAnnotation(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_DIL :
							new DataIDLabel(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_VERSION :
							new LibVersion(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_NT :
							new NumberType(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_NDG :
							new NumericalDataGroup(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_SD :
							new ScientificData(this, offset, length, tag, ref);
							break;
						case DataObject.DFTAG_SDD :
							new ScientificDataDimension(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_SDL :
							new ScientificDataLabel(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_VG :
							new VirtualGroup(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_VH :
							new VdataDescription(this, bytes, tag, ref);
							break;
						case DataObject.DFTAG_VS :
							new Vdata(this, bytes, tag, ref);
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
							throw new HDFException("Unrecognized tag: " + tag);
					}
				}
				if (nextBlock == 0) {
					doAgain = false;
				} else {
					seek(nextBlock);
				}
			} while (doAgain);
			final Iterator temp = objectList.iterator();
			while (temp.hasNext()) {
				final DataObject ob = (DataObject) (temp.next());
				ob.interpretBytes();
			}
		} catch (IOException e) {
			throw new HDFException(
				"Problem reading HDF objects: " + e.getMessage());
		}
	}

	/**
	 * @return object in file with the matching tag and ref
	 * @param t tag of HDF object
	 * @param r <em>unique</em> reference number in file
	 */
	public DataObject getObject(short t, short r) {
		DataObject match = null;
		final Short tag=new Short(t);
		final Short ref=new Short(r);
		if (tagMap.containsKey(tag)){
			final Map refMap=(Map)tagMap.get(tag);
			if (refMap.containsKey(ref)){
				match=(DataObject)refMap.get(ref);
			}
		}
		return match;
	}
	
	/**
	 *  @exception HDFException unrecoverable error
	 */
	protected void mark() throws HDFException {
		try {
			synchronized(this){
				mark = getFilePointer();
			}
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
	 * @return a subset of the given list of <code>DataObject</code>'s of
	 * the specified type 
	 * @param in the list to search
	 * @param tagType the type to return
	 */
	public List ofType(Collection in, short tagType) {
		/*final List output = new ArrayList();
		final Iterator temp = in.iterator(); 
		while ( temp.hasNext()) {
			final DataObject ob = (DataObject) (temp.next());
			if (ob.getTag() == tagType) {
				output.add(ob);
			}
		}
		return output;*/
		final Set ssin=new HashSet();
		final Object temp=tagMap.get(new Short(tagType));
		if (temp !=null){
			ssin.addAll(in);
			Map refMap=(Map)temp;
			ssin.retainAll(refMap.values());
		}
		return new ArrayList(ssin);
	}

	/**
	 * @return a list of all <code>DataObject</code>s in this file of the
	 * same type as the passed instance
	 * @param in example of the type to return 
	 */
	public List ofType(DataObject in) {
		//return ofType(objectList, in.getTag());
		final List rval=new ArrayList();
		final Object temp=tagMap.get(in.getTagKey());
		if (temp != null){
			final Map refMap=(Map)temp;
			rval.addAll(refMap.values());
		}
		return rval;
	}
	
	/**
	 * @return a list of all <code>DataObject</code>s in this file of the
	 * given type
	 * @param tagType the type to return 
	 */
	public List ofType(short tagType) {
		//return ofType(objectList, tagType);
		final List rval=new ArrayList();
		final Object temp=tagMap.get(new Short(tagType));
		if (temp != null){//the refmap exists
			final Map refMap=(Map)temp;
			rval.addAll(refMap.values());
		}
		return rval;		
	}

	/**
	 * Add a file identifier based on the given string.
	 * 
	 * @param ID file-id is based on this
	 */
	public void addFileID(String ID) {
		new FileIdentifier(this, ID);
	}
	

	/**
	 * Add a text note to the file, which includes the state of 
	 * <code>JamProperties</code>.
	 *
	 * @throws IOException if there's a problem writing to the file
	 * @see jam.global.JamProperties
	 */
	public void addFileNote() throws IOException {
		final String noteAddition=
			"\n\nThe histograms when loaded into jam are displayed starting at channel zero up\n"
				+ "to dimension-1.  Two-dimensional data are properly displayed with increasing channel\n"
				+ "number from the lower left to the lower right for, and from the lower left to the upper\n"
				+ "left."
				+ "All error bars on histogram counts should be considered Poisson, unless a\n"
				+ "Numerical Data Group labelled 'Errors' is present, in which case the contents\n"
				+ "of that should be taken as the error bars.";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final String header="Jam Properties at time of save:";
		JamProperties.getProperties().store(baos,header);
		final String notation = new String(baos.toByteArray())+noteAddition;
		new FileDescription(this, notation);
	}

	/**
	 * Checks whether this file contains the HDF magic word 
	 * at the beginning.
	 *
	 * @return <code>true</code> if this file has a valid HDF magic word
	 */
	public boolean checkMagicWord() {
		final HDFileFilter filter=new HDFileFilter(false);
		return filter.accept(this.getFile());
	}
	
	/**
	 * First, calls <code>super.close()</code>, then clears collections of temporary objects used
	 * to build the file, and then sets their references to
	 * to <code>null</code>. 
	 * 
	 * @see java.io.RandomAccessFile#close()
	 */
	public void close() throws IOException{
		super.close();
		for (Iterator it=objectList.iterator(); it.hasNext();){
			DataObject ob=(DataObject)it.next();
			ob.bytes=null;
			ob.file=null;
			ob.refKey=null;
			ob.tagKey=null;
		}
		objectList.clear();
		tagMap.clear();
		objectList=null;
		tagMap=null;
		intNT=null;
		doubleNT=null;
	}
}
