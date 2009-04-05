package jam.util;

import javax.swing.SwingUtilities;

/**
 * This is the 3rd version of SwingWorker (also known as SwingWorker 3), an
 * abstract class that you subclass to perform GUI-related work in a dedicated
 * thread. For instructions on and examples of using this class, see:
 * 
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 * 
 * Note that the API changed slightly in the 3rd version: You must now invoke
 * start() on the SwingWorker after creating it.
 */
public abstract class AbstractSwingWorker {
	private Object value; // see getValue(), setValue()

	/**
	 * Class to maintain reference to current worker thread under separate
	 * synchronization control.
	 */
	private static class ThreadVar {
		private transient Thread thread;

		ThreadVar(final Thread newThread) {
			super();
			thread = newThread;
		}

		protected Thread get() {
			synchronized (this) {
				return thread;
			}
		}

		protected void clear() {
			synchronized (this) {
				thread = null;// NOPMD
			}
		}
	}

	private transient final ThreadVar threadVar;

	/**
	 * Get the value produced by the worker thread, or null if it hasn't been
	 * constructed yet.
	 * 
	 * @return the value returned by the worker thread
	 */
	protected Object getValue() {
		synchronized (this) {
			return value;
		}
	}

	/**
	 * Set the value produced by worker thread.
	 * 
	 * @param object
	 *            value to set it to
	 */
	private void setValue(final Object object) {
		synchronized (this) {
			value = object;
		}
	}

	/**
	 * Compute the value to be returned by the <code>get</code> method.
	 * 
	 * @return the computed value
	 */
	public abstract Object construct();

	/**
	 * Called on the event dispatching thread (not on the worker thread) after
	 * the <code>construct</code> method has returned.
	 */
	public void finished() {
		// do-nothing default implementations
	}

	/**
	 * A new method that interrupts the worker thread. Call this method to force
	 * the worker to stop what it's doing.
	 */
	public void interrupt() {
		final Thread thread = threadVar.get();
		if (thread != null) {
			thread.interrupt();
		}
		threadVar.clear();
	}

	/**
	 * Return the value created by the <code>construct</code> method. Returns
	 * null if either the constructing thread or the current thread was
	 * interrupted before a value was produced.
	 * 
	 * @return the value created by the <code>construct</code> method
	 */
	public Object get() {
		Object rval = null;
		while (true) {
			final Thread tempThread = threadVar.get();
			if (tempThread == null) {
				rval = getValue();
				break;
			}
			try {
				tempThread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate
				break;
			}
		}
		return rval;
	}

	/**
	 * Start a thread that will call the <code>construct</code> method and then
	 * exit.
	 */
	public AbstractSwingWorker() {
		super();
		final Runnable doFinished = new Runnable() {
			public void run() {
				finished();
			}
		};
		final Runnable doConstruct = new Runnable() {
			public void run() {
				try {
					setValue(construct());
				} finally {
					threadVar.clear();
				}

				SwingUtilities.invokeLater(doFinished);
			}
		};
		final Thread tempThread = new Thread(doConstruct);
		threadVar = new ThreadVar(tempThread);
	}

	/**
	 * Start the worker thread.
	 */
	public void start() {
		final Thread tempThread = threadVar.get();
		if (tempThread != null) {
			tempThread.start();
		}
	}
}
