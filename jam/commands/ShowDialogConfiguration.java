package jam.commands;

import jam.commands.AbstractShowDialog;
import jam.sort.control.ConfigurationDisplay;

public class ShowDialogConfiguration extends AbstractShowDialog {



	public void initCommand(){
		putValue(NAME,"Configuration\u2026");			
		dialog=new ConfigurationDisplay();
		//enable(); 
	}
}
