package jam.sort.stream;

/**
 * Parameters for L003  ORNL formatted event stream
 *
 * @version	0.5 April 98
 * @author 	Dale Visser
 * @since       JDK1.1
 */
public interface L003Parameters {

    /**
     * Any word for a parameter number value bitwise 'and'ed with this gives the event 
     * parameter number.
     */
    short EVENT_PARAMETER_MASK  =(short)0x07FF;

    
    /**
     * All header blocks must begin with an ASCII version of this.
     */
    String HEADER_START="HHIRF   L003    LIST DATA       ";    
    
    
    /**
     * Length of a event data record in bytes.
     */
    int EVENT_RECORD_SIZE=8192;
    
    /**
     * Size of scaler records in bytes. (?)
     */
    int SCALER_RECORD_SIZE=32000;
    
    /**
     * Size of scaler buffers in bytes. (?) 
     */
    int SCALER_BUFFER_SIZE=256;
}
    