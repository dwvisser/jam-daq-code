/*
 * Created on Dec 31, 2003
 */
package jam;
import java.awt.Color;

/**
 * represents the possible run states of Jam.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4
 */
public class RunState {
	
	private static final String [] NAMES={"NO_ACQ","ACQ_ON",
	"ACQ_OFF","RUN_OFF","RUN_ON","REMOTE"};
	private static final String STOPPED="   Stopped   ";
	private static final String [] LABELS={"   Welcome   ", 
		"   Started   ",STOPPED,STOPPED,"","   Remote   "};
	private static final Color [] COLORS={Color.LIGHT_GRAY,
		Color.ORANGE,Color.RED,Color.RED,Color.GREEN,Color.LIGHT_GRAY
	};
	private static final boolean [] ACQUIRE_ON={false,true,
		false,false,true,false};
	private static final boolean [] ACQUIRE_MODE={false,true,
		true,true,true,false
	};
	
	private final String name;
	private String label;
	private final Color color;
	private final boolean acquireOn,acquireMode;

	private RunState(int i){
		name=NAMES[i];
		label=LABELS[i];
		color=COLORS[i];
		acquireOn=ACQUIRE_ON[i];
		acquireMode=ACQUIRE_MODE[i];
	}
	
	/**
	 * Acquisition is not set up.
	 */
	public static final RunState NO_ACQ=new RunState(0);
	
	/**
	 * Online acquisition is set up.
	 */
	public static final RunState ACQ_ON=new RunState(1);
	
	/**
	 * Offline acquisition is set up.
	 */
	public static final RunState ACQ_OFF=new RunState(2);
	
	/**
	 * Offline acquisition is set up and running.
	 */
	public static final RunState RUN_OFF=new RunState(3);
	
	/**
	 * Getting our display data from a remote session.
	 */
	public static final RunState REMOTE=new RunState(5);
	
	/**
	 * Online acquisition is set up, running and storing events to 
	 * disk.
	 * 
	 * @param run run number
	 * @return a new state object
	 */
	public static final RunState RUN_ON(int run){
		RunState rval=new RunState(4);
		rval.setLabel("   Run "+run+"   ");
		return rval;
	}
	
	private void setLabel(String l){
		label=l;
	}
	
	public String getLabel(){
		return label;
	}
	
	public String toString(){
		return name;
	}
	
	public Color getColor(){
		return color;
	}
	
	public boolean isAcqOn(){
		return acquireOn;
	}
	
	public boolean isAcquireMode(){
		return acquireMode;
	}
}
