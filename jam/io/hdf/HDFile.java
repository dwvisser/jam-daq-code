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
 * Class which reads and writes DataObjects to and from
 * HDF files on disk.
 *
 * @author 	Dale Visser, Ken Swartz
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
	private AsyncProgressMonitor asyncProgressMonitor;
	private int stepsProgress;
	
	/**
	 * variable for marking position in file
	 */
	private transient long mark = 0;

	/**
	 * Constructor called with a <code>File</code> object, and an access
	 * mode.
	 *
	 * @param file file to be accessed
	 * @param mode "r" or "rw"
	 * @exception FileNotFoundException if given file not found
	 */
	public HDFile(File file, String mode, AsyncProgressMonitor asyncProgressMonitor, int stepsProgress) throws FileNotFoundException {
		super(file, mode);
		this.asyncProgressMonitor=asyncProgressMonitor;
		this.stepsProgress=stepsProgress;
	}
	
	/**
	 * Constructor called with a <code>File</code> object, and an access
	 * mode.
	 *
	 * @param file file to be accessed
	 * @param mode "r" or "rw"
	 * @exception FileNotFoundException if given file not found
	 */
	public HDFile(File file, String mode) throws FileNotFoundException {
		super(file, mode);
		stepsProgress=0;	//ignores progress
	}
	
	/* non-javadoc:
	 * Write a hdf file from all DataObjects
	 * 
	 * @throws HDFException
	 */
	void writeFile() throws HDFException {
		updateBytesOffsets();            
        writeMagicWord();	      
        writeDataDescriptorBlock();
        writeAllObjects();
	}
	
	/**
	 * Looks at the internal index of data elements and sets the offset
	 * fields of the <code>DataObject</code>'s. To be run when all data
	 * elements have been defined.
	 */
	private synchronized void updateBytesOffsets() {
		//final int DDblockSize = 2 + 4 + 12 * objectList.size();
		final int initOffset = sizeDataDescriptorBlock() + 4; //add in HDF file header
		int counter = initOffset;
		final Iterator temp = DataObject.getDataObjectList().iterator();
		while (temp.hasNext()) {
			final DataObject dataObject = (DataObject) (temp.next());
			dataObject.refreshBytes();
			dataObject.setOffset(counter);
			counter += dataObject.getBytes().capacity();
		}
	}
	
	private final int sizeDataDescriptorBlock(){
	    /* The size of the DD block. */
		/* numDD's + offset to next (always 0 here) + size*12 for
		 * tag/ref/offset/length info */
		final int size = DataObject.getDataObjectList().size();
	    return 2 + 4 + 12 * size;
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
		final List objectList = DataObject.getDataObjectList();
		try {
			seek(4); //skip header
			writeShort(objectList.size()); //number of DD's
			writeInt(0); //no additional descriptor block
			final Iterator temp = objectList.iterator();
			while (temp.hasNext()) {
				final DataObject dataObject = (DataObject) (temp.next());
				writeShort(dataObject.getTag());
				writeShort(dataObject.getRef());
				writeInt(dataObject.getOffset());
				writeInt(dataObject.getBytes().capacity());
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
	private void writeAllObjects() throws HDFException {
		final List objectList = DataObject.getDataObjectList();
		
		int numberObjctProgressStep;
		int countObjct=0;
		
		numberObjctProgressStep=getNumberObjctProgressStep(objectList.size());
		
		final Iterator temp = objectList.iterator();
		boolean foundEmpty = false;
		writeLoop: while (temp.hasNext()) {
			if (countObjct%numberObjctProgressStep==0) {
				asyncProgressMonitor.increment();
			}
			
			final DataObject dataObject = (DataObject) (temp.next());
			if (dataObject.getBytes().capacity() == 0){
			    foundEmpty=true;
			    break writeLoop;
			}
			writeDataObject(dataObject);
			countObjct++;
		}
		if (foundEmpty){
			throw new HDFException("DataObject with no length encountered, halted writing HDF File.");
		}
	}
		
	private void setProgress(final ProgressMonitor monitor, final int value){
		final Runnable runner=new Runnable(){
			public void run(){
				monitor.setProgress(value);					
			}
		};
		SwingUtilities.invokeLater(runner);
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
			write(data.getBytes().array());
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
		
		int numberObjctProgressStep;
		int countObjct=0;
			
		try {
			if (!checkMagicWord()) {
				throw new HDFException("Not an hdf file");
			}
			
			seek(4);
			boolean doAgain = true;
			do {
				final int numDD = readShort(); //number of DD's
				final int nextBlock = readInt();
				//Just one dd block for now
				numberObjctProgressStep=getNumberObjctProgressStep(numDD);
				
				for (int i = 1; i <= numDD; i++) {
					final short tag = readShort();
					final short ref = readShort();
					final int offset = readInt();
					final int length = readInt();
					final byte [] bytes = readBytes(offset,length);
					DataObject.create(bytes,tag,ref,offset,length);
					
					if (countObjct%numberObjctProgressStep==0) {
						if (asyncProgressMonitor!=null)	//FIXME KBS cleanup
							asyncProgressMonitor.increment();
					}
					countObjct++;
				}
				if (nextBlock == 0) {
					doAgain = false;
				} else {
					seek(nextBlock);
				}
			} while (doAgain);
			/*FIXME KBS remove
			final Iterator temp = DataObject.getDataObjectList().iterator();
			while (temp.hasNext()) {
				final DataObject dataObject = (DataObject) (temp.next());
				dataObject.interpretBytes();
			}
			*/
			
		} catch (IOException e) {
			throw new HDFException(
				"Problem reading HDF file objects. ",e);
		}
	}
	
	private byte [] readBytes(int offset, int length) throws IOException {
		final byte [] rval = new byte[length];
		mark();
		seek(offset);
		read(rval);
		reset();
		return rval;
	}
	
	/* non-javadoc:
	 * Checks whether this file contains the HDF magic word 
	 * at the beginning.
	 *
	 * @return <code>true</code> if this file has a valid HDF magic word
	 */
	private boolean checkMagicWord() throws IOException {
		seek(0);
		final int magicInt =readInt();
		return (magicInt == HDF_HEADER);
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
	 * First, calls <code>super.close()</code>, then clears collections of temporary objects used
	 * to build the file, and then sets their references to
	 * to <code>null</code>. 
	 * 
	 * @see java.io.RandomAccessFile#close()
	 */
	public void close() throws IOException{
		super.close();
	}

	private int getNumberObjctProgressStep(int numberObjects) {
		int numberObjctProgressStep;
		int countObjct=0;
		if (stepsProgress>0) {
			numberObjctProgressStep =numberObjects/stepsProgress;
			if (numberObjctProgressStep <=0)
				numberObjctProgressStep=1;
		} else {
			numberObjctProgressStep =1;
		}
		return numberObjctProgressStep;
	}

}
