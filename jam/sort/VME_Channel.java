package jam.sort;

/**
 * Represents a single channel of data that the acquisition
 * electronics will read out. Used by VME_Map to store its info.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public class VME_Channel{

    private int slot, baseAddress, channel, threshold, type, arrayIndex;
    //private Scaler scaler;
    public static final int EVENT_DATA = 349;
    public static final int SCALER_DATA = 987;

    /**
     * Creates an event parameter.  V775/V785 TDC's/ADC's can have
     * base addresses from 0x20000000-0xe0ff0000.  
     *
     * @param parameterNumber integer from 0 to 2047 indicating the event stream parameter number to be assigned by the VME
     * @param baseAddress 24-bit base address of ADC or TDC module ()
     * @param channel integer from 0 to 31 indicating channel in ADC or TDC
     * @param threshold integer from 0 to 4095 indicating lower threshold for recording the value
     */
    VME_Channel(int slot, int baseAddress, int channel, int threshold,
    int arrayIndex) throws SortException {
        if (channel >= 0 && channel < 32) {
            this.channel=channel;
        } else {
            throw new SortException(getClass().getName()+".VMEChannel(): Invalid"
            + " channel = "+channel);
        }
        if (slot >=2 && slot <= 20) {//valid slots for ADC's and TDC's
            this.slot = slot;
        } else {
            throw new SortException(getClass().getName()+".VMEChannel(): Invalid"
            + " slot = "+slot);
        }
        if (baseAddress < 0x20000000  || baseAddress > 0xe0ff0000) {//highest hex digit must = 2 or 3
            this.baseAddress=baseAddress;
        } else {
            throw new SortException(getClass().getName()+".VMEChannel(): Invalid"
            + " base address = 0x"+Integer.toHexString(baseAddress));
        }
        if (threshold >= 0 && threshold < 4096) {
            int threshNum=(int)Math.round(threshold /16.0);
            System.err.println("Requested threshold: "+threshold+", truncated to: "+threshNum+
                ", actual threshold: "+threshNum*16);
            this.threshold=threshNum;
        } else {
            throw new SortException(getClass().getName()+".VMEChannel(): Invalid"
            + " threshold = "+threshold);
        }
        this.arrayIndex=arrayIndex;
        type=EVENT_DATA;
    }

    /*VME_Channel(int baseAddress, int channel, Scaler scaler)
    throws SortException {
        if (parameterNumber >= 2048 && parameterNumber < 4096) {
            this.parameterNumber=parameterNumber;
        } else {
            throw new SortException(getClass().getName()+".VMEChannel(): Invalid"
            + " Parameter Number = "+parameterNumber);
        }
        this.baseAddress=baseAddress;
        if (channel >= 0 && channel < 16) {
            this.channel=channel;
        } else {
            throw new SortException(getClass().getName()+".VMEChannel(): Invalid"
            + " channel = "+channel);
        }
        this.scaler=scaler;
        type=SCALER_DATA;
    }*/

    public short getParameterNumber() {
        return (short)(channel+(slot-2)*32);
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public int getChannel() {
        return channel;
    }

    public int getType(){
        return type;
    }

    public int getThreshold(){
        return threshold;
    }
    
    public int getSlot() {
        return slot;
    }

    /*public Scaler getScaler() throws SortException {
        if (type==EVENT_DATA) throw new SortException("VME_Channel.getScaler performed on "
        + "wrong type.");
        return scaler;
    }*/
}

