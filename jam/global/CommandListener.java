/*
 */
package jam.global; 
/**
 * This is an interface that says a class can be commanded
 * to do something with a text command.
 * <code>JamConsole</code> implements it to do tasks for the user.
 *
 * @author Ken Swartz
 */
 public interface CommandListener {		 
	/**
	 * Class must implement this command to receive commands
	 */
	public void commandPerform(String command, int[] parameters);	
}