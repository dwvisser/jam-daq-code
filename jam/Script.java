package jam;
import java.io.File;
import jam.global.*;
import jam.data.control.*;

/**
 * Class which exposes an API for scripting offline sorting sessions.
	static public void main(){
		
	}
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Apr 5, 2004
 */
public abstract class Script extends GoodThread {
	
	private final JamMain jam;
	
	protected Script(){
		super();
		jam=new JamMain(this);
		try{
			this.setState(GoodThread.RUN);
			System.out.println("End of script.");
		} catch (GlobalException e){
			System.err.println("Error starting script thread: "+e.getMessage());
		}
	}
	
	public final void run(){
		if (checkState()){
			runScript();
		}
	}

	/**
	 * Person writing script should put all statements in this 
	 * method.
	 */
	protected abstract void runScript();

	protected final void setupOffline(File classPath, String sortRoutineName,
	Class inStream, Class outStream){
		sso.setupSort(classPath, sortRoutineName, inStream, outStream);
	}
	
	protected final void addEventFile(File fileOrDir){
		sc.addEventFile(fileOrDir);
		if (fileOrDir.isFile()){
			System.out.println("Added event file to sort: "+fileOrDir.getName());
		}
		if (fileOrDir.isDirectory()){
			System.out.println("Added event files in folder: "+
			fileOrDir.getName());
		}
	}
	
	protected final void loadFileList(File list){
		try {
			sc.readList(list);
		} catch (JamException e){
			System.err.println("Error while loading event file list: "+
			e.getMessage());
		}
	}
	
	protected final void setEventOutput(File eventsOut){
		setEventOutput(eventsOut);
		System.out.println("Set file for pre-sorted events"+
		eventsOut.getAbsolutePath());
	}
	
	protected final void zeroHistograms(){
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
	
	final void setJamCommand(JamCommand jc){
		histCtrl=jc.getHistogramControl();
		sso=jc.getSetupSortOff();
		sc=jc.getSortControl();
	}
	
	protected final void beginSort(){
	}
	
	protected final void saveHDF(File hdf){
	}
	
	protected final void showJam(){
		jam.setVisible(true);
	}
	
	protected final void hideJam(){
		jam.setVisible(false);
	}
	
}
