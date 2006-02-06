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
class Scroller extends JPanel implements AdjustmentListener {

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

	/**
	 * Create the scroller that belongs to the given plot.
	 * 
	 * @param plot
	 *            the plot that uses this scroller
	 */
	Scroller(AbstractPlot plot) {
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
			scrollVert = null;//NOPMD
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
	void setLimits(final Limits limits) {
		synchronized (this) {
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
		synchronized (this) {
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
	void update() {
		if (plot.hasHistogram()) {
			lastCountMax = plot.getLimits().getMaximumCounts();
		}
		// reset scrollbar to middle
		scrollCount.setValue(COUNT_SCROLL_MID);
		countChanging = false;
	}

	/*
	 * non-javadoc: Count scrollBar Change of scale using a quadratic function.
	 * This scrollBar is not quiet smooth and could be improved vertical
	 * scrollBar change scale use to be a quadratic function we will make it
	 * linear.
	 */
	private void countChange(final int scrollValue) {
		synchronized (this) {
			int newMax;

			// get current maximum Counts if we have not done a countChange
			if (!countChanging) {
				lastCountMax = plot.getLimits().getMaximumCounts();
			}
			final int oldMax = lastCountMax;
			// reduce plot count maximum make plot appear bigger
			// 1/2 point taking to account view size
			final double CHANGE_LIN = 0.01; // change 1% for 1
			final double CHANGE_QUAD = 0.000; // change x10 for 100 units
			if (scrollValue < COUNT_SCROLL_MID) {
				final double scrolldiff = (COUNT_SCROLL_MID - scrollValue);
				// implicit cast to double
				double scaleChange = (CHANGE_LIN * scrolldiff + CHANGE_QUAD
						* (scrolldiff * scrolldiff));
				newMax = (int) (oldMax / (1.0F + scaleChange));
				scaleChange = 1.0 / (1.0 + scaleChange);
				// make a change of at least 1
				if (newMax == oldMax) {
					newMax = oldMax - 1;
				}
				// increase plot count maximum which make plot appear smaller
			} else {
				final double scrolldiff = scrollValue - COUNT_SCROLL_MID;
				double scaleChange = (CHANGE_LIN * scrolldiff + CHANGE_QUAD
						* (scrolldiff * scrolldiff));
				newMax = (int) (oldMax * (1.0F + scaleChange));
				scaleChange = 1.0 + scaleChange;
				// make a change of at least 1
				if (newMax == oldMax) {
					newMax = oldMax + 1;
				}
			}
			// plot.scaleMaximumCounts(scaleChange);
			countChanging = true;
			plot.setMaximumCountsConstrained(newMax);
		}
	}

	void enableScrolling(final boolean enableIn) {
		scrollCount.setVisible(enableIn);
		scrollHorz.setVisible(enableIn);
		if (isPlot2d) {
			scrollVert.setVisible(enableIn);
		}
	}
}
