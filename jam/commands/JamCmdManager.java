package jam.commands;

import jam.global.CommandListener;
import jam.global.CommandListenerException;
import jam.global.Broadcaster;
import jam.global.CommandNames;
import jam.global.MessageHandler;
import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.global.JamStatus;

import java.util.Observable;
import java.util.Observer;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;


import javax.swing.Action;

/**
 * Class to create commands and execute them
 *
 * @author Ken Swartz
 */
public class JamCmdManager implements CommandListener, Observer {

	private MessageHandler msghdlr=null;
	private static JamCmdManager _instance=null;
	private static final Map cmdMap = Collections.synchronizedMap(new HashMap());
	private static final Map instances=Collections.synchronizedMap(new HashMap());
	private Commandable currentCommand;
	
	
	/* initializer block for map */
	static {
		cmdMap.put(CommandNames.OPEN_HDF, OpenHDFCmd.class);
		cmdMap.put(CommandNames.SAVE_HDF, SaveHDFCmd.class);
		cmdMap.put(CommandNames.SAVE_AS_HDF, SaveAsHDFCmd.class);
		cmdMap.put(CommandNames.ADD_HDF, AddHDFCmd.class);
		cmdMap.put(CommandNames.RELOAD_HDF, ReloadHDFCmd.class);
		cmdMap.put(CommandNames.SHOW_NEW_HIST, ShowDialogNewHistogramCmd.class);
		cmdMap.put(CommandNames.EXIT, ShowDialogExitCmd.class);
		cmdMap.put(CommandNames.NEW, FileNewClearCmd.class);
		cmdMap.put(CommandNames.PARAMETERS, ShowDialogParametersCmd.class);
		cmdMap.put(CommandNames.DISPLAY_SCALERS, ShowDialogScalersCmd.class);
		cmdMap.put(CommandNames.SHOW_ZERO_SCALERS, ShowDialogZeroScalersCmd.class);
		cmdMap.put(CommandNames.SCALERS, ScalersCmd.class);
		cmdMap.put(CommandNames.EXPORT_TEXT, ExportTextFileCmd.class);
		cmdMap.put(CommandNames.EXPORT_DAMM, ExportDamm.class);
		cmdMap.put(CommandNames.EXPORT_SPE, ExportRadware.class);		
	}
	

	/**
	 * Constructor private as singleton
	 *
	 */
	private JamCmdManager() {
	}
	/**
	 * Singleton accessor
	 * @return
	 */
	public static JamCmdManager getInstance () {
		if (_instance==null) {

			_instance=new JamCmdManager();
			Broadcaster broadcaster=Broadcaster.getSingletonInstance();
			broadcaster.addObserver(_instance);
		}		

		return _instance;
	}
	
	public void setMessageHandler(MessageHandler msghdlr) {
		this.msghdlr = msghdlr;
	}
	/**
	 * Implentation of Observer, listens for broadcast events
	 * 
	 */
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final int command=be.getCommand();
		if (command==BroadcastEvent.SORT_MODE_CHANGED){
			sortModeChanged();
		}	
	}
	/**
	 * Perform command with object parameters
	 *
	 * @param strCmd	String key indicating the command
	 * @param cmdParams	Command parameters
	 */
	public boolean performCommand(String strCmd, Object[] cmdParams)
		throws CommandException {
		boolean success=false;
		if (createCmd(strCmd)) {
			if (currentCommand.isEnabled()){
				currentCommand.performCommand(cmdParams);
				success= true;
			}
		}
		return success;
	}

	/**
	 * Perform command with string parameters
	 *
	 * @param strCmd 		String key indicating the command
	 * @param strCmdParams  Command parameters as strings
	 */
	public boolean performParseCommand(String strCmd, String[] strCmdParams) 
		throws CommandListenerException {
		boolean success=false;
		if (createCmd(strCmd)) {
			if (currentCommand.isEnabled()){
				currentCommand.performParseCommand(strCmdParams);
				success=true;
			}
		} 
		return success;
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
		//boolean success;
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
					currentCommand.setMode(currentCommandMode());
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
	
	/**
	 * The sort mode changed update the commands
	 *
	 */
	private void sortModeChanged(){
		
		int cmdMode;
		
		cmdMode=currentCommandMode();				
					
		Iterator it= (Iterator)((Collection)instances.values()).iterator(); 
		while(it.hasNext()) {
			Commandable cmd =(Commandable)it.next();
			cmd.setMode(cmdMode);
		}
		
		
	}
	private int currentCommandMode(){
		
		int cmdMode;		
		JamStatus status =JamStatus.instance();
		//Convert from SortMode to Commandable static		 
		//FIXME KBS not all modes taken care of yet
		//maybe we can do better by just using SortMode??
		final SortMode mode=status.getSortMode();
		if ((mode== SortMode.ONLINE_DISK)||(mode==SortMode.ONLINE_NO_DISK)) {		
			cmdMode=Commandable.ONLINE;
		}else if (mode == SortMode.OFFLINE) {
			cmdMode=Commandable.OFFLINE;
		}else if (mode == SortMode.FILE) {
			cmdMode=Commandable.FILE;
		}else {
		//FIXME KBS have to handle all SorMode's
			cmdMode=0;
			//throw new RuntimeException("Invalid sort mode");

		}
		return cmdMode;
	}
	public Action getAction(String strCmd){
		return createCmd(strCmd) ? currentCommand : null;
	}
	
	public void setEnabled(String cmd, boolean enable){
		getAction(cmd).setEnabled(enable);
	}
}
