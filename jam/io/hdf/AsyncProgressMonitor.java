package jam.io.hdf;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import java.awt.Component;



/**
 * Progress bar that can be called from asyncronize (non ui) thread
 * 
 * @author Ken Swartz
 *
 */
public class AsyncProgressMonitor {

    private ProgressMonitor monitor;
    private final Component frame;
    private final int min=0;
    private int count;
            
    AsyncProgressMonitor(Component frame){
    	this.frame=frame;
    }
    
    void setup(final String message, final String note, final int max){
    	count =0;
        final Runnable runner = new Runnable() {
            public void run() {
            	monitor = new ProgressMonitor(frame, message, note, min, max);
            	monitor.setMillisToPopup(200);
            }
        };
        try {
            SwingUtilities.invokeAndWait(runner);
        } catch (Exception e) {
            e.printStackTrace();
        }		
	}
    
	void increment() {
		count ++;
        final Runnable runner = new Runnable() {
            public void run() {
                monitor.setProgress(count);
            }
        };
        try {
            SwingUtilities.invokeAndWait(runner);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }

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
            e.printStackTrace();
        }	
    }
}
