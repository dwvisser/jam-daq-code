/*
 */
package jam.global;

/**
 * This is an temp class  that handles message that are
 * then printed, to System.out
 *
 * @author Ken Swartz
 */
public class MessageAdaptor implements MessageHandler {

	CommandListener msgCommand;

	/**
	 * Output part of a message.
	 */
	public void messageOut(String text, int part) {
		if (part == END) {
			System.out.println(text);
		} else {
			System.out.print(text);
		}

	}
	
	/**
	 * Output a message.
	 */
	public void messageOut(String text) {
		System.out.print(text);
	}
	
	/**
	 * Output a message with carriage return.
	 */
	public void messageOutln(String text) {
		System.out.println(text);
	}
	
	/**
	 * Output a warning message.
	 */
	public void warningOutln(String text) {
		System.out.println(text);
	}

	/**
	 * Output an error message.
	 */
	public void errorOutln(String text) {
		System.err.println(text);
	}
	
	/**
	 * Set the class to receive commands.
	 */
	public void setCommandListener(CommandListener msgCommand) {
		this.msgCommand = msgCommand;
	}

}