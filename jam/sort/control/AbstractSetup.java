/*
 * Created on Dec 21, 2004
 */
package jam.sort.control;

import jam.JamException;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.PropertyKeys;
import jam.global.RuntimeSubclassIdentifier;
import jam.sort.SortException;
import jam.sort.SortRoutine;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;
import jam.ui.SelectionTree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

/**
 * Takes care of some of the details for SetupSortOn and SetupSortOff.
 * 
 * @see jam.sort.control.SetupSortOn
 * @see jam.sort.control.SetupSortOff
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 * @version 2004-12-21
 */
abstract class AbstractSetup {

	/**
	 * Calls doApply if bok is the holder
	 * 
	 * @author dvk
	 * 
	 */
	protected final class ApplyAction extends AbstractAction {
		private final static String APPLY = "Apply";

		private final static String OK_TEXT = "OK";

		private final transient boolean m_ok;

		ApplyAction(boolean isOK) {
			super(isOK ? OK_TEXT : APPLY);
			m_ok = isOK;
		}

		/**
		 * Perform setup tasks when OK or APPLY is clicked.
		 * 
		 * @param event
		 *            the event created by clicking OK or APPLY
		 */
		public void actionPerformed(final ActionEvent event) {
			doApply(m_ok);
		}
	}

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * All text message output goes to this object.
	 */
	protected static final Logger LOGGER = Logger.getLogger(AbstractSetup.class
			.getPackage().getName());

	/**
	 * JamStatus instance.
	 * 
	 * @see jam.global.JamStatus
	 */
	protected static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * Apply button.
	 */
	protected final transient AbstractButton bapply;

	/**
	 * Press to browse for a classpath.
	 */
	protected transient final AbstractButton bbrowsef = new JButton("Browse...");

	/**
	 * OK button
	 */
	protected final transient AbstractButton bok;

	/**
	 * Toggle button for the default class path.
	 */
	protected transient final JToggleButton btnDefaultPath;

	/**
	 * When toggled, means that a user-supplied path should be used for
	 * populating the sort chooser.
	 */
	protected transient final JToggleButton btnSpecifyPath;

	/**
	 * The dialog.
	 */
	protected transient final JDialog dialog;

	/**
	 * Chooser for event input stream.
	 */
	protected final transient JComboBox inChooser;

	/** Input stream, how tells how to read an event */
	protected transient AbstractEventInputStream inStream;

	/**
	 * Chooser for event output stream.
	 */
	protected final transient JComboBox outChooser;

	/** Output stream, tells how to write an event */
	protected transient AbstractEventOutputStream outStream;

	/**
	 * sort routine chooser
	 */
	protected transient final SortChooser sortChooser;

	/**
	 * path to base of sort routines' classpath
	 */
	protected transient File specifiedClassPath;

	/**
	 * Text field showing the sort class path.
	 */
	protected transient final JTextField textSortPath;

