/*
 */
package jam.sort;
import java.util.*;
import java.io.*;
import jam.global.*;
import jam.sort.stream.*;

/**
 * <p>CURRENTLY UNTESTED.  Tape deamon to read and write event data for tapes.
 * Implementation of this has been limited by the varying ways in which OS's
 * handle tape drives.</p>
 * <p>The modes are:</p>
 * <dl>
 * <dt>online</dt><dd>to collect data from experiment -> tape</dd>
 * <dt>offline</dt><dd>to collect bytes from tape drive -> pipe</dd>
 * </dl>
 * <p>The reads from tape "files" on unix seem only capable of returning 1 byte at 
 * a time.  Buffering probably goes on behind the scenes, but is not available
 * directly to the user.  </p>
 * 
 * @author Dale Visser
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */
public class TapeDaemon extends StorageDaemon {

	//rewind command command= REWIND_COMMMAND1+noRewindName+REWIND_COMMAND2                   
	private static final String REWIND_COMMAND1 = "mt -f ";
	private static final String REWIND_COMMAND2 = " rewind";

	private File rewindDevice;
	private File noRewindDevice;
	private static String noRewindName;

	private boolean firstOpen;
	private boolean firstInList = true;
	int numberOfRuns;
	int currentRunNumber;

	byte lastByte[];

	private Date now;
	int status;
	int eventNumber = 0;

	//counters of what we have written to tape
	private int recordByteCounter = 0; //number bytes this record
	private int tapeByteCounter = 0; //number bytes this tape
	private int blockCounter = 0; //number of blocks

	/**
	 * Creates a <code>TapeDaemon</code> for online sorting.
	 *
	 *
	 * @param controller the process controlling sorting
	 * @param eventOutput the stream events are written to
	 * @param msgHandler conslole to write user messages to
	 */
	public TapeDaemon(Controller controller, MessageHandler msgHandler) {
		super(controller, msgHandler);
		firstOpen = true;
	}

	/**
	 * set the path of the tape device 
	 * @param tapeDev the name of the tape device     
	 */
	public void setDevice(String device) {
		rewindDevice = new File(device);
		noRewindDevice = new File(device + "n");
		noRewindName = device;
	}
	/**
	 * The rewind device
	 */
	public File getRewindDevice() {
		return rewindDevice;
	}
	/**
	 * The no rewind device
	 */
	public File getNoRewindDevice() {
		return noRewindDevice;
	}
	/**
	 * Writes header block to tape.
	 *
	 * @exception   SortException    writes error messages to the console
	 */
	public void writeHeader() throws SortException {

		try {
			System.out.println("tapeDaemon writing header ");
			eventOutput.writeHeader();

			fos.close();
		} catch (IOException ioe) {
			throw new SortException("Could not write run Header to tape IOException [TapeDaemon]");

		} catch (EventException ioe) {
			throw new SortException("Could not write run Header to tape EventException [TapeDaemon]");
		}
		tapeByteCounter = tapeByteCounter + eventOutput.getHeaderSize();
		recordByteCounter = 0;
	}
	/**
	 *
	 */
	public void openEventInputFile(File file) throws SortException {

		try {
			//FIXME	    if(firstOpen){
			//		System.out.println("open tape device"+rewindDevice);			    
			//		fis = new FileInputStream(rewindDevice);
			//		firstOpen=false;

			//	    } else {

			fis = new FileInputStream(noRewindDevice);
			bis = new BufferedInputStream(fis, RECORD_SIZE);
			eventInput.setInputStream(bis);
			//	    }	    

		} catch (FileNotFoundException fnf) {
			System.err.println("Error openTapeFile: " + fnf);
			throw new SortException(
				"Not able to open tape Device: " + noRewindDevice);
		}

	}
	/**
	 * Open file to write data to fileName used for header information.
	 *
	 * @exception   SortException    writes error messages to the console
	 */
	public void openEventOutputFile(File file) throws SortException {
		try {
			fos = new FileOutputStream(noRewindDevice);
			bos = new BufferedOutputStream(fos, RECORD_SIZE);
			eventOutput.setOutputStream(bos);
		/*} catch (FileNotFoundException fnf) {
			throw new SortException(
				"Not able to open tape Device: " + noRewindDevice);*/
		} catch (IOException ioe) {
			throw new SortException(
				"Not able to open tape Device: IOException" + noRewindDevice);
		}
	}

