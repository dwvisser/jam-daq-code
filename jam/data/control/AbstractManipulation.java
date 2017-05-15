package jam.data.control;

import jam.data.AbstractHistogram;
import jam.data.Factory;
import jam.data.Group;
import jam.data.NameValueCollection;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for histogram manipulations has methods to create combo box of
 * histograms
 * 
 * @author Ken Swartz
 * 
 */
abstract class AbstractManipulation extends AbstractControl {
	@Override
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

	/** String to append to new histogram group in combo box */
	private final static String WILD_CARD = "/.";

	private static final NameValueCollection<Group> GROUPS = jam.data.Warehouse
			.getGroupCollection();

	/**
	 * Constructs a dialog box for manipulation of histograms.
	 * 
	 * @param frame
	 *            application frame
	 * 
	 * @param title
	 *            if dialog
	 * @param modal
	 *            whether to grab focus from parent window
	 * @param broadcaster
	 *            broadcast status updates
	 */
	public AbstractManipulation(final Frame frame, final String title,
			final boolean modal, final Broadcaster broadcaster) {
		super(frame, title, modal, broadcaster);
	}

	/*
	 * non-javadoc: add histograms of type type1 and type2 to chooser
	 */
	protected void loadAllHists(final JComboBox<Object> comboBox,
			final boolean addNew, final int histDim) {
		comboBox.removeAllItems();
		if (addNew) {
			// Add working group new
			comboBox.addItem(NEW_HIST + Group.WORKING_NAME + WILD_CARD);
			// Add new histograms
			for (Group group : GROUPS.getList()) {
				if (group.getType() != Group.Type.SORT
						&& !Group.WORKING_NAME.equals(group.getName())) {
					comboBox.addItem(NEW_HIST + group.getName() + WILD_CARD);
				}
			}
		}
		/* Add Existing hisograms */
		for (Group group : GROUPS.getList()) {
			for (AbstractHistogram hist : group.histograms.getList()) {
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
	protected boolean isNewHistogram(final String name) {
		return name.startsWith(NEW_HIST);
	}

	/*
	 * Get the group name from the combobox string @param name The name in the
	 * combobox @return the group name
	 */
	protected String parseGroupName(final String name) {
		final StringBuilder builder = new StringBuilder(name);
		final String groupName = builder.substring(NEW_HIST.length(),
				name.length() - WILD_CARD.length());
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
	protected final AbstractHistogram createNewDoubleHistogram(
			final String groupName, final String histName, final int size) {
		Group group;
		AbstractHistogram hist;
		group = GROUPS.get(groupName);
		if (group == null) {
			group = Factory.createGroup(groupName, Group.Type.FILE);
		}
		hist = Factory.createHistogram(group, new double[size], histName);
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		return hist;
	}

	/**
	 * Get the mean character width in pixels
	 * 
	 * @param fontMetrics
	 * @return mean width of a character in pixels
	 */
	protected int getMeanCharWidth(final FontMetrics fontMetrics) {
		final int[] widths = fontMetrics.getWidths();
		double sum = 0.0;
		int numWidths = 256; // ASCII set
		for (int i = 0; i < numWidths; i++) {
			sum += widths[i];
		}
		return (int) Math.round(sum / numWidths);
	}
}
