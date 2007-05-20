package jam.data.control;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;

import java.awt.FontMetrics;

import javax.swing.JComboBox;

/**
 * Base class for histogram manipulations has methods to create combo box of
 * histograms
 * 
 * @author Ken Swartz
 * 
 */
abstract class AbstractManipulation extends AbstractControl {
	public abstract void doSetup();

	/**
	 * Width of choosers in characters.
	 */
	protected static final int CHAR_LENGTH = 35;

	/**
	 * Width of text fields in characters.
	 */
	protected static final int TEXT_LENGTH = 15;

	/** String to prepend to new histogram group in combobox */
	protected final static String NEW_HIST = "NEW: ";

	/** String to append to new histogram group in combobox */
	private final static String WILD_CARD = "/.";

	/**
	 * Constructs a dialog box for manipulation of histograms.
	 * 
	 * @param title
	 *            if dialog
	 * @param modal
	 *            whether to grab focus from parent window
	 */
	public AbstractManipulation(String title, boolean modal) {
		super(title, modal);
	}

	/*
	 * non-javadoc: add histograms of type type1 and type2 to chooser
	 */
	void loadAllHists(final JComboBox comboBox, final boolean addNew,
			final int histDim) {
		comboBox.removeAllItems();
		if (addNew) {
			// Add working group new
			comboBox.addItem(NEW_HIST + Group.WORKING_NAME + WILD_CARD);
			// Add new histograms
			for (Group group : Group.getGroupList()) {
				if (group.getType() != Group.Type.SORT
						&& !Group.WORKING_NAME.equals(group.getName())) {
					comboBox.addItem(NEW_HIST + group.getName() + WILD_CARD);
				}
			}
		}
		/* Add Existing hisograms */
		for (Group group : Group.getGroupList()) {
			for (Histogram hist : group.getHistogramList()) {
				if (hist.getType().getDimensionality() == histDim) {
					comboBox.addItem(hist.getFullName());
				}
			}
		}
		if (0 < comboBox.getItemCount()) {
			comboBox.setSelectedIndex(0);
		}
	}

	/*
	 * Is the histogram name one of a new histogram
	 */
	boolean isNewHistogram(final String name) {
		return name.startsWith(NEW_HIST);
	}

	/*
	 * Get the group name from the combobox string @param name The name in the
	 * combobox @return the group name
	 */
	String parseGroupName(final String name) {
		final StringBuilder builder = new StringBuilder(name);
		final String groupName = builder.substring(NEW_HIST.length(), name
				.length()
				- WILD_CARD.length());
		return groupName;
	}

	/**
	 * Create a new 1D double histogram given a group, name and size.
	 * 
	 * @param groupName
	 *            name of group for hist
	 * @param histName
	 *            name of histogram
	 * @param size
	 *            number of channels in histogram
	 * @return a 1D double histogram
	 */
	protected static final Histogram createNewDoubleHistogram(final String groupName,
			final String histName, final int size) {
		Group group;
		Histogram hist;
		group = Group.getGroup(groupName);
		if (group == null) {
			group = Group.createGroup(groupName, Group.Type.FILE);
		}
		hist = Histogram.createHistogram(group, new double[size], histName);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		return hist;
	}
	
	/**
	 * Get the mean character width in pixels
	 * 
	 * @param fontMetrics
	 * @return mean width of a character in pixels
	 */
	int getMeanCharWidth(final FontMetrics fontMetrics) {
		final double numWidths = 256.0;
		double sum = 0.0;
		final int[] widths = fontMetrics.getWidths();
		for (int i = 0; i < numWidths; i++) {
			sum += widths[i];
		}
		return (int) Math.round(sum / numWidths);
	}
}
