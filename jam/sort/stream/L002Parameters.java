package jam.sort.stream;

/**
 * Parameters for L002 ORNL formatted data. 
 *
 * @version	0.5, 0.9
 * @author 	<a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public interface L002Parameters {

    //stream markers
    
    /**
     * Value in the stream indicating the end of an event.
     */
    final short EVENT_END_MARKER=(short)0xFFFF;    
    
    /**
     * Value in the stream indicating the end of a buffer.
     */
    final short BUFFER_END_MARKER=(short)0xFFF0;
    
    /**
     * Value in the stream indicating the end of a run.
     */
    final short RUN_END_MARKER=(short)0xFF03;    
    
    /**
     * Any value bitwise 'and'ed with this that is non-zero (not counting special
     * values above) contains a parameter number. The markers
     * 0x8000 thru 0x87ff are event 
     * parameters, while 0x8800 thru 0x8fff are scaler parameters.
     */
    final short EVENT_PARAMETER_MARKER=(short)0x8000;    
    
    /**
     * Any value bitwise and'ed with this that is not zero (not counting special
     * values denoted above) is the special kind of event parameter containing
     * a scaler value inserted in the event stream.
     */
    final short SCALER_PARAMETER_MARKER=(short)0x8800;
    
    /**
     * Any word for a parameter number value bitwise 'and'ed with this gives the event 
     * parameter number.
     */
    final short EVENT_PARAMETER_MASK  =(short)0x0FFF;

    //header stuff
    
    /**
     * The length of a header block.
     */
    public final int HEADER_LENGTH=256;	
    
    /**
     * All header blocks must beging with an ASCII version of this.
     */
    public final String HEADER_START="HHIRF   L002    LIST DATA       ";    
    
    /**
     * Maximum number of ASCII characters in the header title.
     */
    public final int TITLE_MAX=80;    

    //event data word parameter limits		
    
    /**
     * Minimum allowed parameter number.
     */    	    	    
    final short PARAMETER_MIN=(short)1;
    
    /**
     * Maximum allowed parameter number.
     */    	    	    
    final short PARAMETER_MAX=(short)512;
    
    //tape stuff
    
    /**
     * Length of a event data record on tape.
     */
    static public final int DATA_RECORD_LENGTH=0x10000; //64 kbytes	 //must be an even number
    
    /**
     * Length of an image record on tape.  An image record contains any additional comments about the run
     * that the experimenter wishes to store on tape.
     */
    static public final int IMAGE_RECORD_LENGTH=1600;	//20 lines of 8-bit chars
}
    