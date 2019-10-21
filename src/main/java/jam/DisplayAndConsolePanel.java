package jam;

import javax.swing.JSplitPane;

import com.google.inject.Inject;

import jam.global.Broadcaster;
import jam.ui.Console;

@SuppressWarnings("serial")
final class DisplayAndConsolePanel extends JSplitPane {

	@Inject
	protected DisplayAndConsolePanel(final Broadcaster broadcaster,
			final Console console, final Display display) {
		super(JSplitPane.VERTICAL_SPLIT, true, display, console);
		broadcaster.addPropertyChangeListener(display);
		this.setResizeWeight(0.9);
	}
}