	/**
	 * close tape that was read from
	 *
	 */
	public void closeEventInputFile() throws SortException {

		if (fis != null) {
			try {
				fis.close();
			} catch (IOException ioe) {
				System.err.println("closeTapeFile: " + ioe);
				throw new SortException("Unable to close input from tape [TapeDaemon]");

			}
		}
	}
	/**
	 * close file data written to fileName used for header information.
	 */
	public void closeEventOutputFile() throws SortException {
		//XXXSystem.out.println("    closing Output tape file");
		try {
			dos.flush();
			fos.close();
		} catch (IOException ioe) {
			System.err.println("closeTapeFile: " + ioe);
			throw new SortException("Unable to close output to tape file [TapeDaemon]");

		}
	}

	public boolean hasMoreFiles() {
		return (!sortFilesList.isEmpty());
	}
	/**
	 * Gets next run in the list.
	 *
	 */
	public boolean openEventInputListFile() {

		int runNumber = 0;
		boolean goodHeader = false;
		boolean runFound = false;

		//makes sure tape is closed before we start
		try {
			closeEventInputFile();

			//look until we find a run in the list
			while (!runFound) {

				//able to open a tape	    
				openEventInputFile(null);
				//read header of file 
				goodHeader = readHeader();
				//this is a header and we run number is in run list
				runNumber = eventInput.headerRunNumber;
				if (goodHeader && inRunList(runNumber)) {
					System.out.println("**A good header and run in list**");
					tapeByteCounter += eventInput.getHeaderSize();
					//inputFileName = "" + runNumber;
					fileCount++; //increment file counter
					runFound = true;

				}

				//not in run list so close tape
				if (!runFound) {
					closeEventInputFile();
				}

			}
		} catch (SortException se) {
			msgHandler.errorOutln(se.getMessage());
			return false;
		} catch (EventException ee) {
			msgHandler.errorOutln(ee.getMessage());
			return false;
		}

		return runFound;
	}
	/**
	 * close file that was on the list
	 */
	public boolean closeEventInputListFile() {
		System.out.println("close event input list file");
		try {
			closeEventInputFile();
		} catch (SortException se) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * is the run in the list of runs
	 *
	 */
	private boolean inRunList(int runNumber) {
		System.out.print(
			"   inRunList run "
				+ runNumber
				+ "   number runs left "
				+ numberOfRuns);

		int numberOfRuns = sortFilesList.size();
		int counter = 0;

		while (counter < numberOfRuns) {
			if (((Integer) sortFilesList.get(counter)).intValue()
				== runNumber) {
				sortFilesList.remove(counter);
				System.out.println("   run is in list");
				return true;
			}
			counter++;
		}
		System.out.println("    run not in list");
		return false;
	}
	/**
	 * Get the next stream to read.
	 *
	 * @exception   SortException    writes error messages to the console
	 * @exception   EventException   thrown when there is a problem with the event stream
	 */
	public InputStream getEventInputFileStream()
		throws SortException, EventException {
		System.out.println(
			"getEventInputFileStream runNumber " + currentRunNumber);
		return bis;
	}
	/**
	 * Get the next stream to write to
	 *
	 * @exception   SortException    writes error messages to the console
	 * @exception   EventException   thrown when there is a problem with the event stream
	 */

	public OutputStream getEventOutputFileStream()
		throws SortException, EventException {
		return bos;
	}

	/**
	 * main loop 
	 */
	public void run() {
		try {
			//if (mode==WRITE){
			toTapeRun();
			//} else {
			//System.out.println("Error tapeDaemon, theres is no run for reading ");
			//}
			//FIXME should we have ioe catch here?	    
		} catch (IOException ioe) {
			System.out.println(ioe);
			msgHandler.errorOutln(ioe.getMessage());

		} catch (EventException ee) {
			System.out.println(ee);
			msgHandler.errorOutln(ee.getMessage());
		}

	}
	/**
	 * Read an event header.
	 *
	 * @exception   EventException   thrown when there is a problem with the event stream
	 */
	public boolean readHeader() throws EventException {
		//XXXSystem.out.println("readHeader");        
		return (eventInput.readHeader());
	}

	/**
	 * write data to tape
	 */
	private void toTapeRun() throws IOException, EventException {
		boolean endOfRun;
		short last2bytes;

		int counter = 0;

		System.out.println("TapeDaemon resuming...");
		do {
			buffer = ringBuffer.getBuffer();
			last2bytes =
				(short) (((buffer[buffer.length - 2] & 0xFF) << 8)
					+ (buffer[buffer.length - 1] & 0xFF));
			endOfRun = eventInput.isEndRun(last2bytes);

			//		if ((counter+numRead)>RECORD_SIZE){
			//		    fos.close();
			//		    counter=0;

			//		}

			if (endOfRun) {
				fos.write(buffer);
				counter = 0;
				fos.close();
				//XXX		    netDaemon.setEventWriter(false);
				System.out.println("TapeDaemon at endOfRun, file closed.");
			} else {
				fos.write(buffer);
				//		    counter=counter++;
			}
			yield();
		} while (true);
	}

	/* private methods, methods needed for tape device */
	/**
	 * Execute unix command
	 * rewind tape uses unix command
	 */

	public static String rewindTape() {
		String cmd;
		Process exec;
		int returnValue = 0;
		String mesg = "";

		//command for unix    
		cmd = REWIND_COMMAND1 + noRewindName + REWIND_COMMAND2;
		//XXXSystem.out.println(cmd);

		try {
			exec = Runtime.getRuntime().exec(cmd);
			exec.waitFor();
			returnValue = exec.exitValue();
			//XXXSystem.out.println("Exit Value: "+returnValue);
		} catch (Exception e) {
			System.err.println("Exception: " + e);
		}
		if (returnValue == 0) {
			mesg = "OK";
		} else if (returnValue == 1) {
			mesg = "Unable to open drive.";
		} else if (returnValue == 2) {
			mesg = "Failure.";
		}
		return mesg;
	}
	/**
	 * The method startNextEventRecord is called only by writeEvent to start 
	 * a new block when there is not enough space in the current record
	 * to store an event.  XXXcontroller.getEventSize()
	 */
	private void startNextEventRecord() throws IOException, EventException {

		//pad out current record
		while (recordByteCounter < RECORD_SIZE) {

			eventOutput.writeEndRun();
			recordByteCounter = recordByteCounter + 2;
			tapeByteCounter = tapeByteCounter + 2;
		}

		bos.flush();
		fos.close();
		fos = new FileOutputStream(noRewindDevice);
		bos = new BufferedOutputStream(fos, RECORD_SIZE);
		dos = new DataOutputStream(bos);
		recordByteCounter = 0;
		blockCounter++;
		System.out.println("Starting block " + blockCounter + " on tape.");
		System.out.println(
			"Total kBytes So Far:  " + (float) tapeByteCounter / 1024.0);
	}

	/**
	 * end the event current record 
	 *
	 */
	public void finishRun() throws IOException {

		while (recordByteCounter < RECORD_SIZE) {
			//FIXME	    dos.writeShort(EVENT_END_MARKER);
			recordByteCounter = recordByteCounter + 2;
			tapeByteCounter = tapeByteCounter + 2;
		}
		bos.flush();
		fos.close();
		recordByteCounter = 0;

	}
	/**
	 *
	 */
	private boolean enoughSpace() {
		return (
			(recordByteCounter + 4 * (eventOutput.getEventSize()) + 2)
				<= (RECORD_SIZE - 1));
	}
	/**
	 *
	 */
	private int bytesToNextRecord() {
		int temp = 256;
		do {
			temp = temp + RECORD_SIZE;
		} while (temp <= recordByteCounter);
		return temp - (1 + recordByteCounter);
	}

}
