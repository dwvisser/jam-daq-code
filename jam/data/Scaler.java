package jam.data;
import java.util.*;
import jam.util.*;
import java.io.Serializable;

/**
 * Class representing an individual scaler in the experiment.
 *
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */

public class Scaler implements Serializable  {



    public static Hashtable scalerTable=new Hashtable(11);
    public static List scalerList=new Vector(11);
    /**
     * Limit on name length.
     */
    public final static int NAME_LENGTH = 16;


    private String name;	//name of scaler

    private int number;		//number in list
    private int value;		//value of scaler


    /**
     * Creates a new scaler with an assigned name and number.
     *
     * @param	name	name of the scaler
     * @param	number	number of scaler, most often the same as the register number in a CAMAC scaler unit
     */

    public Scaler(String name, int number) throws DataException{        
		final StringUtilities su=StringUtilities.instance();
        if(name.length()>NAME_LENGTH){//give error if name is too long
            throw new DataException("Scale name '"+name+"' too long maximum characters "+NAME_LENGTH);
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
        return scalerList;
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

    /**
     * Update all the scaler values.
     * The value indexs refer to the scaler number.
     * @param inValue the list of all the new values for the scalers
     */
    public static void update(int [] inValue){
        /* check we do not try to update mores scalers than there are */
        int numberScalers=Math.min( inValue.length, scalerList.size() );
        for (int i=0;i<numberScalers;i++){
            Scaler currentScaler=(Scaler)scalerList.get(i);
            currentScaler.setValue(inValue[currentScaler.getNumber()]);
        }
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
    public int getValue(){
        return value;
    }

    /**
     * Sets this scaler's value.
     *
     * @param valueIn the new value for this scaler
     */
    public  void setValue(int valueIn){
        value=valueIn;
    }
}

