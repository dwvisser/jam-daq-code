package jam.plot;

final class Options {

	/**
	 * Don't use full scale channel for auto scale
	 */
	private transient boolean ignoreChFull;

	/**
	 * Don't use 0 channel for auto scale
	 */
	private transient boolean ignoreChZero;

	private boolean noFillMode;

	private transient boolean printing = false;

	Options() {
		super();
	}

	protected boolean isIgnoreChFull() {
		return ignoreChFull;
	}

	protected boolean isIgnoreChZero() {
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

	protected boolean isPrinting() {
		return printing;
	}

	/*
	 * non-javadoc: ignore channel full scale on auto scale
	 */
	protected void setIgnoreChFull(final boolean state) {
		ignoreChFull = state;
	}

	/*
	 * non-javadoc: ignore channel zero on auto scale
	 */
	protected void setIgnoreChZero(final boolean state) {
		ignoreChZero = state;
	}

	protected void setNoFillMode(final boolean bool) {
		synchronized (this) {
			noFillMode = bool;
		}
	}

	protected void setPrinting(final boolean value) {
		printing = value;
	}
}
