/*
 * Created on Feb 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package test.sort.mockfrontend;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Feb 15, 2004
 */
public final class Counter extends NamedTextPanel {
	private transient int value = 0;
	private transient final Object syncObject = new Object();

	/**
	 * @param sname
	 *            counter name
	 * @param init
	 *            initial value
	 */
	public Counter(final String sname, final int init) {
		super(sname, String.valueOf(init));
	}

	/**
	 * @param value
	 *            new value
	 */
	public void setValue(final int value) {
		synchronized (this.syncObject) {
			this.value = value;
			this.updateLabel();
		}
	}

	/**
	 * Increment the existing value.
	 */
	public void increment() {
		synchronized (this.syncObject) {
			this.value++;
			this.updateLabel();
		}
	}

	private void updateLabel() {
		this.setText(String.valueOf(this.value));
	}
}
