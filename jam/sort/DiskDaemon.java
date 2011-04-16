package jam.sort;

import injection.GuiceInjector;
import jam.sort.stream.EventException;
import jam.util.NumberUtilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.logging.Level;

/**
 * Writes events to disk and reads them back.
 * @author Ken Swartz
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 */
public final class DiskDaemon extends AbstractStorageDaemon {

    private transient BufferedInputStream bis;

    private transient BufferedOutputStream bos;

    private transient boolean reachedRunEnd = false;

    private transient final Object rreLock = new Object();

    /**
     * @see AbstractStorageDaemon#AbstractStorageDaemon(Controller)
     */
    public DiskDaemon(final Controller controller) {
        super(controller);
        setName("Disk I/O for Event Data");
    }

    /**
     * Returns whether online sorting is all caught up with incoming buffers.
     * @return <code>true</code> if all received buffers have been sorted
     */
    public boolean caughtUpOnline() {
        if (ringBuffer == null) {
            throw new IllegalStateException(
                    "Should always have a ring buffer here.");
        }
        boolean rval = false;
        if (ringBuffer.isEmpty()) {
            synchronized (rreLock) {
                if (reachedRunEnd) {
                    rval = true;
                }
            }
        }
        return rval;
    }

    /**
     * Closes file that was written to or read from.
     * @exception SortException
     *                exception that sends message to console
     */
    @Override
    public void closeEventInputFile() throws SortException {
        if (inputFileOpen) {
            try {
                bis.close();
                inputFileOpen = false;
            } catch (IOException ioe) {
                throw new SortException("Unable to close file [DiskDaemon]",
                        ioe);
            }
        }
    }

    /**
     * Close event input file that is from the list, if one from the list is
     * open.
     */
    @Override
    public boolean closeEventInputListFile() {
        boolean rval = true;
        try {
            closeEventInputFile();
        } catch (SortException ioe) {
            LOGGER.log(Level.SEVERE,
                    "Unable to close file: " + inputFile.getPath(), ioe);
            rval = false;
        }
        return rval;
    }

    /**
     * Closes file that was written to or read from.
     * @exception SortException
     *                exception that sends message to console
     */
    @Override
    public void closeEventOutputFile() throws SortException {
        if (outputFileOpen) {
            try {
                if (mode == Mode.OFFLINE) {
                    eventOutput.writeEndRun();
                }
                bos.close();// flushes bos, then closes underlying stream
                outputFileOpen = false;
            } catch (EventException ee) {
                throw new SortException("Unable to close file EventException:"
                        + ee.getMessage() + " [DiskDaemon]", ee);
            } catch (IOException ioe) {
                throw new SortException("Unable to close file [DiskDaemon]",
                        ioe);
            }
        }
    }

    /**
     * Implementation of <code>StorageDaemon</code> abstract method.
     * @exception SortException
     *                thrown for unrecoverable errors
     */
    @Override
    public InputStream getEventInputFileStream() throws SortException {
        return bis;
    }

    /* implementations of StorageDeamon abstract methods */

    /**
     * Implementation of <code>StorageDaemon</code> abstract method.
     * @exception SortException
     *                thrown for unrecoverable errors
     */
    @Override
    public OutputStream getEventOutputFileStream() throws SortException {
        return bos;
    }

    /**
     * Need to implement such that sets a variable to stop write loop.
     */
    @Override
    public boolean hasMoreFiles() {
        return sortFiles.hasNext();
    }

