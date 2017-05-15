package jam.plot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Implements the scroll bars for one and two Dimensional Plots.
 * 
 * @version 1.0
 * @author Ken Swartz
 * @author Dale Visser
 */
class Scroller extends JPanel implements AdjustmentListener, Limitable {

	// constants for count scroll bar

	private static final int COUNT_SCROLL_MID = 100;

	private transient final AbstractPlot plot;

	private transient final boolean isPlot2d;

	// horizontal limits, vertical limits (2d only), and counts scrollbar
	// instances
	private transient JScrollBar scrollHorz, scrollVert, scrollCount;

	// keep track of last counts update
	private transient int lastScrollValue, lastScrollVisible;// NOPMD

	private transient int lastCountMax; // not used

	private transient boolean countChanging; // are we changing the count

	// scale

	/** plot limits that have the bounded range models */
	private transient Limits plotLimits;

	private transient final Object LOCK = new Object();

	Scroller(final Plot1d plot) {
		super();
		this.plot = plot;
		isPlot2d = false;
		initialize();
	}

	Scroller(final Plot2d plot) {
		super();
		this.plot = plot;
		isPlot2d = true;
		initialize();
	}

	private final void initialize() {
		this.setLayout(new BorderLayout());
		// add scroll bars to plot
		plot.addScrollBars(this);
		// plot in middle panel
		add(plot.getComponent(), BorderLayout.CENTER);
		// scroll bar to move along the x axis
		scrollHorz = new JScrollBar(Adjustable.HORIZONTAL, 0, 255, 0, 255);
		this.add(scrollHorz, BorderLayout.SOUTH);
		scrollHorz.addAdjustmentListener(this);
		// if 2d plot add y scrollbar
		if (isPlot2d) {
			// scroll bar to move along the y axis
			scrollVert = new JScrollBar(Adjustable.VERTICAL, 0, 255, 0, 255);
			this.add(scrollVert, BorderLayout.WEST);
			scrollVert.addAdjustmentListener(this);
		} else {
			scrollVert = null;// NOPMD
		}
		// scrollbar to change scale
		final int COUNT_SCROLL_MIN = 0;
		final int COUNT_SCROLL_MAX = 210;
		final int COUNT_SCROLL_VIEW = 10;
		scrollCount = new JScrollBar(Adjustable.VERTICAL, COUNT_SCROLL_MID,
				COUNT_SCROLL_VIEW, COUNT_SCROLL_MIN, COUNT_SCROLL_MAX);
		this.add(scrollCount, BorderLayout.EAST);
		scrollCount.addAdjustmentListener(this);
		scrollCount.addMouseListener(mouseListener);
		// starting not updating count scale
		countChanging = false;
	}

	private transient final MouseAdapter mouseListener = new MouseAdapter() {
		/**
		 * Returns the count scrollbar to the middle.
		 */
		@Override
		public void mouseReleased(final MouseEvent event) {
			update();
		}
	};

	/**
	 * Set the Limits that the scroll bars are connected to the models in
	 * Limits.
	 * 
	 * @param limits
	 *            new limits
	 */
	public void setLimits(final Limits limits) {
		synchronized (LOCK) {
			plotLimits = limits;
			scrollHorz.setModel(plotLimits.getModelX());
			if (isPlot2d) {
				scrollVert.setModel(plotLimits.getModelY());
			}
		}
	}

	/**
	 * Called when any scrollbar has been changed.
	 * 
	 * @param event
	 *            adjustment event message
	 */
	public void adjustmentValueChanged(final AdjustmentEvent event) {
		synchronized (LOCK) {
			if (plot.hasHistogram()) {
				final JScrollBar source = (JScrollBar) event.getSource();
				final int scrollValue = event.getValue();
				final Adjustable adj = event.getAdjustable();
				final int scrollVisible = adj.getVisibleAmount();
				boolean updatePlot = false;
				if (source.equals(scrollCount)) {
					// scale scroll bar
					if ((scrollValue != lastScrollValue)
							|| (scrollVisible != lastScrollVisible)) {
						countChange(scrollValue, scrollVisible);
						updatePlot = true;
					}
				} else {
					// not scale scroll bar, so must be limits scroll bars
					plotLimits.update();
					updatePlot = true;
				}
				/*
				 * update the plot can't use refresh as it resets count
				 * scrollbar
				 */
				if (updatePlot) {
					plot.getComponent().repaint();
				}
			}
		}
	}

	/**
	 * called to reset the counts scroll bar used because mouse release is not
	 * alwayed call
	 */
	public void update() {
		synchronized (LOCK) {
			if (plot.hasHistogram()) {
				lastCountMax = plot.getLimits().getMaximumCounts();
			}
			// reset scrollbar to middle
			scrollCount.setValue(COUNT_SCROLL_MID);
			countChanging = false;
		}
	}

	/*
	 * non-javadoc: Count scrollBar Change of scale using a linear function.
	 * This scrollBar is not quiet smooth and could be improved vertical
	 * scrollBar change scale use to be a quadratic function we will make it
	 * linear.
	 */
	private void countChange(final int scrollValue, final int scrollVisible) {
		synchronized (LOCK) {
			// get current maximum Counts if we have not done a countChange
			if (!countChanging) {
				lastCountMax = plot.getLimits().getMaximumCounts();
			}

			// whether we are under or over the midpoint determines the sign
			// of the adjustment
			final int sign = scrollValue < COUNT_SCROLL_MID ? -1 : 1;
			final double scrolldiff = sign * (scrollValue - COUNT_SCROLL_MID);

			// change 1% for every 1
			final double scaleChange = 1.0 + 0.01 * scrolldiff;

			// effectively multiplies if we are increasing, divides if we
			// are decreasing
			int newMax = (int) Math.round(lastCountMax
					* Math.pow(scaleChange, sign));

			// change by one at least
			if (newMax == lastCountMax) {
				newMax = lastCountMax + sign;
			}

			countChanging = true;
			plot.setMaximumCountsConstrained(newMax);
			lastScrollValue = scrollValue;
			lastScrollVisible = scrollVisible;
		}
	}

	protected void enableScrolling(final boolean enableIn) {
		scrollCount.setVisible(enableIn);
		scrollHorz.setVisible(enableIn);
		if (isPlot2d) {
			scrollVert.setVisible(enableIn);
		}
	}
}
