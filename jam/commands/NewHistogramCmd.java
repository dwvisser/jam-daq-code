package jam.commands;

import jam.data.Histogram;
/**
 *  Command to create a histogram
 * 
 * @author Ken Swartz
 *
 */
public class NewHistogramCmd extends AbstractCommand {

	
	public NewHistogramCmd(){
		super();
		/* Default constructor needed */
	}
	
	/**
	 * Execute the command
	 */
	public void execute(Object [] cmdParams){		
		String name = (String)cmdParams[0];
		String title = (String)cmdParams[1];
		int type = ((Integer)cmdParams[2]).intValue();
		int sizeX = ((Integer)cmdParams[3]).intValue();		
		int sizeY = ((Integer)cmdParams[4]).intValue();						
		new Histogram(name, type, sizeX, sizeY, title);		
	}
	
	/*public void performCommand(int cmdParams) throws CommandException {
		execute(null);
	}*/
	
	/**
	 * Execute the command
	 */
	public void executeParse(String [] cmdParams){	
		String name = cmdParams[0];
		String title = cmdParams[1];
		int type = Integer.parseInt(cmdParams[2]);	
		int sizeX = Integer.parseInt(cmdParams[3]);	
		int sizeY = Integer.parseInt(cmdParams[4]);						
		new Histogram(name, type, sizeX, sizeY, title);			
	}
	
}
