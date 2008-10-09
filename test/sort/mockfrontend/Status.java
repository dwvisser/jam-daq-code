/*
 * Created on Feb 15, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package test.sort.mockfrontend;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Feb 15, 2004
 */
public class Status extends NamedTextPanel {
	static class Value {

		private static final String[] values = { "Booted", "Initialized",
				"Started", "Stopped" };

		private transient final String stringValue;

		private Value(final int value) {
			this.stringValue = values[value];
		}

		public static final Value BOOTED = new Value(0);
		public static final Value INIT = new Value(1);
		public static final Value START = new Value(2);
		public static final Value STOP = new Value(3);

		@Override
		public String toString() {
			return stringValue;
		}
	}

	/**
	 * @param init
	 *            initial value
	 */
	public Status(final Value init) {
		super("Status", init.toString());
	}

	/**
	 * @param value
	 *            new value
	 */
	public final void setValue(final Value value) {
		this.setText(value.toString());
	}
}
