package jam.plot;

class Size {
	private transient final int sizeX, sizeY;

	Size(final int... size) {
		super();
		sizeX = size[0];
		sizeY = (size.length > 1) ? size[1] : 0;
	}

	protected int getSizeX() {
		return sizeX;
	}

	protected int getSizeY() {
		return sizeY;
	}
}