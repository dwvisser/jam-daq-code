package jam.commands;

import jam.global.Broadcaster;
import jam.global.CommandListener;
import jam.global.CommandListenerException;
import jam.global.CommandNames;
import jam.global.MessageHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Action;

/**
 * Class to create commands and execute them
 *
 * @author Ken Swartz
 */
public class CommandManager implements CommandListener, CommandNames {

	private MessageHandler msghdlr=null;
	private static CommandManager _instance=null;
	private static final Map cmdMap = Collections.synchronizedMap(new HashMap());
	private static final Map instances=Collections.synchronizedMap(new HashMap());
	private Commandable currentCommand;
	
	
	/* initializer block for map */
	static {
		cmdMap.put(OPEN_HDF, OpenHDFCmd.class);
		cmdMap.put(SAVE_HDF, SaveHDFCmd.class);
		cmdMap.put(SAVE_AS_HDF, SaveAsHDFCmd.class);
		cmdMap.put(SAVE_GATES, SaveGatesCmd.class);
		cmdMap.put(ADD_HDF, AddHDFCmd.class);
		cmdMap.put(RELOAD_HDF, ReloadHDFCmd.class);
		cmdMap.put(SHOW_NEW_HIST, ShowDialogNewHistogramCmd.class);
		cmdMap.put(SHOW_HIST_ZERO, ShowDialogZeroHistogram.class); 
		cmdMap.put(SHOW_NEW_GATE, ShowDialogNewGateCmd.class);
		cmdMap.put(SHOW_SET_GATE, ShowDialogSetGate.class); 
		cmdMap.put(SHOW_ADD_GATE, ShowDialogAddGate.class); 
		cmdMap.put(EXIT, ShowDialogExitCmd.class);
		cmdMap.put(NEW, FileNewClearCmd.class);
		cmdMap.put(PARAMETERS, ShowDialogParametersCmd.class);
		cmdMap.put(DISPLAY_SCALERS, ShowDialogScalersCmd.class);
		cmdMap.put(SHOW_ZERO_SCALERS, ShowDialogZeroScalersCmd.class);
		cmdMap.put(SCALERS, ScalersCmd.class);
		cmdMap.put(EXPORT_TEXT, ExportTextFileCmd.class);
		cmdMap.put(EXPORT_DAMM, ExportDamm.class);
		cmdMap.put(EXPORT_SPE, ExportRadware.class);	
		cmdMap.put(PRINT, Print.class);
		cmdMap.put(PAGE_SETUP, PageSetupCmd.class);	 
		cmdMap.put(IMPORT_TEXT, ImportTextFile.class);
		cmdMap.put(IMPORT_DAMM, ImportDamm.class);
		cmdMap.put(IMPORT_SPE, ImportRadware.class);
		cmdMap.put(IMPORT_XSYS, ImportXSYS.class);
		cmdMap.put(IMPORT_BAN, ImportORNLban.class);
		cmdMap.put(DELETE_HISTOGRAM, DeleteHistogram.class);
		cmdMap.put(USER_GUIDE, ShowUserGuide.class);	
		cmdMap.put(OPEN_SELECTED, OpenSelectedHistogram.class);
		cmdMap.put(DISPLAY_MONITORS, ShowMonitorDisplay.class);
		cmdMap.put(DISPLAY_MON_CONFIG, ShowMonitorConfig.class);
		cmdMap.put(SHOW_BATCH_EXPORT, ShowBatchExport.class);
	}
	

	/**
	 * Constructor private as singleton
	 *
	 */
	private CommandManager() {
	}
	
	/**
	 * Singleton accessor
	 * @return
	 */
	public static CommandManager getInstance () {
		if (_instance==null) {
			_instance=new CommandManager();
		}		
		return _instance;
	}
	
	public void setMessageHandler(MessageHandler msghdlr) {
		this.msghdlr = msghdlr;
	}
	
	/**
	 * Perform command with object parameters
	 *
	 * @param strCmd	String key indicating the command
	 * @param cmdParams	Command parameters
	 */
	public boolean performCommand(String strCmd, Object[] cmdParams)
		throws CommandException {
		boolean validCommand=false;
		if (createCmd(strCmd)) {
			if (currentCommand.isEnabled()){
				currentCommand.performCommand(cmdParams);
			} else {
				msghdlr.errorOutln("Disabled command \""+strCmd+"\"");
			}				
			validCommand= true;
		}
		return validCommand;
	}

	/**
	 * Perform command with string parameters
	 *
	 * @param strCmd 		String key indicating the command
	 * @param strCmdParams  Command parameters as strings
	 */
	public boolean performParseCommand(String strCmd, String[] strCmdParams) 
		throws CommandListenerException {
		boolean validCommand=false;
		if (createCmd(strCmd)) {
			if (currentCommand.isEnabled()){
				currentCommand.performParseCommand(strCmdParams);
			} else {
				msghdlr.errorOutln("Disabled command \""+strCmd+"\"");
			}
			validCommand=true;
		} 
		return validCommand;
	}
	
	/**
	 * See if we have the instance created, create it if necessary,
	 * and return whether it was successfully created. 
	 * 
	 * @param strCmd name of the command
	 * @return <code>true</code> if successful, <code>false</code> if 
	 * the given command doesn't exist
	 */
	private boolean createCmd(String strCmd)  {
		final boolean exists=cmdMap.containsKey(strCmd);
		if (exists) {
			final Class cmdClass = (Class)cmdMap.get(strCmd);
			currentCommand = null;
			final boolean created=instances.containsKey(strCmd);
			if (created){
				currentCommand=(Commandable) instances.get(strCmd);
			} else {
				try {
					currentCommand = (Commandable) (cmdClass.newInstance());
					currentCommand.init(msghdlr);
					if (currentCommand instanceof Observer){
						Broadcaster.getSingletonInstance().addObserver(
						(Observer)currentCommand);
					}
				} catch (Exception e) {
					/* There was a problem resolving the command class or 
					 * with creating an instance. This should never happen
					 * if exists==true. */
					throw new RuntimeException(e);
				}
				instances.put(strCmd,currentCommand);
			}
		}
		return exists;
	}
	
	public Action getAction(String strCmd){
		return createCmd(strCmd) ? currentCommand : null;
	}
	
	public void setEnabled(String cmd, boolean enable){
		getAction(cmd).setEnabled(enable);
	}
	
	public String [] getSimilarCommnands(final String s){
		final SortedSet sim=new TreeSet();
		final Set keys=cmdMap.keySet();
		for (int i=s.length(); i>=1; i--){
			final String com=s.substring(0,i);
			for (Iterator it=keys.iterator(); it.hasNext();){
				final String key=(String)it.next();
				if (key.startsWith(com)){
					sim.add(key);
				}
			}
			if (!sim.isEmpty()){
				break;
			}
		}
		final String [] rval=new String[sim.size()];
		int i=0;
		for (Iterator it=sim.iterator(); it.hasNext(); i++){
			rval[i]=(String)it.next();
		}
		return rval;
	}
	
	public String [] getAllCommands(){
		final Object [] c=cmdMap.keySet().toArray();
		final String [] rval=new String[c.length];
		System.arraycopy(c,0,rval,0,c.length);
		return rval;
	}
}
