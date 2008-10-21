package jam.plot;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

/**
 * This Class does the scroll bars for one and two Dimensional Plots.
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

	private transient final JScrollBar scrollHorz, scrollVert, scrollCount;

	// keep track of last counts update
	private transient int lastScrollValC, lastScrollVisC;// NOPMD

	private transient int lastCountMax; // not used

	private transient boolean countChanging; // are we changing the count

	// scale

	/** plot limits that have the bounded range models */
	private transient Limits plotLimits;

	private transient final Object LOCK = new Object();

	/**
	 * Create the scroller that belongs to the given plot.
	 * 
	 * @param plot
	 *            the plot that uses this scroller
	 */
	Scroller(final AbstractPlot plot) {
		super();
		this.plot = plot;
		if (plot instanceof Plot1d) {
			isPlot2d = false;
		} else {
			isPlot2d = true;
		}
		this.setLayout(new BorderLayout());
		// add scroll bars to plot
		plot.addScrollBars(this);
		// plot in middle panel
		add(plot.getComponent(), BorderLayout.CENTER);
		// scroll bar to move along the x axis
		scrollHorz = new JScrollBar(Adjustable.HORIZONTAL, 0, 255, 0, 255);
		this.add(scrollHorz, BorderLayout.SOUTH);
		scrollHorz.addAdjustmentListener(this);
		// if 2d plot add y scrollers
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
		scrollCount.addMouseListener(new MouseAdapter() {
			/**
			 * Returns the count scrollbar to the middle.
			 */
			@Override
			public void mouseReleased(final MouseEvent event) {
				update();
			}
		});
		// starting not updating count scale
		countChanging = false;
	}

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
				final Adjustable adj = event.getAdjustable();
				final int scrollValue = event.getValue();
				final int scrollVisible = adj.getVisibleAmount();
				boolean updatePlot = false;
				// scale scroll bar
				if (source.equals(scrollCount)) {
					if ((scrollValue != lastScrollValC)
							|| (scrollVisible != lastScrollVisC)) {
						countChange(scrollValue);
						lastScrollValC = scrollValue;
						lastScrollVisC = scrollVisible;
						updatePlot = true;
					}
					// horizontal scroll bar
				} else if (source.equals(scrollHorz)) {
					plotLimits.update();
					updatePlot = true;
					// vertical scroll bar
				} else if (source.equals(scrollVert)) {
					plotLimits.update();
					updatePlot = true;
				}
				/* update the plot can't use refresh as it resets count scroller */
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
	private void countChange(final int scrollValue) {
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
			int newMax = lastCountMax
					* (int) Math.round(Math.pow(scaleChange, sign));

			// change by one at least
			if (newMax == lastCountMax) {
				newMax = lastCountMax + sign;
			}
			countChanging = true;
			plot.setMaximumCountsConstrained(newMax);
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
