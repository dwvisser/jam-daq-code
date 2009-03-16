/*
 * Created on June 4, 2004
 */
package jam.commands;

import jam.SetupRemote;

import java.util.Observable;
import java.util.Observer;

import com.google.inject.Inject;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
final class ShowSetupRemote extends AbstractShowDialog implements Observer {

	@Inject
	ShowSetupRemote(final SetupRemote setupRemote) {
		super("Observe Remote\u2026");
		dialog = setupRemote;
		enable();
	}

	private void enable() {
		setEnabled(false);
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}

}
