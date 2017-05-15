package jam.global;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * Extends the thread class to allow proper stopping and suspending of threads,
 * by allowing the object itself to decide when it's appropriate to stop after a
 * stop request has been sent to it. Based on code given in "Java Design" by
 * Peter Coad and Mark Mayfield.
 * @author <a href="dwvisser@users.sourceforge.net">Dale Visser</a>
 */
public class GoodThread extends Thread {

    /**
     * The possible thread states for a <code>GoodThread</code>.
     * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
     */
    public enum State {
        /**
         * Represents a running thread.
         */
        RUN,

        /**
         * Represents a temporarily suspended thread.
         */
        SUSPEND,

        /**
         * Represents a fully stopped thread.
         */
        STOP
    }

    private transient State state = State.RUN;

    private final transient Object stateLock = new Object();

    /**
     * Simply calls the superclass's constructor of the same signature. We start
     * with the state equal to <code>RUN</code>.
     */
    public GoodThread() {
        super();
    }

    /**
     * Simply calls the superclass's constructor of the same signature. We start
     * with the state equal to <code>RUN</code>.
     * @param runnable
     *            code to execute in thread
     */
    public GoodThread(final Runnable runnable) {
        super(runnable);
    }

    /**
     * For logging messages.
     */
    protected static final Logger LOGGER = Logger.getLogger(GoodThread.class
            .getPackage().getName());

    /**
     * To stop, suspend, or restart a GoodThread, call this.
     * @param newState
     *            <code>STOP, SUSPEND,</code> or <code>RUN</code>
     */
    public void setState(final State newState) {
        synchronized (stateLock) {
            state = newState;
            if (state != State.SUSPEND) {
                stateLock.notifyAll();// wake up thread
            }
            LOGGER.info(toString());
        }
    }

    /**
     * Should be called frequently by the thread's <code>run()</code> method.
     * Code in the run() method will decide what to do for a <code>STOP</code>
     * state. If the state is <code>SUSPEND</code>, <code>checkState()</code>
     * takes care of suspending the thread, and it takes a call to
     * <code>setState(int)</code> with either <code>STOP</code> or
     * <code>RUN</code> to get the thread out of <code>checkState()</code>.
     * @return <code>true</code> if OK to resume, <code>false</code> if state is
     *         <code>STOP</code>
     */
    public boolean checkState() {
        synchronized (stateLock) {
            while (state == State.SUSPEND) {
                try {
                    stateLock.wait();
                } catch (InterruptedException ie) {
                    JOptionPane.showMessageDialog(null, ie.getMessage(),
                            getClass().getName()
                                    + " interrupted while suspended",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            return (state == State.RUN);
        }
    }

    @Override
    public String toString() {
        final StringBuilder rval = new StringBuilder(super.toString());
        rval.append(": state=");
        synchronized (stateLock) {
            return rval.append(state).toString();
        }
    }
}
