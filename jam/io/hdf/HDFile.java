package jam.io.hdf;

import static jam.io.hdf.Constants.DFTAG_NULL;
import static jam.io.hdf.Constants.HDF_HEADER;
import static jam.io.hdf.Constants.HEADER_BYTES;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.logging.Logger;

/**
 * Class which reads and writes DataObjects to and from HDF files on disk.
 * 
 * @author Dale Visser
 * @author Ken Swartz
 * @since JDK1.1
 */
public final class HDFile extends RandomAccessFile {
	private static class FractionTime {
		private FractionTime() {
			super();
		}

		public static final float READ_ALL = 1.0f;

		public static final float READ_LAZY_HISTS = 0.7f;

		public static final float READ_NOT_HIST = 0.3f;

		public static final float WRITE_ALL = 1.0f;
	}

	private static final Logger LOGGER = Logger.getLogger(HDFile.class
			.getPackage().getName());

	/**
	 * Check if a file is an HDF file
	 * 
	 * @param file
	 * @return true if file is an hdf file
	 */
	protected static boolean isHDFFile(final File file) {
		final HDFileFilter filter = new HDFileFilter(false);
		return filter.accept(file);
	}

	/** Count number of data object that have been lazy loaded */
	private transient int lazyCount;

	private boolean lazyLoadData = true;

	/** Number of data objects to lazy load */
	private transient int lazyLoadNum;

	/**
	 * variable for marking position in file
	 */
	private transient long markPosition = 0;

	private transient final AsyncProgressMonitor monitor;

	private transient final int stepsToTake;

	/**
	 * Constructor called with a <code>File</code> object, and an access mode.
	 * 
	 * @param file
	 *            file to be accessed
	 * @param mode
	 *            "r" or "rw"
	 * @exception FileNotFoundException
	 *                if given file not found
	 */
	public HDFile(final File file, final String mode)
			throws FileNotFoundException {
		this(file, mode, null, 0);// ignores progress
	}

	/**
	 * Constructor called with a <code>File</code> object, and an access mode.
	 * 
	 * @param file
	 *            file to be accessed
	 * @param mode
	 *            "r" or "rw"
	 * @param progMon
	 *            progress monitor
	 * @param steps
	 *            to take to completion
	 * @exception FileNotFoundException
	 *                if given file not found
	 */
	HDFile(final File file, final String mode,
			final AsyncProgressMonitor progMon, final int steps)
			throws FileNotFoundException {
		super(file, mode);
		monitor = progMon;
		stepsToTake = steps;
	}

	/*
	 * non-javadoc: Checks whether this file contains the HDF magic word at the
	 * beginning.
	 * 
	 * @return <code>true</code> if this file has a valid HDF magic word
	 */
	private boolean checkMagicWord() throws IOException {
		seek(0);
		final int magicInt = readInt();
		return (magicInt == HDF_HEADER);
	}

	/*
	 * non-javadoc: Count objects in file
	 * 
	 * @return @throws IOException
	 */
	private int countHDFOjects() throws IOException {

		int numberObjects = 0;

		seek(HEADER_BYTES);

		boolean hasNextBlock = true;
		while (hasNextBlock) {
			final int numDD = readShort(); // number of DD in block
			final int nextBlock = readInt(); // Next DD block
			numberObjects += numDD;
			if (nextBlock == 0) {
				hasNextBlock = false;
			} else {
				seek(nextBlock);
			}
		}
		return numberObjects;
	}

	private static final String REF = " ref ";

	/*
	 * non-javadoc: Fixes reference for old files @param ref
	 */
	protected void debugDumpDD(final short tag, final short ref,
			final int offset, final int length) throws IOException {
		mark();
		seek(offset);
		LOGGER.info("Read Tag " + tag + REF + ref + " offset " + offset
				+ " length " + length);
		if (tag == Constants.DFTAG_NDG) {
			final short propTag1 = readShort();
			final short propRef1 = readShort();
			final short propTag2 = readShort();
			final short propRef2 = readShort();
			LOGGER.info("  NDG  propTag1 " + propTag1 + " propRef1 " + propRef1
					+ " propTag2 " + propTag2 + " propRef2 " + propRef2);
		} else if (tag == Constants.DFTAG_VG) {
			final short numItems = readShort();
			final short[] tags = new short[numItems];
			final short[] refs = new short[numItems];
			for (int i = 0; i < numItems; i++) {
				tags[i] = readShort();
			}
			for (int i = 0; i < numItems; i++) {
				refs[i] = readShort();
			}
			for (int i = 0; i < numItems; i++) {
				LOGGER.info("  VG  num " + i + " tag " + tags[i] + REF
						+ refs[i]);
			}
		}
		reset();
	}

