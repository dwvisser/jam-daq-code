package jam.sort;

/**
 * Represents a single channel of data that the acquisition
 * electronics will read out. Used by VME_Map to store its info.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public class VME_Channel{

    private final int slot, baseAddress, channel, threshold;
    private final Type type;
    
    /**
     * Encapsulates whether a parameter is an event paramter
     * or a scaler parameter.
     * 
     * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
     */
    public static class Type{
        private static final int EVENT_DATA = 349;
        private static final int SCALER_DATA = 987;
        private final int type;
        private Type(int param){
            type=param;
        }
        
        public boolean equals(Object o){
            return o instanceof Type ? ((Type)o).type==type : false;
        }
        
        /**
         * Indicates an event parameter.
         */
        public static final Type EVENT=new Type(EVENT_DATA);
        
        /**
         * Indicates a scaler parameter.
         */
        public static final Type SCALER=new Type(SCALER_DATA);
    }

    /**
     * Creates an event parameter.  V775/V785 TDC's/ADC's can have
     * base addresses from 0x20000000-0xe0ff0000.  
     *
     * @param map the map this channel belongs to
     * @param slot the slot the module occupies in the VME crate
     * @param parameterNumber integer from 0 to 2047 indicating the event stream parameter number to be assigned by the VME
     * @param baseAddress 24-bit base address of ADC or TDC module ()
     * @param channel integer from 0 to 31 indicating channel in ADC or TDC
     * @param threshold integer from 0 to 4095 indicating lower threshold for recording the value
     * @throws SortException if passed invalid values
     */
    VME_Channel(VME_Map map, int slot, int baseAddress, int channel, int threshold) throws SortException {
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
            map.appendMessage("Requested threshold: "+threshold+", truncated to: "+threshNum+
                ", actual threshold: "+threshNum*16+"\n");
            this.threshold=threshNum;
        } else {
            throw new SortException(getClass().getName()+".VMEChannel(): Invalid"
            + " threshold = "+threshold);
        }
        type=Type.EVENT;
    }

    /**
     * Gets the index in the event stream that this parameter is associated 
     * with.
     * 
     * @return parameter number for this channel
     */
    public short getParameterNumber() {
        return (short)(channel+(slot-2)*32);
    }

    /**
     * Gets the base address of the module associated with this channel.
     * 
     * @return the module base address
     */
    public int getBaseAddress() {
        return baseAddress;
    }

    /**
     * Gets the channel within the VME module for this parameter.
     * @return the channel within the module
     */
    public int getChannel() {
        return channel;
    }

    /**
     * Returns the type of parameter this is.
     * @return event or scaler
     * @see Type#EVENT
     * @see Type#SCALER
     */
    public Type getType(){
        return type;
    }

    /**
     * Get the threshold channel defined for the event parameter.
     * The threshold channel is a lower value threshold that the
     * ADC or TDC requires for storing a parameter.
     * 
     * @return the lower value threshold
     */
    public int getThreshold(){
        return threshold;
    }
    
    /**
     * Gets the slot the VME module resides in.
     * @return the physical slot number
     */
    public int getSlot() {
        return slot;
    }
}

