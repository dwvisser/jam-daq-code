package jam;
import jam.data.control.HistogramZero;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.Observable;
import java.util.Observer;

/**
 * Class which exposes an API for scripting offline sorting sessions.
 * Using this class, you can write and compile a .java file which, when
 * executed, will<p>
 * <ol>
 *  <li>Launch Jam in the background.</li>
 *  <li>Setup offline sorting.</li>
 *  <li>Load an HDF file (i.e., load gates and parameters).</li>
 *  <li>Zero histograms (often needed after the previous step).</li>
 *  <li>Define the list of event files to sort.</li>
 *  <li>For sort routines that invoke <code>writeEvent()</code>, 
 *      specify the event output file.</li>
 *  <li>(Optional) Show the Jam window to observe sort progress.</li>
 *  <li><em>Perform the sort.</em></li>
 *  <li>Save the results in an HDF file.</li>
 *  <li>(Optional) Add histograms in stored HDF files together.
 * </ol>
 * The last step may be desirable
 * in the case where you would like to execute portions of the 
 * sorting task on different machines at the same time, and combine
 * their results in a "merge script".
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Apr 5, 2004
 */
public final class Script implements Observer {
	
	private transient final JamMain jam;
	private transient  File base;
	private transient SetupSortOff sso;
	private transient SortControl sortControl;
	private transient HDFIO hdfio;
	private transient boolean isSetup=false;
	private transient boolean filesGiven=false;
	
	/**
	 * Creates an instance, of which the user then invokes the methods 
	 * to script an offline sorting session. A non-trivial side-effect
	 * of invoking this constructor is that an instance of Jam is 
	 * started up in the background.
	 */
	public Script(){
		super();
		Broadcaster.getSingletonInstance().addObserver(this);
		jam=new JamMain(false);
		initFields();
		base=new File(System.getProperty("user.dir"));
	}
	
	/**
	 * Utility method for defining a file reference relative to the 
	 * directory that java was launched from. Here's an example for Linux
	 * and MacOS X systems: If the JVM was 
	 * launched from <code>/home/user/sortscript</code>, and this
	 * method is called with the argument 
	 * <code>"../thesisRuns/run34.evn"</code>, then a File object 
	 * representing <code>/home/user/thesisRuns/run34.evn</code> is 
	 * returned, <em>whether or not that file really exists yet</em>.
	 * 
	 * @param fname relative path reference to the file of interest
	 * @return File object resolving <code>fname</code> against the 
	 * base path given by the system property <code>user.dir</code>
	 * at startup
	 */
	public File defineFile(String fname){
		return new File(base,fname);
	}

	/**
	 * Completes the task equivalent of specifying the settings in
	 * Jam's dialog for setting up offline sorting. That is, 
	 * calling this defines the classpath to sort 
	 * routines, the fully qualified name of the 
	 * <code>SortRoutine</code> to use for sorting, and references to
	 * the <code>EventInputStream</code> and 
	 * <code>EventOutputStream</code> to use.
	 * 
	 * @param classPath the path that sort routines get loaded from
	 * @param sortName fully qualified with all package names
	 * in the standard java "dot" notation, e.g., <code>"sort.Calorimeter"</code> 
	 * for the file <code>sort/Calorimeter.class</code> relative to 
	 * <code>classPath</code>
	 * @param inStream e.g., <code>jam.sort.stream.YaleInputStream.class</code>
	 * @param outStream e.g., <code>jam.sort.stream.YaleOutputStream.class</code>
	 * @see jam.sort.SortRoutine
	 * @see jam.sort.stream.EventInputStream
	 * @see jam.sort.stream.EventOutputStream
	 */
	public  void setupOffline(final File classPath, 
	final String sortName, final Class inStream, final Class outStream){
		sso.setupSort(classPath, sortName, inStream, outStream);
		System.out.println("Setup online sorting:");
		System.out.println("\t"+classPath+": "+sortName);
		System.out.println("\tin: "+inStream);
		System.out.println("\tout: "+outStream);
		isSetup=true;
	}
	
	/**
	 * Add an event file to the list of event files to sort. If the
	 * file reference passed represents a directory, then all files
	 * with names ending in <code>.evn</code> in the directory are
	 * added to the list. Files whose names don't meet this 
	 * requirement are ignored and not added to the list. This method
	 * does <em>not</em> recurse into the directory tree.
	 * 
	 * @param fileOrDir <code>.evn</code> file or folder containing
	 * such files
	 * @throws IllegalStateException if <code>setupOffline()</code>
	 * hasn't been called yet
	 */
	public void addEventFile(File fileOrDir){
		if (!isSetup){
			throw new IllegalStateException(
			"You may not call addEventFile() before calling setupOffline().");
		}
		final int numFiles=sortControl.addEventFile(fileOrDir);
		if (fileOrDir.isFile()){
			System.out.println("Added event file to sort: "+fileOrDir.getName());
		}
		if (fileOrDir.isDirectory()){
			System.out.println("Added event files in folder: "+
			fileOrDir.getName());
		}
		if (numFiles>0){
			filesGiven=true;
		} else {
			System.err.println(fileOrDir.getName()+" didn't contain any usable files.");
		}
	}
	
