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
class Scroller
	extends JPanel
	implements AdjustmentListener/*, MouseListener*/ {

	//which scrollbars to update
	final static int ALL = 0;
	final static int POSITION = 1;
	final static int COUNT = 2;

	// constants for horizontal and vertical scrollbar
	static final int SCROLL_MIN = 0;
	static final int SCROLL_MAX = 200;
	static final int SCROLL_SIZE = (SCROLL_MAX - SCROLL_MIN);

	//constants for count scroll bar
	static final int COUNT_SCROLL_MIN = 0;
	static final int COUNT_SCROLL_MAX = 210;
	static final int COUNT_SCROLL_VIEW = 10;
	static final int COUNT_SCROLL_MID = 100;
	//1/2 point taking to account view size
	static final double CHANGE_LIN = 0.01; //change  1% for 1
	static final double CHANGE_QUAD = 0.000; //change  x10 for 100 units

	private final AbstractPlot plot;
	private final boolean isPlot2d;

	private final JScrollBar scrollHorz, scrollVert, scrollCount;

	//keep track of last counts update
	private int lastScrollValC, lastScrollVisC;

	private int lastCountMax; //not used
	private boolean countChange; //are we changing the count scale

	/** plot limits that have the bounded range models */
	private Limits plotLimits;

	private MouseAdapter mouseAdapter=new MouseAdapter(){
		/**
		 * Returns the count scrollbar to the middle.
		 */
		public void mouseReleased(MouseEvent me) {
			update(COUNT);
		}
	};
	
	/**
	 * Create the scroller that belongs to the given plot.
	 * 
	 * @param plot the plot that uses this scroller
	 */
	Scroller(AbstractPlot plot) {
		this.plot = plot;
		if (plot instanceof Plot1d) {
			isPlot2d = false;
		} else {
			isPlot2d = true;
		}
		this.setLayout(new BorderLayout());
		//add scroll bars to plot
		plot.addScrollBars(this);
		//plot in middle panel
		this.add(plot, BorderLayout.CENTER);
		//scroll bar to move along the x axis
		scrollHorz = new JScrollBar(JScrollBar.HORIZONTAL, 0, 255, 0, 255);
		this.add(scrollHorz, BorderLayout.SOUTH);
		scrollHorz.addAdjustmentListener(this);
		//if 2d plot add y scrollers
		if (isPlot2d) {
			//scroll bar to move along the y axis
			scrollVert = new JScrollBar(JScrollBar.VERTICAL, 0, 255, 0, 255);
			this.add(scrollVert, BorderLayout.WEST);
			scrollVert.addAdjustmentListener(this);
		} else {
			scrollVert = null;
		}
		//scrollbar to change scale
		scrollCount =
			new JScrollBar(
				JScrollBar.VERTICAL,
				COUNT_SCROLL_MID,
				COUNT_SCROLL_VIEW,
				COUNT_SCROLL_MIN,
				COUNT_SCROLL_MAX);
		this.add(scrollCount, BorderLayout.EAST);
		scrollCount.addAdjustmentListener(this);
		scrollCount.addMouseListener(mouseAdapter);
		//starting not updating count scale
		countChange = false;
	}

	/**
	 * Set the Limits that the scroll bars are connected
	 * to the models in Limits.
	 */
	synchronized void setLimits(Limits limits) {
		plotLimits = limits;
		scrollHorz.setModel(plotLimits.getModelX());
		if (isPlot2d) {
			scrollVert.setModel(plotLimits.getModelY());
		}
	}

	/**
	 * Called when any scrollbar has been changed.
	 */
	public synchronized void adjustmentValueChanged(AdjustmentEvent ae) {
		JScrollBar source = (JScrollBar) ae.getSource();
		Adjustable adj = ae.getAdjustable();
		int scrollValue = ae.getValue();
		int scrollVisible = adj.getVisibleAmount();
		boolean updatePlot = false;
		//scale scroll bar
		if (source.equals(scrollCount)) {
			if ((scrollValue != lastScrollValC)
				|| (scrollVisible != lastScrollVisC)) {
				countChange(scrollValue);
				lastScrollValC = scrollValue;
				lastScrollVisC = scrollVisible;
				updatePlot = true;
			}
			//horizontal scroll bar
		} else if (source.equals(scrollHorz)) {
			plotLimits.update();
			updatePlot = true;
			//vertical scroll bar
		} else if (source.equals(scrollVert)) {
			plotLimits.update();
			updatePlot = true;
		}
		//update the plot can't use refresh as it resets count scroller
		if (updatePlot) {
			//FIXME KBS plot.copyCounts();
			plot.repaint();
		}
	}
	
	/**
	 * update automatically by limits.
	 *  Update the scroll bars for this plot
	 *  this routine calls updateHorz, updateVert and updateCounts
	 *  as is required for plot
	 */
	void update(int type) {
		if (type == COUNT || type == ALL) {
			updateCount();
		}
	}

	/**
	 * called to reset the counts scroll bar
	 * used because mouse release is not alwayed call
	 */
	private void updateCount() {
		lastCountMax = plot.getLimits().getMaximumCounts();
		//reset scrollbar to middle
		scrollCount.setValue(COUNT_SCROLL_MID);
		countChange = false;
	}

	/**
	 * Count scrollBar Change of scale using a quadratic function.
	 * This scrollBar is not quiet smooth and could be improved
	 * vertical scrollBar change scale use to be a quadratic function
	 * we will make it linear.
	 */
	private synchronized void countChange(int scrollValue) {
		int newMax;

		//get current maximum Counts if we have not done a countChange
		if (!countChange) {
			lastCountMax = plot.getLimits().getMaximumCounts();
		}
		final int oldMax = lastCountMax;
		//reduce plot count maximum make plot appear bigger
		if (scrollValue < COUNT_SCROLL_MID) {
			final double scrolldiff = (COUNT_SCROLL_MID - scrollValue);
			//implicit cast to double
			double scaleChange =
				(CHANGE_LIN * scrolldiff
					+ CHANGE_QUAD * (scrolldiff * scrolldiff));
			newMax = (int) (oldMax / (1.0F + scaleChange));
			scaleChange = 1.0 / (1.0 + scaleChange);
			//make a change of at least 1
			if (newMax == oldMax) {
				newMax = oldMax - 1;
			}
			//increase plot count maximum which make plot appear smaller
		} else {
			final double scrolldiff = scrollValue - COUNT_SCROLL_MID;
			double scaleChange =
				(CHANGE_LIN * scrolldiff
					+ CHANGE_QUAD * (scrolldiff * scrolldiff));
			newMax = (int) (oldMax * (1.0F + scaleChange));
			scaleChange = 1.0 + scaleChange;
			//make a change of at least 1
			if (newMax == oldMax) {
				newMax = oldMax + 1;
			}
		}
		//  plot.scaleMaximumCounts(scaleChange);
		countChange = true;
		plot.setMaximumCountsConstrained(newMax);
	}
	

}
