package jam.io.hdf;

import jam.data.Histogram;
import jam.global.JamProperties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

	//FIXME KBS remove
	/**
	 *  number types automatically thrown into the file
	 */
	//private transient NumberType intNT, doubleNT;

	/**
	 * List of objects in the file.
	 */
	//private transient List objectList=Collections.synchronizedList(new ArrayList());
	//private transient Map tagRefMap=Collections.synchronizedMap(new HashMap());

	/**
	 * variable for marking position in file
	 */
	private transient long mark = 0;



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
	public HDFile(File file, String mode) throws FileNotFoundException {
		super(file, mode);
		DataObject.clear();
		this.file = file;
		//FIXME KBS remove
		//addNumberTypes();
		/*
		if ("rw".equals(mode)) { //Saving a file
			//writeMagicWord();
			//addVersionNumber();
		} else { //should be "r" ,i.e., opening a file
			if (!checkMagicWord()){
				throw new HDFException(file+" is not a valid HDF File!");
			}
		}
		*/
	}
	
	boolean isHDFFile() {
		return checkMagicWord();
	}
	void writeHeader()  throws HDFException, IOException {
		writeMagicWord();		
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

	/**
	 * Writes the unique 4-byte pattern at the head of the file denoting
	 * that it is an HDF file.
	 *
	 * @throws HDFException error with writing hdf file
	 */
	private void writeMagicWord() throws HDFException {
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
		List objectList = DataObject.getDataObjectList();
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
		List objectList = DataObject.getDataObjectList();
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
			rval = new ScientificDataDimension(h);
		}
		return rval;
	}

	/**
	 *
	 * @exception HDFException unrecoverable errror
	 */
	synchronized void writeDataDescriptorBlock() throws HDFException {
		List objectList = DataObject.getDataObjectList();
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
		List objectList = DataObject.getDataObjectList();
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
		List objectList = DataObject.getDataObjectList();
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
							new DataIDAnnotation(bytes, tag, ref);
							break;
						case DataObject.DFTAG_DIL :
							new DataIDLabel(bytes, tag, ref);
							break;
						case DataObject.DFTAG_VERSION :
							new LibVersion(bytes, tag, ref);
							break;
						case DataObject.DFTAG_NT :
							new NumberType(bytes, tag, ref);
							break;
						case DataObject.DFTAG_NDG :
							new NumericalDataGroup(bytes, tag, ref);
							break;
						case DataObject.DFTAG_SD :
							new ScientificData(offset, length, tag, ref);
							break;
						case DataObject.DFTAG_SDD :
							new ScientificDataDimension(bytes, tag, ref);
							break;
						case DataObject.DFTAG_SDL :
							new ScientificDataLabel(bytes, tag, ref);
							break;
						case DataObject.DFTAG_VG :
							new VirtualGroup(bytes, tag, ref);
							break;
						case DataObject.DFTAG_VH :
							new VdataDescription(bytes, tag, ref);
							break;
						case DataObject.DFTAG_VS :
							new Vdata(bytes, tag, ref);
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
		List objectList = DataObject.getDataObjectList();
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
	 * Checks whether this file contains the HDF magic word 
	 * at the beginning.
	 *
	 * @return <code>true</code> if this file has a valid HDF magic word
	 */
	private boolean checkMagicWord() {
		final HDFileFilter filter=new HDFileFilter(false);
		return filter.accept(getFile());
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
		DataObject.clear();
	}
	/**
	 * Add default objects always needed.
	 * 
	 * Almost all of Jam's number storage needs are satisfied by the type
	 * hard-coded into the class <code>NumberType</code>.  This method
	 * creates the <code>NumberType</code> object in the file
	 * that gets referred to repeatedly by the other data elements.
	 *
	 * @see jam.io.hdf.NumberType
	 */
	
	void addDefaultDataObjects(String fileID) {
		new LibVersion(); //DataObjects add themselves
		
		NumberType.createDefaultTypes();
		
		new JavaMachineType();
		new FileIdentifier(fileID);
		addFileNote();
		
	}			

	/**
	 * Add a text note to the file, which includes the state of 
	 * <code>JamProperties</code>.
	 *
	 * @throws IOException if there's a problem writing to the file
	 * @see jam.global.JamProperties
	 */
	void addFileNote() {
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
		try {
			JamProperties.getProperties().store(baos,header);			
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to serialize properties");
		}

		final String notation = baos.toString()+noteAddition;
		new FileDescription(notation);
	}
	
}
