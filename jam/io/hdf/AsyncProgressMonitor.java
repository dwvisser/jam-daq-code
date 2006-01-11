package jam.io.hdf;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * Progress bar that can be called from asyncronize (non ui) thread
 * 
 * @author Ken Swartz
 * 
 */
public class AsyncProgressMonitor {

	private static final Logger LOGGER = Logger.getLogger("jam.io.hdf");

	private static final int MIN = 0;

	private transient int count;

	private transient final Component frame;

	private transient ProgressMonitor monitor;

	AsyncProgressMonitor(Component frame) {
		super();
		this.frame = frame;
	}

	void close() {
		final Runnable runner = new Runnable() {
			public void run() {
				monitor.close();
			}
		};
		try {
			SwingUtilities.invokeAndWait(runner);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	void increment() {
		count++;
		final Runnable runner = new Runnable() {
			public void run() {
				monitor.setProgress(count);
			}
		};
		try {
			SwingUtilities.invokeAndWait(runner);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	void setNote(final String note) {
		final Runnable runner = new Runnable() {
			public void run() {
				monitor.setNote(note);
			}
		};
		try {
			SwingUtilities.invokeAndWait(runner);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	void setProgress(final int value) {
		final Runnable runner = new Runnable() {
			public void run() {
				monitor.setProgress(value);
			}
		};
		try {
			SwingUtilities.invokeAndWait(runner);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	void setup(final String message, final String note, final int max) {
		count = 0;
		final Runnable runner = new Runnable() {
			public void run() {
				monitor = new ProgressMonitor(frame, message, note, MIN, max);
				monitor.setMillisToPopup(100);
			}
		};
		try {
			SwingUtilities.invokeAndWait(runner);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
