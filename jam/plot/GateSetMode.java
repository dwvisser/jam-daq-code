package jam.plot;

/**
 * Enumeration type to represent the different modes of gate setting.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jan 26, 2004
 */
enum GateSetMode {
	
	/**
	 * settting a new gate
	 */
	GATE_NEW,

	/**
	 * cancel the setting of a gate
	 */
	GATE_CANCEL,

	/**
	 * continue setting gate
	 */
	GATE_CONTINUE,
	
	/**
	 * add a point to setting gate
	 */
	GATE_ADD,
	
	/**
	 * add a point to setting gate
	 */
	GATE_REMOVE,

	/**
	 * save the gate that is being set
	 */
	GATE_SAVE
}
	
