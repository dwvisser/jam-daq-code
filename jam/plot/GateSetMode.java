package jam.plot;

/**
 * Enumeration type to represent the different modes of gate setting.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jan 26, 2004
 */
class GateSetMode {
	final int value;
	private final static int [] VALUE_SET={0,1,2,3,4,5};
	
	/**
	 * settting a new gate
	 */
	final static GateSetMode GATE_NEW = new GateSetMode(0);

	/**
	 * cancel the setting of a gate
	 */
	final static GateSetMode GATE_CANCEL = new GateSetMode(1);

	/**
	 * continue setting gate
	 */
	final static GateSetMode GATE_CONTINUE = new GateSetMode(2);
	
	/**
	 * add a point to setting gate
	 */
	final static GateSetMode GATE_ADD = new GateSetMode(3);
	
	/**
	 * add a point to setting gate
	 */
	final static GateSetMode GATE_REMOVE = new GateSetMode(4);

	/**
	 * save the gate that is being set
	 */
	final static GateSetMode GATE_SAVE = new GateSetMode(5);
	
	private GateSetMode(int i){
		value=GateSetMode.VALUE_SET[i];
	}
}