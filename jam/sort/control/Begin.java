package jam.sort.control;

import jam.global.JamException;
import jam.sort.SortException;
import jam.ui.Icons;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

class Begin extends AbstractAction {
	
	private static final Logger LOGGER = Logger.getLogger(Begin.class.getPackage().getName());

	private transient final RunControl runControl;

	private transient final JTextComponent textRunTitle;

	Begin(RunControl runControl, JTextComponent text) {
		super();
		this.runControl = runControl;
		textRunTitle = text;
		putValue(Action.NAME, "Begin Run");
		putValue(Action.SHORT_DESCRIPTION, "Begins the next run.");
		putValue(Action.SMALL_ICON, Icons.getInstance().BEGIN);
		setEnabled(false);
	}

	public void actionPerformed(final ActionEvent event) {
		final String runTitle = textRunTitle.getText().trim();
		final boolean confirm = (JOptionPane.showConfirmDialog(runControl,
				"Is this title OK? :\n" + runTitle, "Run Title Confirmation",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
		if (confirm) {
			try {
				runControl.beginRun();
			} catch (SortException se) {
				LOGGER.log(Level.SEVERE, se.getMessage(), se);
			} catch (JamException je) {
				LOGGER.log(Level.SEVERE, je.getMessage(), je);
			}

		}
	}

}
