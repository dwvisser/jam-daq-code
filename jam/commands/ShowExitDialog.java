package jam.commands;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 * Show the exit dialog.
 * 
 * @author Ken Swartz
 */
final class ShowExitDialog extends AbstractCommand{
	
	private ShowExitDialog(){
		super();
		putValue(NAME,"Exit\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
		CTRL_MASK));
	}		
	
	/**
	 * Execute the command
	 */
	protected void execute(Object [] cmdParams){

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
	protected void executeParse(String [] cmdParams){
		
		if (cmdParams.length!=0) {
			
			Boolean confirm;
			if (cmdParams[0].equals("noconfirm"))
				confirm = Boolean.valueOf(false);
			else	
				confirm =Boolean.valueOf(true);
				 
			Object [] cmdParmObj=new Object[1];
			cmdParmObj[0]=confirm;
			execute(cmdParmObj);
			
		} else {
			execute(null);	
		}

	}
	
}
