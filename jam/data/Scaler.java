package jam.data;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.util.StringUtilities;

import java.io.Serializable;
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

public class Scaler implements Serializable  {

    private static final Map scalerTable=Collections.synchronizedMap(new HashMap());
    private static final List scalerList=Collections.synchronizedList(new ArrayList());
    
    /**
     * Limit on name length.
     */
    public final static int NAME_LENGTH = 16;

    private final String name;	//name of scaler
    private final int number;		//number in list
    private int value;		//value of scaler

    /**
     * Creates a new scaler with an assigned name and number.
     *
     * @param	name	name of the scaler, which must be <=16 characters
     * @param	number	number of scaler, most often the same as the register number in a CAMAC scaler unit
     * @throws UnsupportedArgumentException if name > <code>NAME_LENGTH</code> characters
     */
    public Scaler(String name, int number) {        
		final StringUtilities su=StringUtilities.instance();
        if(name.length()>NAME_LENGTH){//give error if name is too long
            throw new IllegalArgumentException("Scale name '"+name+"' too long maximum characters "+NAME_LENGTH);
        }
        name=su.makeLength(name, NAME_LENGTH);
        /* make sure name is unique */
        int prime=1;
        String addition;
        while(scalerTable.containsKey(name)){
            addition="["+prime+"]";
            name=su.makeLength(name,NAME_LENGTH - addition.length())+addition;
            prime++;

        }
        this.name=name;
        this.number=number;
        /* Add to list of scalers */
        scalerTable.put(name, this);
        scalerList.add(this);
    }

    /**
     * Get the list of scalers
     *
     * @return the list of all scalers
     */
    public static List getScalerList(){
        return Collections.unmodifiableList(scalerList);
    }

    /**
     * Sets the list of scalers.
     * Used for remote setting of scaler values.
     *
     * @param inScalerList the new list of all scalers
     */
    public static void setScalerList(List inScalerList){
        clearList();
        Iterator allScalers=inScalerList.iterator();
        while( allScalers.hasNext()) {//loop for all histograms
            Scaler scaler=(Scaler)allScalers.next();
            scalerTable.put(scaler.getName(), scaler);
            scalerList.add(scaler);
        }

    }

    /**
     * Clears the list of all scalers.
     */
    public static void  clearList(){
        scalerTable.clear();
        scalerList.clear();
        /* run garbage collector to free memory */
        System.gc();
    }

    private static final Broadcaster broadcaster=Broadcaster.getSingletonInstance();

    /**
     * Update all the scaler values.
     * The value indexs refer to the scaler number.
     * @param inValue the list of all the new values for the scalers
     */
    public static void update(int [] inValue){
        /* check we do not try to update mores scalers than there are */
        final int numberScalers=Math.min( inValue.length, scalerList.size() );
        for (int i=0;i<numberScalers;i++){
            final Scaler currentScaler=(Scaler)scalerList.get(i);
            currentScaler.setValue(inValue[currentScaler.getNumber()]);
        }
        broadcaster.broadcast(BroadcastEvent.Command.SCALERS_UPDATE);
    }

    /**
     * Returns the scaler with the specified name.
     *
     * @param name the name of the desired scaler
     * @return the scaler with the specified name
     */
    public static Scaler getScaler(String name){
        return (Scaler)scalerTable.get(name);
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

