package jam.commands;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
/**
 * Show the histogram dialog
 * 
 * @author Ken
 *
 */
public class ShowDialogExitCmd extends AbstractCommand{
	
	//Null constructor needed
	public ShowDialogExitCmd(){
		
	}		
	
	/**
	 * Execute the command
	 */
	public void execute(Object [] cmdParams){

		boolean confirm;
		JFrame frame =status.getFrame();				
		
		confirm=true;
		if (cmdParams!=null) {
			if ( !((Boolean)cmdParams[0]).booleanValue()) {
				confirm=false;	
			} 
		}			
		
		//Confirm exit
		if (confirm) {				
		
			final int rval =
				JOptionPane.showConfirmDialog(
					frame,
					"Are you sure you want to exit?",
					"Exit Jam Confirmation",
					JOptionPane.YES_NO_OPTION);
					
			if (rval == JOptionPane.YES_OPTION) {
				System.exit(0);
			} else {
				frame.setVisible(true);
			}
		}else {
			System.exit(0);		
		}
	}
	
	/**
	 * Execute the command
	 */
	public void executeStrParam(String [] cmdParams){
		
		if (cmdParams.length!=0) {
			
			Boolean confirm;
			if (cmdParams[0].equals("noconfirm"))
				confirm = Boolean.valueOf(true);
			else	
				confirm =Boolean.valueOf(false);
				 
			Object [] cmdParmObj=new Object[1];
			cmdParmObj[0]=confirm;
			execute(cmdParmObj);
			
		} else {
			execute(null);	
		}

	}
	
}
