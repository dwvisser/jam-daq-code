package test.sort.mockfrontend;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * GUI widget for a text panel with a label.
 * 
 * @author Dale Visser
 * 
 */

public class NamedTextPanel extends JPanel {

	private transient final JLabel label = new JLabel();

	private transient String text;

	/**
	 * @param sname
	 *            label
	 * @param init
	 *            initial text in field
	 */
	public NamedTextPanel(final String sname, final String init) {
		super();
		final Border border = BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED);
		this.setBorder(BorderFactory.createTitledBorder(border, sname));
		this.setText(init);
		this.add(this.label);
	}

	/**
	 * @param text
	 *            new value for text in field
	 */
	public final void setText(final String text) {
		synchronized (label) {
			this.text = text;
			this.updateLabel();
		}
	}

	private void updateLabel() {
		SwingUtilities.invokeLater(() -> NamedTextPanel.this.label.setText(text));
	}

}
