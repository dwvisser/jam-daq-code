package jam;

import jam.global.Broadcaster;
import jam.ui.Console;

import javax.swing.JSplitPane;

import com.google.inject.Inject;

final class DisplayAndConsolePanel extends JSplitPane {

	@Inject
	protected DisplayAndConsolePanel(final Broadcaster broadcaster,
			final Console console, final Display display) {
		super(JSplitPane.VERTICAL_SPLIT, true, display, console);
		broadcaster.addObserver(display);
		this.setResizeWeight(0.9);
	}
}
