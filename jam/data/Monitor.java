package jam.data;
import java.util.*;
import java.io.Serializable;
import jam.global.Sorter;


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
public class Monitor implements Serializable {

    /**
     * Lookup table for all monitors.
     */
    public static Hashtable monitorTable=new Hashtable(11);

    /**
     * List of all monitors.
     */
    public static List monitorList=new Vector(11);


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

    private String name;  //name
    private int type;    //monitor type gate, scaler ...

    private double threshold;
    private double maximum;

    private boolean alarm;
    private java.applet.AudioClip audioClip;

    private Sorter sortClass;
    private Scaler scaler;
    private Gate gate;

    private double valueNew;  //the newest value set
    private double valueOld;  //the previous value set
    private double value;    //value for testing

    /**
     * Master constructor always called with name and type of monitor.
     *
     * @param name name of the monitor for display in dialog
     * @param type one of four defined types
     * @see #SCALER
     * @see #GATE
     * @see #SORT
     * @see #TEST
     */
    private Monitor(String name, int type ) {
        this.name=name;
        this.type=type;
        // Add to list of monitors
        monitorTable.put(name,this);
        monitorList.add(this);
    }

    /**
     * Constructor which gives type <code>SORT</code>.
     *
     * @param name name of the monitor for display in dialog
     * @param sort the sort routine which produces the monitor values
     * @see #SORT
     */
    public Monitor(String name, Sorter sort) {
        this(name, SORT);
        this.sortClass=sort;
    }

    /**
     * Constructor which gives type <code>SCALER</code>.
     *
     * @param name name of the monitor for display in dialog
     * @param scaler the scaler which is monitored
     * @see #SCALER
     */
    public Monitor(String name, Scaler scaler)  {
        this(name, SCALER);
        this.scaler=scaler;
    }

    /**
     * Constructor which gives type <code>GATE</code>.
     *
     * @param name name of the monitor for display in dialog
     * @param gate the gate whose area is monitored
     * @see #GATE
     */
    public Monitor(String name, Gate gate)  {
        this(name, GATE);
        this.gate=gate;
    }

    /**
     * Set the interval in seconds at which updates occur.
     *
     * @param intervalIn interval in seconds
     */
    public static void setInterval(int intervalIn){
        System.out.println("interval = "+intervalIn+" s.");
        interval=intervalIn;
    }

    /**
     * Gets the interval in seconds at which updates occur.
     *
     * @return interval in seconds
     */
    public static int getInterval(){
        return interval;
    }

    /**
     * Returns the list of monitors.
     *
     * @return the list of monitors
     */
    public static List getMonitorList() {
        return monitorList;
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
    public double getValue(){
        return value;
    }

    /**
     * Sets this monitor's latest value.
     *
     * @param valueIn the new value
     */
    public void setValue(int valueIn){
        valueNew=valueIn;
    }

    /**
     * Sets this monitor's value to zero.
     */
    public void reset() {
        value=0;
    }

    /**
     * Updates this monitor, calculating the latest monitor values.
     * Keeps the most recent value, too, for rate determination.
     */
    public void update(){
        //System.out.println("Name: "+name+", Type: "+type);
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
    public void setThreshold(double inThreshold){
        threshold=inThreshold;
    }

    /**
     * Returns the threshold value for this monitor.
     *
     * @return the threshold value
     */
    public double getThreshold(){
        return threshold;
    }

    /**
     * Sets the maximum value, which is the maximum value for a monitor to have without
     * <code>MonitorControl</code> issuing a warning beep.
     *
     * @param inMaximum the new maximum
     * @see jam.data.control.MonitorControl
     */
    public void setMaximum(double inMaximum){
        maximum=inMaximum;
    }

    /**
     * Returns the maximum value for this monitor.
     *
     * @return the maximum value for this monitor
     */
    public double getMaximum(){
        return maximum;
    }

    /**
     * Sets whether the alarm is activated. If the alarm is not activated, <code>MonitorControl</code>
     * simply turns the indicator bar red when the value is below threshold or above the maximum.  If
     * it is activated, an alarm sound is issued too.
     *
     * @param inAlarm <code>true</code> if an audible alarm is desired, <code>false</code> if not
     */
    public void setAlarm(boolean inAlarm){
        alarm=inAlarm;
    }

    /**
     * Returns whether alarm is activated or not.
     *
     * @return <code>true</code> if an audible alarm is desired, <code>false</code> if not
     */
    public boolean getAlarm(){
        return alarm;
    }

    /**
     * NOT YET IMPLEMENTED, Sets an <code>AudioClip</code> object to be played for alarms if the alarm is enabled.
     * Currently, the plan is to fully implement this when the JDK 1.2 <code>javax.media</code> packeage is
     * available.
     */
    public void setAudioClip(java.applet.AudioClip audioClip){
        this.audioClip=audioClip;
    }

    /**
     * NOT YET IMPLEMENTED, Gets the current <code>AudioClip</code> object to be played for alarms if the alarm is enabled.
     * Currently, the plan is to fully implement this when the JDK 1.2 <code>javax.media</code> packeage is
     * available.
     *
     * @return the sound clip for this monitor's alarm, <code>null</code> indicates that a default system beep is desired
     */
    public java.applet.AudioClip getAudioClip(){
        return audioClip;
    }

}
