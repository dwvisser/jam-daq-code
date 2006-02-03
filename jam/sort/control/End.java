package jam.sort.control;

import jam.ui.Icons;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

class End extends AbstractAction {

	private transient final RunControl runControl;

	End(RunControl runControl) {
		super();
		this.runControl = runControl;
		putValue(Action.NAME, "End Run");
		putValue(Action.SHORT_DESCRIPTION, "Ends the current run.");
		putValue(Action.SMALL_ICON, Icons.getInstance().END);
		setEnabled(false);
	}

	public void actionPerformed(final ActionEvent event) {
		runControl.endRun();
	}

}
