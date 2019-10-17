package jam;

import com.google.inject.Inject;
import jam.ui.SelectionTree;

import javax.swing.*;

final class SelectionAndDisplayPanel extends JSplitPane {
	@Inject
	protected SelectionAndDisplayPanel(final SelectionTree selectTree,
			final DisplayAndConsolePanel dcPanel) {
		super(JSplitPane.HORIZONTAL_SPLIT, true, selectTree, dcPanel);
		this.setResizeWeight(0.1);
	}
}
