package jam.data;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class representing an individual scaler in the experiment.
 *
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */

public class Scaler {

    private static final Map TABLE=Collections.synchronizedMap(new HashMap());
    private static final List LIST=Collections.synchronizedList(new ArrayList());
    
    /**
     * Limit on name length.
     */
    public final static int NAME_LENGTH = 16;

    private transient final String name;	//name of scaler
    private transient final int number;		//number in list
    private int value;		//value of scaler

    /**
     * Creates a new scaler with an assigned name and number.
     * 
     * @param group
     *            for this scaler to belong to
     * @param name
     *            name of the scaler, which must be <=16 characters
     * @param number
     *            number of scaler, most often the same as the register number
     *            in a CAMAC scaler unit
     * @throws IllegalArgumentException
     *             if name ><code>NAME_LENGTH</code> characters
     */
    public Scaler(Group group, String name, int number) {
        final StringUtilities stringUtil = StringUtilities.instance();
        if (name.length() > NAME_LENGTH) {//give error if name is too long
            throw new IllegalArgumentException("Scale name '" + name
                    + "' too long maximum characters " + NAME_LENGTH);
        }
        name = stringUtil.makeLength(name, NAME_LENGTH);
        //        if (group==null) {
        //FIXME KBS should not reference Status
        //group = STATUS.getCurrentGroup();
        //        }
        group.addScaler(this);
        /* make sure name is unique */
        int prime = 1;
        String addition;
        while (TABLE.containsKey(name)) {
            addition = "[" + prime + "]";
            name = stringUtil.makeLength(name, NAME_LENGTH - addition.length())
                    + addition;
            prime++;

        }

        this.name = name;
        this.number = number;
        /* Add to list of scalers */
        TABLE.put(name, this);
        LIST.add(this);
    }

    /**
     * Get the list of scalers
     *
     * @return the list of all scalers
     */
    public static List getScalerList(){
        return Collections.unmodifiableList(LIST);
    }

    /**
     * Sets the list of scalers. Used for remote setting of scaler values.
     * 
     * @param inScalerList
     *            the new list of all scalers
     */
    public static void setScalerList(List inScalerList) {
        clearList();
        final Iterator allScalers = inScalerList.iterator();
        while (allScalers.hasNext()) {//loop for all histograms
            final Scaler scaler = (Scaler) allScalers.next();
            TABLE.put(scaler.getName(), scaler);
            LIST.add(scaler);
        }
    }

    /**
     * Clears the list of all scalers.
     */
    public static void  clearList(){
        TABLE.clear();
        LIST.clear();
        /* run garbage collector to free memory */
        System.gc();
    }

    private static final Broadcaster BROADCASTER=Broadcaster.getSingletonInstance();

    /**
     * Update all the scaler values.
     * The value indexs refer to the scaler number.
     * @param inValue the list of all the new values for the scalers
     */
    public static void update(int [] inValue){
        /* check we do not try to update mores scalers than there are */
        final int numScalers=Math.min( inValue.length, LIST.size() );
        for (int i=0;i<numScalers;i++){
            final Scaler scaler=(Scaler)LIST.get(i);
            scaler.setValue(inValue[scaler.getNumber()]);
        }
        BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_UPDATE);
    }

    /**
     * Returns the scaler with the specified name.
     *
     * @param name the name of the desired scaler
     * @return the scaler with the specified name
     */
    public static Scaler getScaler(String name){
        return (Scaler)TABLE.get(name);
    }

    /**
     * Returns the name of this scaler.
     *
     * @return the name of this scaler
     */

    public String getName(){
        return name;
    }

    /**
     * Returns the number of this scaler.
     *
     * @return the number of this scaler
     */
    public int getNumber(){
        return number;
    }

    /**
     * Returns this scaler's value.
     *
     * @return the value of this scaler
     */
    public synchronized int getValue(){
        return value;
    }

    /**
     * Sets this scaler's value.
     *
     * @param valueIn the new value for this scaler
     */
    public synchronized void setValue(int valueIn){
        value=valueIn;
    }
}

