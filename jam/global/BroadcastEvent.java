/*
 */
package jam.global;
/**
 * A event that is broadcast.
 *
 * @author Ken Swartz
 */
public class BroadcastEvent {

    public static final int REFRESH=1;			//refresh the display
    public static final int HISTOGRAM_NEW=10;		//a new set of histograms has been defined
    public static final int HISTOGRAM_ADD=11;		//a histogram has been added
    public static final int HISTOGRAM_SELECT=12;	//a histogram had been selected

    public static final int SCALERS_READ=20;		//scalers have been read
    public static final int SCALERS_CLEAR=21;
    public static final int SCALERS_UPDATE=22;

    public static final int GATE_SELECT=30;		//a gate has been select to be displayed
    public static final int GATE_ADD=31;		//a gate has been added
    public static final int GATE_SET_ON=32;
    public static final int GATE_SET_OFF=33;
    public static final int GATE_SET_SAVE=34;
    public static final int GATE_SET_POINT=35;
    public static final int GATE_SET_ADD=36;
    public static final int GATE_SET_REMOVE=37;

    public static final int COUNTERS_UPDATE=40;
    public static final int COUNTERS_READ=41;
    public static final int COUNTERS_ZERO=42;

    private static final int[] POSSIBLE_CODES = {REFRESH, HISTOGRAM_NEW,
        HISTOGRAM_ADD,HISTOGRAM_SELECT,SCALERS_READ,SCALERS_CLEAR,
        SCALERS_UPDATE,GATE_SELECT,GATE_ADD,GATE_SET_ON,GATE_SET_OFF,
        GATE_SET_SAVE,GATE_SET_POINT,GATE_SET_ADD,GATE_SET_REMOVE,
        COUNTERS_UPDATE,COUNTERS_READ,COUNTERS_ZERO};
    
    private int command;
    private Object content;

    public BroadcastEvent(int command, Object content) throws GlobalException {
        if (!isValid(command)) throw new GlobalException(getClass().getName()+
        "(): invalid command parameter: "+command);
        this.command=command;
        this.content=content;
    }
    
    static private boolean isValid(int command) {
        for (int i=POSSIBLE_CODES.length-1; i >= 0; i--) {
            if (command == POSSIBLE_CODES[i]) return true;
        }
        return false;
    }
    
    /**
     * the command that is send
     * see broadcaster for types
     */
    public int getCommand(){
        return command;
    }
    
    public Object getContent(){
        return content;
    }
}