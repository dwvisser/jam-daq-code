package jam.sort.stream;

/**
 * Parameters for L003  ORNL formatted event stream
 *
 * @version	0.5 April 98
 * @author 	Dale Visser
 * @since       JDK1.1
 */
public final class L003Parameters {
	
	private L003Parameters(){
		super();
	}

    /**
     * Any word for a parameter number value bitwise 'and'ed with this gives the event 
     * parameter number.
     */
    static public final short EVENT_MASK  =(short)0x07FF;

    
    /**
     * All header blocks must begin with an ASCII version of this.
     */
    static public final String HEADER_START="HHIRF   L003    LIST DATA       ";    
    
    
    /**
     * Length of a event data record in bytes.
     */
    static public final int EVENT_RECORD_SIZE=8192;
    
    /**
     * Size of scaler records in bytes. (?)
     */
    static public final int SCALER_REC_SIZE=32000;
    
    /**
     * Size of scaler buffers in bytes. (?) 
     */
    static public final int SCALER_BUFF_SIZE=256;
}
    