	AbstractSetup(String dialogName) {
		super();
		// Jam properties needed
		final String defInStream = JamProperties
				.getPropString(PropertyKeys.EVENT_INSTREAM);
		final String defOutStream = JamProperties
				.getPropString(PropertyKeys.EVENT_OUTSTREAM);
		final String defSortPath = JamProperties
				.getPropString(PropertyKeys.SORT_CLASSPATH);
		final boolean useDefault = (defSortPath
				.equals(JamProperties.DEFAULT_SORTPATH));
		final String defSortRoutine = JamProperties
				.getPropString(PropertyKeys.SORT_ROUTINE);

		specifiedClassPath = new File(defSortPath);

		// Create GUI widgets
		bok = new JButton(new ApplyAction(true));
		bapply = new JButton(new ApplyAction(false));
		dialog = new JDialog(STATUS.getFrame(), dialogName, false);
		textSortPath = new JTextField(defSortPath);
		textSortPath.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				specifiedClassPath = new File(textSortPath.getText());
				sortChooser.loadChooserClassPath(specifiedClassPath);
			}
		});
		// Radio buttons
		btnSpecifyPath = new JRadioButton("Specify a classpath", !useDefault);
		btnSpecifyPath
				.setToolTipText("Specify a path to load your sort routine from.");
		btnSpecifyPath.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent itemEvent) {
				if (btnSpecifyPath.isSelected()) {
					selectPath(!btnSpecifyPath.isSelected());
				}
			}
		});
		btnDefaultPath = new JRadioButton(
				"Use help.* and sort.* in default classpath", useDefault);
		btnDefaultPath
				.setToolTipText("Don't include your sort routines in the default"
						+ " classpath\n if you want to be able to edit, recompile and reload "
						+ " without first quitting Jam.");
		btnDefaultPath.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				if (btnDefaultPath.isSelected()) {
					selectPath(btnDefaultPath.isSelected());
				}
			}
		});

		sortChooser = new SortChooser();

		bbrowsef.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				specifiedClassPath = browseSortPath();
				textSortPath.setText(specifiedClassPath.getPath());
				sortChooser.loadChooserClassPath(specifiedClassPath);
			}
		});
		bbrowsef.setEnabled(false);
		textSortPath.setToolTipText("Use Browse button to change. \n"
				+ "May fail if classes have unresolvable references."
				+ "\n* use the sort.classpath property in your JamUser.ini "
				+ "file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEditable(false);
		textSortPath.setEnabled(false);

		// Input streams
		final Set<Class<?>> inStreams = getClasses("jam.sort.stream",
				AbstractEventInputStream.class);
		inChooser = new JComboBox(inStreams.toArray());
		inChooser.setToolTipText("Select input event data format.");
		selectName(inChooser, inStreams, defInStream);

		// Output streams
		final Set<Class<?>> outStreams = getClasses("jam.sort.stream",
				AbstractEventOutputStream.class);
		outChooser = new JComboBox(outStreams.toArray());
		outChooser.setToolTipText("Select output event format.");
		selectName(outChooser, outStreams, defOutStream);

		selectPath(useDefault);
		sortChooser.selectSortClass(defSortRoutine);
	}

	/**
	 * Browses for the sort file.
	 * 
	 * @return the directory to look in for event files
	 */
	protected final File browseSortPath() {
		File rval = specifiedClassPath;
		final JFileChooser chooser = new JFileChooser(specifiedClassPath);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int option = chooser.showOpenDialog(STATUS.getFrame());
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
				&& chooser.getSelectedFile() != null) {
			synchronized (this) {
				rval = chooser.getSelectedFile();// save current directory
			}
		}
		return rval;
	}

	/**
	 * Should be called when OK or Apply is actuated.
	 * 
	 * @param dispose
	 *            whether to dispose of the dialog
	 */
	protected abstract void doApply(boolean dispose);

	/**
	 * Get list of classes implemented a interface
	 */
	private Set<Class<?>> getClasses(final String inPackage, final Class inClass) {
		final RuntimeSubclassIdentifier runtimeSubclassIdentifier = RuntimeSubclassIdentifier.getSingletonInstance();
		final Set<Class<?>> lhs = new java.util.LinkedHashSet<Class<?>>(runtimeSubclassIdentifier
				.find(inPackage, inClass, false));
		lhs.remove(inClass);
		return lhs;
	}

	/**
	 * Returns the dialog for setting up offline sorting.
	 * 
	 * @return the dialog for setting up offline sorting
	 */
	public final JDialog getDialog() {
		return dialog;
	}

	/**
	 * Initializes the sort routine.
	 * 
	 * @throws JamException
	 *             error given with useful error message to the final user
	 */
	protected final void initializeSorter() throws JamException {
		final StringBuffer message = new StringBuffer(400);
		final SortRoutine sortRoutine = sortChooser.getSortRoutine();
		final String sortName = sortRoutine.getClass().getName();
		try {
			sortRoutine.initialize();
		} catch (Exception thrown) {
			message.append("Exception in AbstractSortRoutine: ").append(
					sortName).append(".initialize(); Message= '").append(
					thrown.getClass().getName()).append(": ").append(
					thrown.getMessage()).append('\'');
			throw new JamException(message.toString(), thrown);
		} catch (OutOfMemoryError thrown) {
			message
					.append(sortName)
					.append(
							" attempts to allocate too much memory. Reduce its requirments or start Jam with more available heap space. The current maximum amount of memory available to the JVM is ");
			final double megabytes = Runtime.getRuntime().maxMemory()
					/ (1024.0 * 1024.0);
			message.append(megabytes).append(" MB.");// NOPMD
			throw new JamException(message.toString(), thrown);
		} catch (Throwable thrown) {// NOPMD
			message
					.append("Couldn't load ")
					.append(sortName)
					.append(
							"; You probably need to re-compile it against the current version of Jam.");
			throw new JamException(message.toString(), thrown);
		}
		/* setup scaler, parameter, monitors, gate, dialog boxes */
		AbstractControl.setupAll();
	}

	/**
	 * Locks up the setup so the fields cannot be edited.
	 * 
	 * @param lock
	 *            is true if the fields are to be locked
	 */
	protected abstract void lockMode(boolean lock);

	/**
	 * Do what it takes to open up the tree to the first histogram in the sort
	 * routine.
	 */
	public void selectFirstSortHistogram() {
		// Select first histogram
		final Group sortGroup = Group.getSortGroup();
		STATUS.setCurrentGroup(sortGroup);
		final List<Histogram> histList = sortGroup.getHistogramList();
		if (!histList.isEmpty()) {
			final Histogram firstHist = histList.get(0);
			SelectionTree.setCurrentHistogram(firstHist);
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT);
	}

	/**
	 * Finds a class matching the given string in the collection, and attempts
	 * to select it from the chooser.
	 * 
	 * @param jcb
	 *            chooser
	 * @param collection
	 *            of <code>Class</code> objects
	 * @param defInStream
	 *            name of class to try to select
	 */
	protected final void selectName(final JComboBox jcb,
			final Collection<Class<?>> collection, final String defInStream) {
		for (Class clazz : collection) {
			final String name = clazz.getName();
			if (name.equals(defInStream)) {
				jcb.setSelectedItem(clazz);
				break;
			}
		}
	}

	protected final void selectPath(final boolean useDefault) {
		if (useDefault) {
			bbrowsef.setEnabled(false);
			textSortPath.setEnabled(false);
			textSortPath.setEditable(false);
			textSortPath.setText("default");
			sortChooser.loadChooserDefault();
		} else {
			bbrowsef.setEnabled(true);
			textSortPath.setEnabled(true);
			textSortPath.setEditable(true);
			textSortPath.setText(specifiedClassPath.getPath());
			sortChooser.loadChooserClassPath(specifiedClassPath);
		}

	}

	/**
	 * Sets up the sort.
	 * 
	 * @throws SortException
	 *             if there's a problem
	 * @throws JamException
	 *             if there's a problem
	 */
	protected abstract void setupSort() throws SortException, JamException;
}
