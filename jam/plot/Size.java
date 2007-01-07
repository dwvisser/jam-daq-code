package jam.plot;

class Size {
	private transient final int sizeX, sizeY;

	Size(int... size) {
		super();
		sizeX = size[0];
		sizeY = (size.length > 1) ? size[1] : 0;
	}

	int getSizeX() {
		return sizeX;
	}

	int getSizeY() {
		return sizeY;
	}
}