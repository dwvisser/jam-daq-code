package jam.commands;

import jam.global.BroadcastEvent;
/**
 *  Command for scalers
 * 
 * @author Ken Swartz
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
final class ScalersCmd extends AbstractCommand implements Commandable {

	protected static int READ =1;
	protected static int ZERO =2;
	/* 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		int param =((Integer)cmdParams[0]).intValue();
		
		if (param==READ) {
			readScalers();
		}else if (param==ZERO) {
			zeroScalers();
		}		
		
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		
		Object [] params = new Object[1];
		
		if (cmdTokens[0].equals("read")) {			
			params[0]= new Integer(READ);
			
		}else if (cmdTokens[0].equals("zero")) {
			params[0]= new Integer(ZERO);
		}
		 	
		execute(params);	

	}
	/**
	 * Does the scaler zeroing
	 *
	 */
	private void zeroScalers() {
		broadcaster.broadcast(BroadcastEvent.SCALERS_CLEAR);
		readScalers();				
	}
	
	/**
	 * Does the scaler reading
	 *
	 */
	private void readScalers() {
		if (status.isOnLine()){
			broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
		} 		
	}

}