	private int getNumberObjctProgressStep(final int numObjects,
			final float timeFraction) {
		int rval = 1;
		if (stepsToTake > 0) {
			rval = numObjects / (stepsToTake - 1);
			if (lazyLoadData) { // half the steps if lazy load (redo to take
				// care of round off)
				rval = (int) (numObjects / (stepsToTake - 1.0) / timeFraction);
			}
			if (rval <= 0) {
				rval = 1;
			}
		}
		return rval;
	}

	protected boolean isLazyLoadData() {
		return lazyLoadData;
	}

	/*
	 * non-javadoc: Lazy load the bytes for an object
	 */
	protected byte[] lazyReadData(final AbstractData dataObject)
			throws HDFException {
		final int numObjSteps = getNumberObjctProgressStep(lazyLoadNum,
				FractionTime.READ_LAZY_HISTS);
		final byte[] localBytes = new byte[dataObject.getLength()];
		try {
			seek(dataObject.getOffset());
			read(localBytes);
			if (lazyCount % numObjSteps == 0 && monitor != null) {
				monitor.increment();
			}
			lazyCount++;
		} catch (IOException e) {
			throw new HDFException("Problem lazy reading data objects. ", e);
		}
		return localBytes;
	}

	/**
	 * Helper for reading objects
	 * 
	 * @exception IOException
	 *                unrecoverable error
	 */
	private void mark() throws IOException {
		synchronized (this) {
			markPosition = getFilePointer();
		}
	}

	private byte[] readBytes(final int offset, final int length)
			throws IOException {
		final byte[] rval = new byte[length];
		mark();
		seek(offset);
		final int numRead = read(rval);
		if (numRead < length) {
			throw new IllegalStateException("Tried to read " + length
					+ " bytes from file. Only got " + numRead + ".");
		}
		reset();
		return rval;
	}

	/**
	 * Reads file into set of DataObject's and sets their internal variables.
	 * 
	 * @exception HDFException
	 *                unrecoverable error
	 */
	public void readFile() throws HDFException {
		int countObjct = 0;
		lazyLoadNum = 0;
		lazyCount = 0;
		try {
			if (!checkMagicWord()) {
				throw new HDFException("Not an hdf file.");
			}
			final int numObjSteps = getNumberOfSteps();
			seek(HEADER_BYTES);
			boolean hasNextBlock = true;
			while (hasNextBlock) {
				final int numDD = readShort(); // number of DD's
				final int nextBlock = readInt();
				for (int i = 1; i <= numDD; i++) {
					final short tag = readShort();
					final short ref = readShort();
					final int offset = readInt();
					final int length = readInt();
					// Debug
					LOGGER.fine("Read Tag " + tag + REF + ref + " offset "
							+ offset + " length " + length);
					if (tag != DFTAG_NULL) {// Not an empty tag
						loadDataObject(tag, ref, offset, length);
					}
					countObjct++;
					// Update progress bar
					if (countObjct % numObjSteps == 0 && monitor != null) {
						monitor.increment();
					}
				}
				if (nextBlock == 0) {
					hasNextBlock = false;
				} else {
					seek(nextBlock);
				}
			}
		} catch (IOException e) {
			throw new HDFException("Problem reading HDF file objects. ", e);
		}
	}

