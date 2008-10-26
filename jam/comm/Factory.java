package jam.comm;

/**
 * Creates instances of jam.comm classes and interfaces.
 * 
 * @author Dale Visser
 * 
 */
public final class Factory {
	private Factory() {
		// static factory class
	}

	/**
	 * @return a ScalerCommunication object
	 */
	public static ScalerCommunication createScalerCommunication() {
		return new ScalerVMECommunicator(VMECommunication
				.getSingletonInstance());
	}

	/**
	 * @return a FrontEndCommunication object
	 */
	public static FrontEndCommunication createFrontEndCommunication() {
		return new FrontEndVMECommunicator(VMECommunication.getSingletonInstance());
	}
}
