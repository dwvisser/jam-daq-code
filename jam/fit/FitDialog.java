package jam.fit;

import javax.swing.*;
import java.awt.*;

final class FitDialog {

	private transient final JDialog dialog;

	private transient PlotInteraction plotInteraction;

	public FitDialog(final Frame owner, final String title) {
		this.dialog = new JDialog(owner, title, false);
		this.dialog.setResizable(false);
		this.dialog.setLocation(20, 50);
		this.dialog.getContentPane().setLayout(new BorderLayout());
	}

	protected void setPlotInteraction(final PlotInteraction plotInteraction) {
		this.plotInteraction = plotInteraction;
	}

	public void show() {
		this.plotInteraction.setMouseActive(false);
		this.dialog.setVisible(true);
	}

	public JDialog getDialog() {
		return this.dialog;
	}

}
