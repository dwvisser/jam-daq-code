package jam.data;
import jam.global.Sorter;

import java.applet.AudioClip;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class is for monitoring the status of data acquisition.  Monitors can show the
 * status of
 * things like event rates, beam current, rate of growth in a histogram, etc.
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5,0.9
 * @since JDK 1.1
 */
public final class Monitor implements Serializable {

    /**
     * Lookup table for all monitors.
     */
    public static Map monitorTable=Collections.synchronizedMap(new HashMap());

    /**
     * List of all monitors.
     */
    public static List monitorList=Collections.synchronizedList(new ArrayList());

    /**
     * Monitor based on value of a scaler.
     */
    public static final int SCALER=0;

    /**
     * Monitor based on area in a gate.
     */
    public static final int GATE=1;

    /**
     * Sortfile defined monitor.
     */
    public static final int SORT=2;
    /** The update interval */
    private static int interval;

    private final String name;  //name
    private final int type;    //monitor type gate, scaler ...

    private double threshold;
    private double maximum;

    private boolean alarm;
    private java.applet.AudioClip audioClip;

    private final Sorter sortClass;
    private final Scaler scaler;
    private final Gate gate;

    private double valueNew;  //the newest value set
    private double valueOld;  //the previous value set
    private double value;    //value for testing

    /**
     * Constructor which gives type <code>SORT</code>.
     *
     * @param n name of the monitor for display in dialog
     * @param sort the sort routine which produces the monitor values
     * @see #SORT
     */
    public Monitor(String n, Sorter sort) {
        name=n;
        type=SORT;
        sortClass=sort;
        gate=null;
        scaler=null;
        addToCollections();
    }
    
    private final void addToCollections(){
		monitorTable.put(name,this);
		monitorList.add(this);    	
    }

    /**
     * Constructor which gives type <code>SCALER</code>.
     *
     * @param n name of the monitor for display in dialog
     * @param s the scaler which is monitored
     * @see #SCALER
     */
    public Monitor(String n, Scaler s)  {
        name=n;
        scaler=s;
        gate=null;
        sortClass=null;
        type=SCALER;
        addToCollections();
    }

    /**
     * Constructor which gives type <code>GATE</code>.
     *
     * @param n name of the monitor for display in dialog
     * @param g the gate whose area is monitored
     * @see #GATE
     */
    public Monitor(String n, Gate g)  {
        name=n;
        gate=g;
        scaler=null;
        sortClass=null;
        type=GATE;
        addToCollections();
    }

    /**
     * Set the interval in seconds at which updates occur.
     *
     * @param intervalIn interval in seconds
     */
    public synchronized static void setInterval(int intervalIn){
        interval=intervalIn;
    }

    /**
     * Gets the interval in seconds at which updates occur.
     *
     * @return interval in seconds
     */
    public synchronized static int getInterval(){
        return interval;
    }

    /**
     * Returns the list of monitors.
     *
     * @return the list of monitors
     */
    public static List getMonitorList() {
        return Collections.unmodifiableList(monitorList);
    }

    /**
     * Sets the list of monitor objects.
     *
     * @param inMonList must contain all <code>Monitor</code> objects
     */
    public static void setMonitorList(List inMonList){
        //clear current lists
        clearList();
        //loop for all histograms
        for(Iterator allMonitors=inMonList.iterator(); allMonitors.hasNext();) {
            Monitor monitor=(Monitor)allMonitors.next();
            String name=monitor.getName();
            monitorTable.put(name, monitor);
            monitorList.add(monitor);
        }
    }

    /**
     * Clears the list of monitors.
     */
    public static void  clearList(){
        monitorTable.clear();
        monitorList.clear();
        //run garbage collector
        System.gc();
    }

    /**
     * Returns this monitor's name. The name is used in
     * display and to retrieve the monitor.
     *
     * @return this montor's name
     */
    public String getName(){
        return name;
    }

    /**
     * Returns this monitor's current value.
     *
     * @return this monitor's current value
     */
    public synchronized double getValue(){
        return value;
    }

    /**
     * Sets this monitor's latest value.
     *
     * @param valueIn the new value
     */
    public synchronized void setValue(int valueIn){
        valueNew=valueIn;
    }

    /**
     * Sets this monitor's value to zero.
     */
    public synchronized void reset() {
        value=0;
    }

    /**
     * Updates this monitor, calculating the latest monitor values.
     * Keeps the most recent value, too, for rate determination.
     */
    public void update(){
        if(type==SCALER){
            valueNew=scaler.getValue();
            value=(valueNew-valueOld)/interval;
            valueOld=valueNew;
        } else if(type==GATE) {
            valueNew=((double)gate.getArea());
            value=(valueNew-valueOld)/interval;
            valueOld=valueNew;
        } else if(type==SORT) {
            value=sortClass.monitor(name);
        }
    }

    /**
     * Sets the threshold value, which is the minimum value for a monitor to have without
     * <code>MonitorControl</code> issuing a warning beep.
     *
     * @param inThreshold the new minimum
     * @see jam.data.control.MonitorControl
     */
    public synchronized void setThreshold(double inThreshold){
        threshold=inThreshold;
    }

    /**
     * Returns the threshold value for this monitor.
     *
     * @return the threshold value
     */
    public synchronized double getThreshold(){
        return threshold;
    }

    /**
     * Sets the maximum value, which is the maximum value for a monitor to have without
     * <code>MonitorControl</code> issuing a warning beep.
     *
     * @param inMaximum the new maximum
     * @see jam.data.control.MonitorControl
     */
    public synchronized void setMaximum(double inMaximum){
        maximum=inMaximum;
    }

    /**
     * Returns the maximum value for this monitor.
     *
     * @return the maximum value for this monitor
     */
    public synchronized double getMaximum(){
        return maximum;
    }

    /**
     * Sets whether the alarm is activated. If the alarm is not activated, <code>MonitorControl</code>
     * simply turns the indicator bar red when the value is below threshold or above the maximum.  If
     * it is activated, an alarm sound is issued too.
     *
     * @param inAlarm <code>true</code> if an audible alarm is desired, <code>false</code> if not
     */
    public synchronized void setAlarm(boolean inAlarm){
        alarm=inAlarm;
    }

    /**
     * Returns whether alarm is activated or not.
     *
     * @return <code>true</code> if an audible alarm is desired, <code>false</code> if not
     */
    public synchronized boolean getAlarm(){
        return alarm;
    }

    /**
     * NOT YET IMPLEMENTED, Sets an <code>AudioClip</code> object to be played for alarms if the alarm is enabled.
     * Currently, the plan is to fully implement this when the JDK 1.2 <code>javax.media</code> packeage is
     * available.
     */
    public synchronized void setAudioClip(AudioClip ac){
        audioClip=ac;
    }

    /**
     * NOT YET IMPLEMENTED, Gets the current <code>AudioClip</code> object to be played for alarms if the alarm is enabled.
     * Currently, the plan is to fully implement this when the JDK 1.2 <code>javax.media</code> packeage is
     * available.
     *
     * @return the sound clip for this monitor's alarm, <code>null</code> indicates that a default system beep is desired
     */
    public synchronized AudioClip getAudioClip(){
        return audioClip;
    }

	public synchronized boolean isAcceptable(){
		return value>maximum || value < threshold;
	}
}