    /**
     * Open file to write events to.
     * @exception SortException
     *                exception that sends message to console
     */
    @Override
    public void openEventInputFile(final File file) throws SortException {
        if (file == null) {
            final SortException exception = new SortException(getClass()
                    .getName() + ": Cannot open input event file, null name.");
            LOGGER.throwing("DiskDaemon", "openEventInputFile", exception);
            throw exception;
        }
        try {
            final FileInputStream fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis, RingBuffer.BUFFER_SIZE);
            eventInput.setInputStream(bis);
            inputFile = file;
            inputFileOpen = true;
        } catch (IOException ioe) {
            throw new SortException("Unable to open file: " + file.getPath()
                    + " [DiskDaemon]", ioe);
        }
    }

    /**
     * Open next file in list.
     */
    @Override
    public boolean openEventInputListFile() {
        boolean rval = false;
        final File file = sortFiles.next();
        try {
            openEventInputFile(file);// local open file method
            rval = eventInput.readHeader();
            if (rval) {
                fileCount++;
            } else {
                LOGGER.severe("File does not have correct header. File: "
                        + file.getAbsolutePath());
            }
        } catch (EventException ee) {
            LOGGER.log(Level.SEVERE, ee.getMessage(), ee);
            rval = false;
        } catch (SortException je) {
            LOGGER.log(Level.SEVERE, je.getMessage(), je);
            rval = false;
        }
        return rval;
    }

    /**
     * Open file to write events to.
     * @exception SortException
     *                exception that sends message to console
     */
    @Override
    public void openEventOutputFile(final File file) throws SortException {
        if (file == null) {
            throw new SortException(getClass().getName()
                    + ": Cannot open output event file, file name is null.");
        }
        try {
            final FileOutputStream fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, RingBuffer.BUFFER_SIZE);
            eventOutput.setOutputStream(bos);
            this.outputFile = file;
            outputFileOpen = true;
        } catch (IOException ioe) {
            throw new SortException("Unable to open file: " + file.getPath()
                    + " [DiskDaemon]", ioe);
        }
    }

    /**
     * Implementation of <code>StorageDaemon</code> abstract method.
     * @exception SortException
     *                thrown for unrecoverable errors
     */
    @Override
    public boolean readHeader() throws SortException {
        try {
            final BufferedInputStream headerInputStream = new BufferedInputStream(
                    new FileInputStream(inputFile), eventInput.getHeaderSize());
            eventInput.setInputStream(headerInputStream);
            final boolean goodHeader = eventInput.readHeader();
            headerInputStream.close();
            return goodHeader;
        } catch (EventException ioe) {
            throw new SortException("Could not read header record.", ioe);
        } catch (FileNotFoundException fnf) {
            throw new SortException("Event file not found.", fnf);
        } catch (IOException ioe) {
            throw new SortException("Problem closing event file.", ioe);
        }

    }

    /**
     * Reset the "reached run end" state to <code>false</code>.
     */
    public void resetReachedRunEnd() {
        synchronized (rreLock) {
            reachedRunEnd = false;
        }
    }

    /**
     * Starting point of thread for online writing to disk
     */
    @Override
    public void run() {
        try {
            if (mode == Mode.ONLINE) {
                writeLoop();
            } else {
                throw new IllegalStateException(
                        "run() called when mode not ONLINE");
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Error while writing data to file: "
                    + ioe.getMessage(), ioe);
        }
    }

    /**
     * Implementation of <code>StorageDaemon</code> abstract method.
     * @exception SortException
     *                thrown for unrecoverable errors
     */
    @Override
    public void writeHeader() throws SortException {
        try {
            eventOutput.writeHeader();
        } catch (EventException ioe) {
            throw new SortException(
                    "Could not write Header Record [DiskDaemon]", ioe);
        }
    }

    /*
     * non-javadoc: Take data from ring buffer and write it out to a file until
     * you see a end of run marker, then inform controller.
     */
    private void writeLoop() throws IOException {
        final NumberUtilities numberUtilities = GuiceInjector
                .getObjectInstance(NumberUtilities.class);
        final byte[] buffer = GuiceInjector.getObjectInstance(
                RingBufferFactory.class).freshBuffer();
        final int offset = buffer.length - 2;
        /*
         * checkState() waits until state is STOP (return value=false) or RUN
         * (return value=true)
         */
        while (checkState()) {
            // read from pipe and write file
            try {
                ringBuffer.getBuffer(buffer);
            } catch (InterruptedException e) {
                // Not using IOException(Throwable) constructor to retain
                // J2SE 5 compatibility.
                throw new IOException(e.getMessage());// NOPMD
            }
            bos.write(buffer);
            bufferCount++;
            // check for end-of-run marker
            final short last2bytes = numberUtilities.bytesToShort(buffer,
                    offset, ByteOrder.BIG_ENDIAN);
            if (eventInput.isEndRun(last2bytes)) {
                // tell control we are done
                fileCount++;
                synchronized (rreLock) {
                    reachedRunEnd = true;
                }
                controller.atWriteEnd();
            }
            yield();
        }
        // end loop forever
    }
}
