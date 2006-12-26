package jam.plot;

/**
 * Plot package public constants that don't neatly fit into a single class.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 2004-12-13
 */
public final class Constants {
	
	private Constants(){
		super();
	}

    /**
     * Maximum value to display on the counts scale.
     */
    public static final int MAXIMUM_COUNTS = 1000000000;
    
	static final int BOTTOM = 1;//NOPMD

	static final int TOP = 2;//NOPMD

	static final int LEFT = 3;//NOPMD

	static final int RIGHT = 4;//NOPMD

	/* fake zero for Log scale 1/2 a count */
	static final double LOG_FAKE_ZERO = 0.5;//NOPMD

}
