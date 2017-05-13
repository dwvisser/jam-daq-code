package jam.data;

import jam.global.Nameable;

/**
 * Interface for all data objects
 * 
 * @author Ken Swartz
 */
public interface DataElement extends Nameable, Dimensional {

	/**
	 * Possible types of data elements.
	 * 
	 * @author Dale Visser
	 */
	enum Type {
		/**
		 * Represents a gate.
		 */
		GATE,
		/**
		 * Represents a histogram.
		 */
		HISTOGRAM,
		/**
		 * Represents a scaler.
		 */
		SCALER
	}

    /**
	 * 
	 * @return ???
	 */
	double getCount();

	/**
	 * 
	 * @return which kind of element this is
	 */
	Type getElementType();
}
