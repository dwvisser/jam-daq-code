package jam;

import com.google.inject.Inject;
import jam.global.Broadcaster;
import jam.ui.Console;

import javax.swing.*;

final class DisplayAndConsolePanel extends JSplitPane {

	@Inject
	protected DisplayAndConsolePanel(final Broadcaster broadcaster,
			final Console console, final Display display) {
		super(JSplitPane.VERTICAL_SPLIT, true, display, console);
		broadcaster.addPropertyChangeListener(display);
		this.setResizeWeight(0.9);
	}
}
