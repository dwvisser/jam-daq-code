package jam.plot;

/**
 * Represents the type of counts plot scale.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 * @version 2004-11-08
 */
public class Scale {
	/**
	 * Value representing linear counts scale.
	 */
	static public final Scale LINEAR = new Scale(0);

	/**
	 * Value representing log counts scale.
	 */
	static public final Scale LOG = new Scale(1);

	private int type;

	private Scale(int t) {
		type = t;
	}
	
	public boolean equals(Object o){
	    return o instanceof Scale ? ((Scale)o).type == type : false;
	}
}