	/**
	 * @param tag
	 * @param ref
	 * @param offset
	 * @param length
	 * @throws HDFException
	 * @throws IOException
	 */
	private void loadDataObject(final short tag, final short ref,
			final int offset, final int length) throws HDFException,
			IOException {
		// Load scientific data as last moment needed
		if (lazyLoadData && tag == Constants.DFTAG_SD) {
			AbstractData.create(ScientificData.class, ref, offset, length);
			lazyLoadNum++;
		} else {
			final byte[] bytes = readBytes(offset, length);
			AbstractData.create(bytes, AbstractData.TYPES.get(tag), ref);
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private int getNumberOfSteps() throws IOException {
		int numObjSteps;
		if (lazyLoadData) {
			numObjSteps = getNumberObjctProgressStep(countHDFOjects(),
					FractionTime.READ_NOT_HIST);
		} else {
			numObjSteps = getNumberObjctProgressStep(countHDFOjects(),
					FractionTime.READ_ALL);
		}
		return numObjSteps;
	}

	/**
	 * Helper for reading objects
	 * 
	 * @exception IOException
	 *                unrecoverable error
	 */
	private void reset() throws IOException {
		synchronized (this) {
			seek(markPosition);
		}
	}

	protected void setLazyLoadData(final boolean lazy) {
		lazyLoadData = lazy;
	}

	private int sizeDataDescriptorBlock() {
		/* The size of the DD block. */
		/*
		 * numDD's + offset to next (always 0 here) + size12 for
		 * tag/ref/offset/length info
		 */
		final int size = AbstractData.getDataObjectList().size();
		return 2 + 4 + 12 * size;
	}

	/**
	 * Looks at the internal index of data elements and sets the offset fields
	 * of the <code>DataObject</code>'s. To be run when all data elements have
	 * been defined.
	 */
	private void updateBytesOffsets() {
		synchronized (this) {
			final int initOffset = sizeDataDescriptorBlock() + 4;
			// file header
			int counter = initOffset;
			for (AbstractData dataObject : AbstractData.getDataObjectList()) {
				dataObject.refreshBytes();
				dataObject.setOffset(counter);
				counter += dataObject.getBytes().capacity();
			}
		}
	}

	/*
	 * non-javadoc: Called after all <code>DataObject</code> objects have been
	 * created.
	 * 
	 * @exception HDFException thrown if err occurs during file write
	 */
	private void writeAllObjects() throws HDFException {
		final List<AbstractData> objectList = AbstractData.getDataObjectList();
		int countObjct = 0;
		final int numObjSteps = getNumberObjctProgressStep(objectList.size(),
				FractionTime.WRITE_ALL);
		writeLoop: for (AbstractData dataObject : objectList) {
			if (countObjct % numObjSteps == 0 && monitor != null) {
				monitor.increment();
			}
			if (dataObject.getBytes().capacity() == 0) {
				break writeLoop;
			}
			writeDataObject(dataObject);
			countObjct++;
		}
	}

	/**
	 * 
	 * @exception HDFException
	 *                unrecoverable errror
	 */
	private void writeDataDescriptorBlock() throws HDFException {
		synchronized (this) {
			final List<AbstractData> objectList = AbstractData
					.getDataObjectList();
			try {
				seek(HEADER_BYTES); // skip header
				writeShort(objectList.size()); // number of DD's
				writeInt(0); // no additional descriptor block
				for (AbstractData dataObject : objectList) {
					writeShort(dataObject.getTag());
					writeShort(dataObject.getRef());
					writeInt(dataObject.getOffset());
					writeInt(dataObject.getBytes().capacity());
					// Debug
					LOGGER.fine("Write Tag " + dataObject.getTag() + REF
							+ dataObject.getRef() + " offset "
							+ dataObject.getOffset() + " length "
							+ dataObject.getBytes().capacity());

				}
			} catch (IOException e) {
				throw new HDFException("Problem writing DD block.", e);
			}
		}
	}

	/**
	 * Given a data object, writes out the appropriate bytes to the file on
	 * disk.
	 * 
	 * @param data
	 *            HDF data element
	 * @exception HDFException
	 *                thrown if unrecoverable error occurs
	 */
	private void writeDataObject(final AbstractData data) throws HDFException {
		try {
			seek(data.getOffset());
			write(data.getBytes().array());
		} catch (IOException e) {
			throw new HDFException("Problem writing HDF data object.", e);
		}
	}

	/*
	 * non-javadoc: Write a hdf file from all DataObjects
	 * 
	 * @throws HDFException
	 */
	protected void writeFile() throws HDFException {
		updateBytesOffsets();
		writeMagicWord();
		writeDataDescriptorBlock();
		writeAllObjects();
	}

	/**
	 * Writes the unique 4-byte pattern at the head of the file denoting that it
	 * is an HDF file.
	 * 
	 * @throws HDFException
	 *             error with writing hdf file
	 */
	private void writeMagicWord() throws HDFException {
		try {
			seek(0);
			writeInt(HDF_HEADER);
		} catch (IOException e) {
			throw new HDFException("Problem writing HDF header.", e);
		}
	}

}
