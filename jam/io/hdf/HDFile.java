package jam.io.hdf;

import jam.data.Histogram;
import jam.global.JamProperties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

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
	private transient NumberType intNT, doubleNT;

	/**
	 * List of objects in the file.
	 */
	private transient List objectList=Collections.synchronizedList(new ArrayList());
	private transient Map tagRefMap=Collections.synchronizedMap(new HashMap());

	/**
	 * variable for marking position in file
	 */
	private transient long mark = 0;

	private short refCount;

	private transient File file; //File object corresponding to this object

	/**
	 * Constructor called with a <code>File</code> object, and an access
	 * mode.
	 *
	 * @param file file to be accessed
	 * @param mode "r" or "rw"
	 * @exception HDFException error with hdf file
	 * @exception IOException error opening file
	 */
	public HDFile(File file, String mode) throws HDFException, IOException {
		super(file, mode);
		refCount=0;
		this.file = file;
		if ("rw".equals(mode)) { //Saving a file
			writeHeader();
			addVersionNumber();
		} else { //should be "r" ,i.e., opening a file
			if (!checkMagicWord()){
				throw new HDFException(file+" is not a valid HDF File!");
			}
		}
	}

	/**
	 * @return the file on disk being accessed
	 */
	File getFile() {
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
	private void writeHeader() throws HDFException {
		try {
			seek(0);
			writeInt(HDF_HEADER);
		} catch (IOException e) {
			throw new HDFException("Problem writing HDF header.",e);
		}
	}

	/**
	 * Looks at the internal index of data elements and sets the offset
	 * fields of the <code>DataObject</code>'s. To be run when all data
	 * elements have been defined.
	 */
	synchronized void setOffsets() {
		//final int DDblockSize = 2 + 4 + 12 * objectList.size();
		final int initialOffset = ddBlockSize() + 4; //add in HDF file header
		int counter = initialOffset;
		final Iterator temp = objectList.iterator();
		while (temp.hasNext()) {
			final DataObject ob = (DataObject) (temp.next());
			ob.setOffset(counter);
			counter += ob.getLength();
		}
	}
	
	private final int ddBlockSize(){
	    /* The size of the DD block. */
		/* numDD's + offset to next (always 0 here) + size*12 for
		 * tag/ref/offset/length info */
	    return 2 + 4 + 12 * objectList.size();
	}

	/**
	 * Given a data object, writes out the appropriate bytes to the file
	 * on disk.
	 *
	 * @param	data	HDF data element
	 * @exception   HDFException	    thrown if unrecoverable error occurs
	 */
	private void writeDataObject(DataObject data) throws HDFException {
		try {
			seek(data.getOffset());
			write(data.getBytes(), 0, data.getLength());
		} catch (IOException e) {
			throw new HDFException(
				"Problem writing HDF data object.",e);
		}
	}

	/**
	 * Almost all of Jam's number storage needs are satisfied by the type
	 * hard-coded into the class <code>NumberType</code>.  This method
	 * creates the <code>NumberType</code> object in the file
	 * that gets referred to repeatedly by the other data elements.
	 *
	 * @see jam.io.hdf.NumberType
	 */
	void addNumberTypes() {
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
	void addMachineType() {
		new JavaMachineType(this);
	}

	/**
	 * @return the double number type.
	 */
	NumberType getDoubleType() {
		return doubleNT;
	}

	/**
	 * @return the int number type.
	 */
	NumberType getIntType() {
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
	void addDataObject(DataObject data, boolean useFileDefault) {
		if (useFileDefault) {
			data.setRef(getUniqueRef());
		}
		
		final Integer key =data.getKey();
		if (!tagRefMap.containsKey(key)){
			tagRefMap.put(key, data);
			objectList.add(data);
		}
	}

	/**
	 * 
	 * The HDF standard only requires that for a particular tag type, each instance have a
	 * unique ref.
	 * ----------------NO TRUE ANY LONGER-----------------  
	 * Since our files are not expected to contain more than
	 * several dozen objects, 
	 * I take the simplest approach of simply
	 * assigning the index number + 1 from the objectList.
	 * ----------------NO TRUE ANY LONGER-----------------
	 * 
	 * Just adds one to ref Count
	 * 
	 * @return a reference number for the given HDF object
	 * @param refs the map for a given tag type
	 */
	private short getUniqueRef() {
		//Just add 1, set to 1 every time class created 
		return ++refCount;
		/*
		while (refs.containsKey(new Short(rval))){
			rval++;
		}
		return rval;
		*/
	}
	
	void changeRefKey(DataObject d, Short old) {
		final Short tag=d.getTagKey();
		final Map refs=(Map)tagRefMap.get(tag);
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
		if (h.getType().isInteger()) {
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
	synchronized void writeDataDescriptorBlock() throws HDFException {
		try {
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
				"Problem writing DD block.",e);
		}
	}

	/* non-javadoc:
	 * Called after all <code>DataObject</code> objects have been 
	 * created.
	 *
	 * @exception HDFException thrown if err occurs during file write
	 */
	void writeAllObjects(final ProgressMonitor pm) throws HDFException {
		pm.setMaximum(objectList.size());
		int progress=1;
		pm.setProgress(progress);
		pm.setNote("Writing to disk");
		final Iterator temp = objectList.iterator();
		while (temp.hasNext()) {
			final DataObject ob = (DataObject) (temp.next());
			if (ob.getLength() == 0){
				throw new HDFException("DataObject with no length encountered, halted writing HDF File.");
			}
			writeDataObject(ob);
			progress++;
			final int value=progress;
			final Runnable runner=new Runnable(){
				public void run(){
					pm.setProgress(value);					
				}
			};
			SwingUtilities.invokeLater(runner);
		}
	}
	
	/**
	 * Reads file into set of DataObject's and sets their internal
	 * variables.
	 * 
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
				"Problem reading HDF objects.",e);
		}
	}

	/**
	 * @return object in file with the matching tag and ref
	 * @param t tag of HDF object
	 * @param r <em>unique</em> reference number in file
	 */
	public DataObject getObject(short t, short r) {
		DataObject match = null;
		final Integer key =calculateKey(t, r);		
		if (tagRefMap.containsKey(key)){
			match=(DataObject)tagRefMap.get(key);
		}
		return match;
	}
	
	/**
	 *  @exception IOException unrecoverable error
	 */
	private synchronized void mark() throws IOException {
            mark = getFilePointer();
    }
	
	/**
	 *  @exception IOException unrecoverable error
	 */
	private synchronized void reset() throws IOException {
		seek(mark);
	}

	/**
	 * @return a subset of the given list of <code>DataObject</code>'s of
	 * the specified type 
	 * @param in the list to search
	 * @param tagType the type to return
	 */
	List ofType(Collection in, short tagType) {
		
		final Set ssin=new HashSet();
		Iterator iter = in.iterator();
		while(iter.hasNext()){
			DataObject dataObject=(DataObject)iter.next();
			if (tagType == dataObject.getTag())
				ssin.add(dataObject);
		}
		/* FIXME remove KBS
		final Object temp=tagRefMap.get(new Short(tagType));
		if (temp !=null){
			ssin.addAll(in);
			Map refMap=(Map)temp;
			ssin.retainAll(refMap.values());
		}
		*/
		return new ArrayList(ssin);
	}

	/**
	 * @return a list of all <code>DataObject</code>s in this file of the
	 * given type
	 * @param tagType the type to return 
	 */
	public List ofType(final short tagType) {
		final List rval=new ArrayList();
		
		final Iterator iter = objectList.iterator();
		while(iter.hasNext()){
			DataObject dataObject=(DataObject)iter.next();
			if (tagType == dataObject.getTag())
				rval.add(dataObject);
		}
		/*FIMXE KBS remove
		final Object temp=tagRefMap.get(new Short(tagType));
		if (temp != null){//the refmap exists
			final Map refMap=(Map)temp;
			rval.addAll(refMap.values());
		}
		*/
		return rval;
		
	}

	/**
	 * Add a file identifier based on the given string.
	 * 
	 * @param ID file-id is based on this
	 */
	void addFileID(String ID) {
		new FileIdentifier(this, ID);
	}
	

	/**
	 * Add a text note to the file, which includes the state of 
	 * <code>JamProperties</code>.
	 *
	 * @throws IOException if there's a problem writing to the file
	 * @see jam.global.JamProperties
	 */
	void addFileNote() throws IOException {
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
		final String notation = baos.toString()+noteAddition;
		new FileDescription(this, notation);
	}

	/**
	 * Checks whether this file contains the HDF magic word 
	 * at the beginning.
	 *
	 * @return <code>true</code> if this file has a valid HDF magic word
	 */
	private boolean checkMagicWord() {
		final HDFileFilter filter=new HDFileFilter(false);
		return filter.accept(getFile());
	}
	
	protected Integer calculateKey(short tag, short ref){
		int key= (((int)tag)<<16)+(int)ref;
		
		return new Integer(key);
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
		tagRefMap.clear();
		objectList=null;
		tagRefMap=null;
		intNT=null;
		doubleNT=null;
	}
}
