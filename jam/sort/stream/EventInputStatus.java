package jam.sort.stream;

/**
 * Static instances represent the current status of
 * the running <code>EventInputStream</code>.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 * @see EventInputStream
 */
public class EventInputStatus{
    /**
     * Status if just read an event.
     */
    static public final EventInputStatus EVENT= new EventInputStatus(0);

    /**
     * Status if just reached the end of a buffer.
     */
    static public final EventInputStatus END_BUFFER=new EventInputStatus(1);

    /**
     * Status if just reached the end of a run.
     */
    static public final EventInputStatus END_RUN=new EventInputStatus(2);

    /**
     * Status if just reached the end of a file.
     */
    static public final EventInputStatus END_FILE=new EventInputStatus(3);

    /**
     * Status if just reached the end of the stream.
     */
    static public final EventInputStatus END_STREAM=new EventInputStatus(4);

    /**
     * Status if only a partial event was just read.
     */
    static public final EventInputStatus PARTIAL_EVENT=new EventInputStatus(5);

    /**
     * Status if unidentified word was just read.
     */
    static public final EventInputStatus UNKNOWN_WORD=new EventInputStatus(6);

    /**
     * Status if there is an unrecoverable error when reading the stream.
     */
    static public final EventInputStatus ERROR=new EventInputStatus(7);


    /**
     * Status if the most recent read parameter is actually a scaler value.
     */
    static public final EventInputStatus SCALER_VALUE=new EventInputStatus(8);

    /**
     * Status if the last bit of the stream was ignorable.
     */
    static public final EventInputStatus IGNORE=new EventInputStatus(9);

    private static final EventInputStatus[] VALUES = {EVENT,END_BUFFER,END_RUN,END_FILE,
    END_STREAM,PARTIAL_EVENT,UNKNOWN_WORD,ERROR,SCALER_VALUE,IGNORE};

    private final int _code;

    private EventInputStatus(int code){
        _code=code;
    }

    public int getCode() {
        return _code;
    }

    public static EventInputStatus code(int arg){
        return VALUES[arg];
    }

    public String toString(){
        if (getCode()==0) return "EVENT";
        if (getCode()==1) return "END_BUFFER";
        if (getCode()==2) return "END_RUN";
        if (getCode()==3) return "END_FILE";
        if (getCode()==4) return "END_STREAM";
        if (getCode()==5) return "PARTIAL_EVENT";
        if (getCode()==6) return "UNKNOWN_WORD";
        if (getCode()==7) return "ERROR";
        if (getCode()==8) return "SCALER_VALUE";
        if (getCode()==9) return "IGNORE";
        return "No recognized code.";
    }

}

