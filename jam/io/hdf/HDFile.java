package jam.io.hdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;

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
	 * Check if a file is an HDF file
	 * @param file
	 * @return true if file is an hdf file
	 */
	static boolean isHDFFile(File file) {
		final HDFileFilter filter=new HDFileFilter(false);
		return filter.accept(file);
	}
	
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
		this.file = file;
	}
	
	/**
	 * Write a hdf file from all DataObjects
	 * 
	 * @throws HDFException
	 * @throws IOException
	 */
	void writeFile(final ProgressMonitor pm) throws HDFException, IOException {
	
        setOffsets();            
        writeMagicWord();	      
        writeDataDescriptorBlock();
        writeAllObjects(pm);

	}
	/**
	 * Looks at the internal index of data elements and sets the offset
	 * fields of the <code>DataObject</code>'s. To be run when all data
	 * elements have been defined.
	 */
	private synchronized void setOffsets() {
		//final int DDblockSize = 2 + 4 + 12 * objectList.size();
		final int initialOffset = sizeDataDescriptorBlock() + 4; //add in HDF file header
		int counter = initialOffset;
		List objectList = DataObject.getDataObjectList();
		final Iterator temp = objectList.iterator();
		while (temp.hasNext()) {
			final DataObject ob = (DataObject) (temp.next());
			ob.setOffset(counter);
			counter += ob.getLength();
		}
	}
	
	private final int sizeDataDescriptorBlock(){
	    /* The size of the DD block. */
		/* numDD's + offset to next (always 0 here) + size*12 for
		 * tag/ref/offset/length info */
		List objectList = DataObject.getDataObjectList();
	    return 2 + 4 + 12 * objectList.size();
	}


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
	 *
	 * @exception HDFException unrecoverable errror
	 */
	private synchronized void writeDataDescriptorBlock() throws HDFException {
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
	private void writeAllObjects(final ProgressMonitor pm) throws HDFException {
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
	 * Reads file into set of DataObject's and sets their internal
	 * variables.
	 * 
	 *  @exception HDFException unrecoverable error
	 */
	public void readFile() throws HDFException {
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
	 * Helper for reading objects
	 *  @exception IOException unrecoverable error
	 */
	private synchronized void mark() throws IOException {
            mark = getFilePointer();
    }
	
	/**
	 * Helper for reading objects
	 *  @exception IOException unrecoverable error
	 */
	private synchronized void reset() throws IOException {
		seek(mark);
	}


	/**
	 * Checks whether this file contains the HDF magic word 
	 * at the beginning.
	 *
	 * @return <code>true</code> if this file has a valid HDF magic word
	 */
	boolean checkMagicWord() {
		final HDFileFilter filter=new HDFileFilter(false);
		return filter.accept(file);
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
	}

}
