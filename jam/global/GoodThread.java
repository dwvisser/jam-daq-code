package jam.global;
import javax.swing.JOptionPane;

/**
 * Extends the thread class to allow proper stopping and suspending
 * of threads, by allowing the object itself to decide when it's 
 * appropriate to stop after a stop request has been sent to it. 
 * Based on code given in "Java Design" by Peter Coad and Mark 
 * Mayfield.
 *
 * @author <a href="dale@visser.name">Dale Visser</a>
 */
public class GoodThread extends Thread {
	
	/**
	 * The possible thread states for a <code>GoodThread</code>.
	 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
	 */
	public static class State {
		private transient final int value;
		private static final String [] MODES = {"RUN","SUSPEND","STOP"};
		
		private State (int val){
			value=val;
		}
		
		public String toString(){
			return MODES[value];
		}
		
		/**
		 * Represents a running thread.
		 */
		public static final State RUN=new State(0);
		
		/**
		 * Represents a temporarily suspended thread.
		 */
		public static final State SUSPEND=new State(1);
		
		/**
		 * Represents a fully stopped thread.
		 */
		public static final State STOP=new State(2);
	}

    private transient State state = State.RUN;
    private transient final Object stateLock=new Object();

	/**
	 * Simply calls the superclass's constructor of the same signature.
	 * We start with the state equal to <code>RUN</code>.
	 */
    public GoodThread(){
        super();
    }

	/**
	 * Simply calls the superclass's constructor of the same signature.
	 * We start with the state equal to <code>RUN</code>.
	 * 
	 * @param runnable code to execute in thread
	 */
    public GoodThread(Runnable runnable){
        super(runnable);
    }

    /**
     * To stop, suspend, or restart a GoodThread, call this.
     *
     * @param newState <code>STOP, SUSPEND,</code> or <code>RUN</code>
     */
    public void setState(State newState) {
        synchronized (stateLock){
			state = newState;
			if (state != State.SUSPEND) {
				stateLock.notifyAll();//wake up thread
			} 
        }
    }

    /**
     * Should be called frequently by the thread's <code>run()</code>
     * method. Code in the run() method will decide what to do for a 
     * <code>STOP</code> state.  If the state is 
     * <code>SUSPEND</code>, <code>checkState()</code> takes care of
     * suspending the thread, and it takes a call to 
     * <code>setState(int)</code> with either <code>STOP</code> or 
     * <code>RUN</code> to get the thread out of
     * <code>checkState()</code>.
     *
     * @return <code>true</code> if OK to resume, <code>false</code> if state is <code>STOP</code>
     */
     public boolean checkState(){
    	synchronized (stateLock){
			while (state == State.SUSPEND) {
				try {
					stateLock.wait();
				} catch (InterruptedException ie) {
					JOptionPane.showMessageDialog(null,
					ie.getMessage(),
					getClass().getName()+" interrupted while suspended",
					JOptionPane.ERROR_MESSAGE);
				}
			}
			return (state == State.RUN);
    	}
    }

    public String toString() {
    	StringBuffer rval=new StringBuffer(super.toString());
    	rval.append(": state=");
    	synchronized (stateLock){
			return rval.append(state).toString();
    	}
    }
}
