/*
 * Created on June 4, 2004
 */
package jam.commands;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.inject.Inject;

import jam.SetupRemote;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
@SuppressWarnings("serial")
final class ShowSetupRemote extends AbstractShowDialog implements PropertyChangeListener {

	@Inject
	ShowSetupRemote(final SetupRemote setupRemote) {
		super("Observe Remote\u2026");
		dialog = setupRemote;
		enable();
	}

	private void enable() {
		setEnabled(false);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		enable();
	}

}
