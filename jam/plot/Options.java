package jam.plot;

final class Options {

	/**
	 * Dont use full scale ch for auto scale
	 */
	private transient boolean ignoreChFull;

	/**
	 * Dont use 0 ch for auto scale
	 */
	private transient boolean ignoreChZero;

	private boolean noFillMode;

	private transient boolean printing = false;

	Options() {
		super();
	}

	boolean isIgnoreChFull() {
		return ignoreChFull;
	}

	boolean isIgnoreChZero() {
		return ignoreChZero;
	}

	/**
	 * @return if we are in the "no fill mode"
	 */
	protected boolean isNoFillMode() {
		synchronized (this) {
			return noFillMode;
		}
	}

	boolean isPrinting() {
		return printing;
	}

	/*
	 * non-javadoc: ignore channel full scale on auto scale
	 */
	void setIgnoreChFull(final boolean state) {
		ignoreChFull = state;
	}

	/*
	 * non-javadoc: ignore channel zero on auto scale
	 */
	void setIgnoreChZero(final boolean state) {
		ignoreChZero = state;
	}

	void setNoFillMode(final boolean bool) {
		synchronized (this) {
			noFillMode = bool;
		}
	}

	void setPrinting(final boolean value) {
		printing = value;
	}

}