	/**
	 * Reads in the given text file, interpreting each line as a
	 * filename, which gets added to the list of event files to sort.
	 * Such files are typically created using the 
	 * <code>Save List</code> button in the 
	 * <code>Control|Sort...</code> dialog in Jam.
	 * 
	 * @param list text file listing event files
	 * @throws IllegalStateException if <code>setupOffline()</code>
	 * hasn't been called yet
	 */
	public void loadFileList(File list){
		if (!isSetup){
			throw new IllegalStateException(
			"You may not call loadFileList() before calling setupOffline().");
		}
		final int numFiles=sortControl.readList(list);
		if (numFiles>0){
			filesGiven=true;
		} else {
			System.err.println(list.getName()+" didn't contain any usable filenames.");
		}
	}
	
	/**
	 * Set the file to output events to when the user's sort routine
	 * invokes <code>writeEvent()</code>
	 * 
	 * @param eventsOut where to write "pre-sort" events
	 * @see jam.sort.SortRoutine#writeEvent(int [])
	 * @throws IllegalStateException if <code>setupOffline()</code>
	 * hasn't been called yet
	 */
	public  void setEventOutput(File eventsOut){
		if (!isSetup){
			throw new IllegalStateException(
			"You may not call setEventOutput() before calling setupOffline().");
		}
		sortControl.setEventOutput(eventsOut);
		System.out.println("Set file for pre-sorted events"+
		eventsOut.getAbsolutePath());
	}
	
	/**
	 * Zero all histograms in memory.
	 */
	public void zeroHistograms(){
		(new HistogramZero(null)).zeroAll();
	}
		
		
	private static final JamStatus STATUS=JamStatus.instance();
	
	private void initFields(){
		sso=SetupSortOff.getSingletonInstance();
		sortControl=SortControl.getInstance();
		hdfio=new HDFIO(STATUS.getFrame(),null);
	}
	
	/**
	 * Launch the offline sort.
	 * 
	 * @throws IllegalStateException if <code>setupOffline()</code>
	 * hasn't been called yet, or if no event files have been specified
	 */
	public void beginSort(){
		if (!isSetup){
			throw new IllegalStateException(
			"You may not call beginSort() before calling setupOffline().");
		}
		if (!filesGiven){
			throw new IllegalStateException(
			"You may not call beginSort() without first specifying event files to sort.");
		}
		try{
			sortControl.beginSort();
			System.out.println("Began sort. Waiting for finish...");
			final Object lock=new Object();
			synchronized (lock) {
				final long millisec=2500;
				while (state != RunState.ACQ_OFF){
					lock.wait(millisec);
				}
			}
			System.out.println("Reached end of sort.");
		} catch (InterruptedException e) {
			System.err.println("Interrupted while waiting for sort to finish.");
		} catch (Exception e){
			System.err.println("Error while beginning sort: "+e.getMessage());
		}
	}
	
	private transient RunState state=RunState.NO_ACQ;
	public void update(Observable event, Object param){
		final BroadcastEvent bEvent=(BroadcastEvent)param;
		final BroadcastEvent.Command command=bEvent.getCommand();
		if (command==BroadcastEvent.Command.RUN_STATE_CHANGED){
			synchronized(state){
				state=(RunState)param;
			}
		}
	}
	
	/**
	 * Load the given HDF file into memory. You must have already 
	 * invoked <code>setupSort()</code>.
	 * 
	 * @param hdf an HDF file
	 * @throws IllegalStateException if <code>setupOffline()</code>
	 * hasn't been called yet
	 */
	public void loadHDF(File hdf){
		if (!isSetup){
			throw new IllegalStateException(
			"You may not call loadHDF() before calling setupOffline().");
		}
		hdfio.readFile(FileOpenMode.RELOAD, hdf);
		System.out.println("Loaded HDF file: "+hdf);
	}
	
	
	/**
	 * Add the hisgogram counts in the given HDF file, or all
	 * HDF files in the given directory, into the 
	 * histograms in memory. You must have already 
	 * invoked <code>setupSort()</code>.
	 * 
	 * @param hdf an HDF file or folder containing HDF files
	 * @throws IllegalStateException if <code>setupOffline()</code>
	 * hasn't been called yet
	 */
	public void addHDF(File hdf){
		final FileFilter filter=new HDFileFilter(false);
		if (!isSetup){
			throw new IllegalStateException(
			"You may not call loadHDF() before calling setupOffline().");
		}
		if (hdf.isDirectory()){
			final File [] files=hdf.listFiles(filter);
			for (int i=0; i< files.length; i++){
				hdfio.readFile(FileOpenMode.ADD, files[i]);
				System.out.println("Added HDF file: "+files[i]);
			}
		} else if (filter.accept(hdf)){
			hdfio.readFile(FileOpenMode.ADD, hdf);
			System.out.println("Added HDF file: "+hdf);
		} else {
			System.out.println(hdf+" isn't an HDF file, not added.");
		}
	}

	/**
	 * Load the given HDF file into memory. 
	 * 
	 * @param hdf an HDF file
	 */
	public void saveHDF(File hdf){
		hdfio.writeFile(hdf);
		System.out.println("Saved HDF file: "+hdf);
	}
	
	/**
	 * Show Jam's graphical interface. Once shown, the user may interact with Jam
	 * like normal, including interrupting the scripted process.
	 *
	 */
	public  void showJam(){
		jam.setVisible(true);
		STATUS.setShowGUI(true);
	}
	
	/**
	 * Hide Jam's graphical interface.
	 */
	public  void hideJam(){
		jam.setVisible(false);
	}
	
}
