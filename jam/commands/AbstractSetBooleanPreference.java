/*
 * Created on Jun 10, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import jam.global.CommandListenerException;

import java.util.prefs.Preferences;

import javax.swing.ImageIcon;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 10, 2004
 */
public abstract class AbstractSetBooleanPreference extends AbstractCommand {
	
	protected Preferences prefsNode;
	protected String key;
	protected boolean state;
	private final ImageIcon checkMark=new ImageIcon("jam/commands/checkmark.png");
	private final ImageIcon clear=new ImageIcon("jam/clear.png");
	
	/**
	 * Set differently in subclass constuctor if necessary.
	 */
	protected boolean defaultState=true;

	protected void initCommand(){
		synchronized(this){
			state=prefsNode.getBoolean(key,defaultState);
		}
		changeIcon();
	}
	
	public synchronized boolean getState(){
		return state;
	}

	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		synchronized(this){
			if (cmdParams !=null && cmdParams.length>0 && cmdParams[0] 
			instanceof Boolean){
				state = ((Boolean)cmdParams[0]).booleanValue();
			} else {
				state = !state;
			}
			prefsNode.putBoolean(key, state);
			changeIcon();
		}
	}
	
	private void changeIcon(){
		synchronized (this){
			putValue(SMALL_ICON, state ? checkMark : clear);
		}
	}

	/**
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		final Boolean [] pass=new Boolean[1];
		synchronized(this){
			if (cmdTokens.length>0){
			final String s=cmdTokens[0];
			if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("yes") ||
			s.equalsIgnoreCase("on") || s.equalsIgnoreCase("1")){
				pass[0]=Boolean.TRUE;				
			} else {
				pass[0]=Boolean.FALSE;
			}
		} else {
			pass[0]=Boolean.valueOf(!state);
		}
		execute(pass);
		}
	}
}
