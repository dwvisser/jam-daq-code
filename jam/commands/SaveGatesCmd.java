package jam.commands;

import jam.io.hdf.HDFIO;
import jam.global.CommandListenerException;

/** 
 * Save gates and scalers 
 * 
 * @author Ken Swartz
 *
 */
public class SaveGatesCmd extends AbstractCommand implements Commandable {

	/**
	 * Constructor
	 *
	 */
	SaveGatesCmd(){
		putValue(NAME,"Save gates, scalers & parameters as HDF\u2026");
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams){
		final HDFIO hdfio = new HDFIO(status.getFrame(), msghdlr);
		hdfio.writeFile(false,true,true,true);

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);

	}

}
