/*
 * Created on Feb 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package test.sort.mockfrontend;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 15, 2004
 */
public final class Counter extends NamedTextPanel {
	private transient int value = 0;
	private transient final Object syncObject = new Object();

	public Counter(final String sname, final int init) {
		super(sname, String.valueOf(init));
	}

	public void setValue(final int value) {
		synchronized (this.syncObject) {
			this.value = value;
			this.updateLabel();
		}
	}

	public void increment() {
		synchronized (this.syncObject) {
			this.value++;
			this.updateLabel();
		}
	}

	public void reset() {
		this.setValue(0);
	}

	private void updateLabel() {
		this.setText(String.valueOf(this.value));
	}
}
