package jam;
import jam.data.control.HistogramControl;
import jam.global.GlobalException;
import jam.global.GoodThread;
import jam.io.hdf.HDFIO;

import java.io.File;

/**
 * Class which exposes an API for scripting offline sorting sessions.
	static public void main(){
		
	}
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Apr 5, 2004
 */
public final class Script extends GoodThread {
	
	private JamMain jam;
	private final File base;
	
	public Script(){
		super();
		jam=new JamMain(this);
		base=new File(System.getProperty("user.dir"));
	}
	
	public final File defineFile(String fname){
		return new File(base,fname);
	}

	public  void setupOffline(final File classPath, 
	final String sortRoutineName, final Class inStream, final Class outStream){
		sso.setupSort(classPath, sortRoutineName, inStream, outStream);
		System.out.println("Setup online sorting:");
		System.out.println("\t"+classPath+": "+sortRoutineName);
		System.out.println("\tin: "+inStream);
		System.out.println("\tout: "+outStream);
	}
	
	public  void addEventFile(File fileOrDir){
		sc.addEventFile(fileOrDir);
		if (fileOrDir.isFile()){
			System.out.println("Added event file to sort: "+fileOrDir.getName());
		}
		if (fileOrDir.isDirectory()){
			System.out.println("Added event files in folder: "+
			fileOrDir.getName());
		}
	}
	
	public  void loadFileList(File list){
		try {
			sc.readList(list);
		} catch (JamException e){
			System.err.println("Error while loading event file list: "+
			e.getMessage());
		}
	}
	
	public  void setEventOutput(File eventsOut){
		sc.setEventOutput(eventsOut);
		System.out.println("Set file for pre-sorted events"+
		eventsOut.getAbsolutePath());
	}
	
	public  void zeroHistograms(){
		try {
			histCtrl.zeroAll();
			System.out.println("Zeroed histograms.");
		} catch (GlobalException e){
			System.err.println("Error while zeroing histograms: "+
			e.getMessage());
		}
	}
	
	private HistogramControl histCtrl;
	private SetupSortOff sso;
	private SortControl sc;
	private HDFIO hdfio;
	
	 void setJamCommand(JamCommand jc){
		histCtrl=jc.getHistogramControl();
		sso=jc.getSetupSortOff();
		sc=jc.getSortControl();
		hdfio=jc.getHDFIO();
	}
	
	public void beginSort(){
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
	
	public void loadHDF(File hdf){
		hdfio.readFile(HDFIO.RELOAD, hdf);
		System.out.println("Loaded HDF file: "+hdf);
	}
	
	public void saveHDF(File hdf){
		hdfio.writeFile(hdf);
		System.out.println("Saved HDF file: "+hdf);
	}
	
	public  void showJam(){
		jam.setVisible(true);
	}
	
	public  void hideJam(){
		jam.setVisible(false);
	}
	
}
