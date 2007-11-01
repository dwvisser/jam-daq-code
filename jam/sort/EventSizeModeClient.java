package jam.sort;

interface EventSizeModeClient {
	/**
	 * Sets how the event size is determined. Generally not called explicitly by
	 * subclasses.
	 * 
	 * @param mode
	 *            how the event size is determined
	 * @throws SortException
	 *             if called inappropriately
	 */
	void setEventSizeMode(final EventSizeMode mode) throws SortException;
}
