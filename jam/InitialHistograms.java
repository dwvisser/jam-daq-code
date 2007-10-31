package jam;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

/**
 * This class to make initial histogram to display that is nice to look at.
 * 
 * @version 0.5 April 98
 * @author Ken Swartz
 * @since JDK1.1
 */

public final class InitialHistograms {

	private transient final Histogram histInitial;

	private transient final Group groupInitial;

	/**
	 * Constructs an instance of this class.
	 */
	public InitialHistograms() {
		super();
		final Group group = Group.createGroup("Initial", Group.Type.FILE);
		/* histogram with Jam name 2d */
		final Histogram histJam2d = group.createHistogram(histNameJam2d(),
				"Histogram2D", "Jam Name 2D");
		/* histogram with Jam name */
		final Histogram histJam1d = group.createHistogram(histNameJam1d(),
				"Histogram1D", "Jam Name 1D");
		/* histogram with triangles */
		group.createHistogram(histTriangle(), "Triangle");
		new Gate("Letter A", histJam1d); // gate
		new Gate("Letter B", histJam1d); // gate
		new Gate("Letter C", histJam1d); // gate
		new Gate("Area A", histJam2d); // gate 2d
		new Gate("Area B", histJam2d); // gate 2d
		new Gate("Area C", histJam2d); // gate 2d
		Broadcaster.getSingletonInstance().broadcast(
				BroadcastEvent.Command.HISTOGRAM_ADD);
		histInitial = histJam2d;
		groupInitial = group;
	}

	Group getInitialGroup() {
		return groupInitial;
	}

	Histogram getInitialHist() {
		return histInitial;
	}

	/**
	 * @return counts for a 1d histogram that says JAM.
	 */
	private int[] histNameJam1d() {
		final int sizeX = 900;
		final int[] counts = new int[sizeX];
		final int height = 300;
		final int scale = height / 100;
		/* Make a J. */
		int value = 15 * scale;
		setRange(counts, 100, 50, value);
		value = 10 * scale;
		setRange(counts, 150, 75, value);
		value = 100 * scale;
		setRange(counts, 225, 75, value);
		/* Make an A. */
		int startCh = 400;
		for (int i = startCh; i < startCh + 50; i++) {
			counts[i] = 2 * (i - startCh) * scale;
		}
		startCh = 450;
		setRange(counts, startCh, 50, value);
		startCh = 500;
		for (int i = startCh; i < startCh + 50; i++) {
			counts[i] = (100 - 2 * (i - startCh)) * scale;
		}
		/* Make a M. */
		startCh = 650;
		setRange(counts, startCh, 50, value);
		startCh = 700;
		for (int i = startCh; i < startCh + 25; i++) {
			counts[i] = (100 - 2 * (i - startCh)) * scale;
		}
		startCh = 725;
		for (int i = startCh; i < startCh + 25; i++) {
			counts[i] = (50 + 2 * (i - startCh)) * scale;
		}
		startCh = 750;
		setRange(counts, startCh, 50, value);
		return counts;
	}

	private void setRange(int[] array, final int first, final int numCh,
			final int value) {
		final int last = first + numCh;
		for (int i = first; i < last; i++) {
			array[i] = value;
		}
	}

	private void markChannel(int[][] counts, final int chX, final int chY) {
		counts[chX][chY] = (int) Math.exp(1 + chY / 20.0);
	}

	private void rectangles(final int[][] counts) {
		final int[] xlow = { 20, 40, 50, 100, 170, 220 };
		final int[] xhigh = { 40, 70, 70, 140, 190, 240 };
		final int[] ylow = { 30, 30, 50, 80, 30, 30 };
		final int[] yhigh = { 60, 50, 150, 100, 150, 150 };
		/* J, some A, some M */
		for (int index = 0; index < xlow.length; index++) {
			for (int i = xlow[index]; i < xhigh[index]; i++) {
				for (int j = ylow[index]; j < yhigh[index]; j++) {
					markChannel(counts, i, j);
				}
			}
		}
	}

	private int[][] histNameJam2d() {
		final int sizeX = 260;
		final int sizeY = 180;
		final int[][] counts2d = new int[sizeX][sizeY];
		rectangles(counts2d);
		/* Rest of A */
		int startCh = 30;
		for (int j = startCh; j < 150; j++) {
			final int channel = (j - startCh) / 10;
			for (int i = 90 + channel; i < 110 + channel; i++) {
				markChannel(counts2d, i, j);
			}
			for (int i = 130 - channel; i < 150 - channel; i++) {
				markChannel(counts2d, i, j);
			}
		}
		/* Rest of M */
		startCh = 75;
		final int endCh = 150;
		for (int j = startCh; j < 150; j++) {
			final int channel = (endCh - j) / 5;
			for (int i = 180 + channel; i < 200 + channel; i++) {
				markChannel(counts2d, i, j);
			}
			for (int i = 210 - channel; i < 230 - channel; i++) {
				markChannel(counts2d, i, j);
			}
		}
		return counts2d;
	}

	/**
	 * @return counds for a 1d histogram of triangles
	 */
	private int[] histTriangle() {
		final int sizeX = 1000;
		final int[] counts = new int[sizeX];
		// make a small triangle
		int position = 0;
		int range = 200;
		for (int i = position; i <= position + range; i++) {
			if (i <= (position + range / 2)) {
				counts[i] = i - position;
			} else {
				counts[i] = position + range - i;
			}
		}
		position = 200;
		range = 600;
		for (int i = position; i <= position + range; i++) {
			if ((i <= position + range / 2)) {
				counts[i] = i - position;
			} else {
				counts[i] = position + range - i;
			}
		}
		position = 800;
		range = 200;
		for (int i = position; i < position + range; i++) {
			if (i <= (position + range / 2)) {
				counts[i] = i - position;
			} else {
				counts[i] = position + range - i;
			}
		}
		return counts;
	}
}