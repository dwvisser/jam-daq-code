/*
 */
package jam.global;
/**
 * Extends the thread class to allow proper stopping and suspending of threads,
 * by allowing the object itself to decide when it's appropriate to stop after a
 * stop request has been sent to it.  Based on code given in "Java Design" by
 * Peter Coad and Mark Mayfield.
 *
 * @author Dale Visser
 */
public class GoodThread extends Thread {

    public static final int RUN = 0;
    public static final int SUSPEND = 1;
    public static final int STOP = 2;

private static final String [] modes = {"RUN","SUSPEND","STOP","SPECIAL"};

    private int state = RUN;

    public GoodThread(){
        super();
    }

    public GoodThread(Runnable r){
        super(r);
    }

    /**
     * To stop, suspend, or restart a GoodThread, call this.
     *
     * @param s <code>STOP, SUSPEND,</code> or <code>RUN</code>
     * @exception GlobalException thrown for bad argument
     */
    public synchronized void setState(int s) throws GlobalException {
        if (s != RUN && s!=SUSPEND && s!=STOP){
            throw new GlobalException("GoodThread.setState("+s+"), invalid thread state");
        }
        this.state = s;
        System.err.println(getClass().getName()+".setState("+modes[s]+")");
        if (this.state != SUSPEND) this.notify();
    }

    /**
     * Should be called frequently by the thread's <code>run()</code> method.
     * Code in the run() method will decide what to do for a <code>STOP</code>
     * state.  If the state is <code>SUSPEND</code>, <code>checkState()</code> takes care
     * of suspending the thread, and it takes a call to <code>setState(int)</code>
     * with either <code>STOP</code> or <code>RUN</code> to get the thread out of
     * <code>checkState()</code>.
     *
     * @return <code>true</code> if OK to resume, <code>false</code> if state is <code>STOP</code>
     */
    protected synchronized boolean checkState(){
        while (this.state == SUSPEND) {
            try {
                wait();
            } catch (InterruptedException ie) {
            }
        }
        return (this.state == RUN);
    }

    public String toString(){
        if (state >=0 && state < modes.length) return modes[state];
        return "Invalid state: "+state;
    }
}
