package jam;
import jam.data.control.HistogramControl;
import jam.io.hdf.HDFIO;

import java.io.File;

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
 * </ol>
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Apr 5, 2004
 */
public final class Script {
	
	private final JamMain jam;
	private final File base;
	private HistogramControl histCtrl;
	private SetupSortOff sso;
	private SortControl sc;
	private HDFIO hdfio;
	private boolean isSetup=false;
	private boolean eventFilesGiven=false;
	
	/**
	 * Creates an instance, of which the user then invokes the methods 
	 * to script an offline sorting session. A non-trivial side-effect
	 * of invoking this constructor is that an instance of Jam is 
	 * started up in the background.
	 */
	public Script(){
		super();
		jam=new JamMain(this);
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
	 * @param sortRoutineName fully qualified with all package names
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
	final String sortRoutineName, final Class inStream, final Class outStream){
		sso.setupSort(classPath, sortRoutineName, inStream, outStream);
		System.out.println("Setup online sorting:");
		System.out.println("\t"+classPath+": "+sortRoutineName);
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
		final int numFiles=sc.addEventFile(fileOrDir);
		if (fileOrDir.isFile()){
			System.out.println("Added event file to sort: "+fileOrDir.getName());
		}
		if (fileOrDir.isDirectory()){
			System.out.println("Added event files in folder: "+
			fileOrDir.getName());
		}
		if (numFiles>0){
			eventFilesGiven=true;
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
		final int numFiles=sc.readList(list);
		if (numFiles>0){
			eventFilesGiven=true;
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
		sc.setEventOutput(eventsOut);
		System.out.println("Set file for pre-sorted events"+
		eventsOut.getAbsolutePath());
	}
	
	/**
	 * Zero all histograms in memory.
	 */
	public  void zeroHistograms(){
		histCtrl.zeroAll();
	}
		
	void setJamCommand(JamCommand jc){
		histCtrl=jc.getHistogramControl();
		sso=jc.getSetupSortOff();
		sc=jc.getSortControl();
		hdfio=jc.getHDFIO();
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
		if (!eventFilesGiven){
			throw new IllegalStateException(
			"You may not call beginSort() without first specifying event files to sort.");
		}
		try{
			sc.beginSort();
			System.out.println("Began sort. Waiting for finish...");
			final Object lock=new Object();
			synchronized (lock) {
				final long ms=2500;//wait time in milliseconds
				while (jam.getRunState() != RunState.ACQ_OFF){
					lock.wait(ms);
				}
			}
			System.out.println("Reached end of sort.");
		} catch (InterruptedException e) {
			System.err.println("Interrupted while waiting for sort to finish.");
		} catch (Exception e){
			System.err.println("Error while beginning sort: "+e.getMessage());
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
		hdfio.readFile(HDFIO.RELOAD, hdf);
		System.out.println("Loaded HDF file: "+hdf);
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
	}
	
	/**
	 * Hide Jam's graphical interface.
	 */
	public  void hideJam(){
		jam.setVisible(false);
	}
	
}
