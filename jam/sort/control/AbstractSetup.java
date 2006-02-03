/*
 * Created on Dec 21, 2004
 */
package jam.sort.control;

import jam.JamException;
import jam.data.control.AbstractControl;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.PropertyKeys;
import jam.global.RTSI;
import jam.global.Sorter;
import jam.sort.SortException;
import jam.sort.SortRoutine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

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
	 * Handle to event broadcaster.
	 */
	protected static final Broadcaster BROADCASTER = Broadcaster
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
	protected static void selectName(final JComboBox jcb,
			final Collection<Class<?>> collection, final String defInStream) {
		for (Class clazz : collection) {
			final String name = clazz.getName();
			final boolean match = name.equals(defInStream);
			if (match) {
				jcb.setSelectedItem(clazz);
				break;
			}
		}
	}

	/**
	 * Press to browse for a classpath.
	 */
	protected transient final AbstractButton bbrowsef = new JButton("Browse...");

	/**
	 * path to base of sort routines' classpath
	 */
	protected transient File classPath;

	/**
	 * Toggle button for the default class path.
	 */
	protected transient final JToggleButton defaultPath;

	/**
	 * The dialog.
	 */
	protected transient final JDialog dialog;

	/**
	 * Last select path
	 */
	protected transient String lastSortPath = "";

	/**
	 * When toggled, means that a user-supplied path should be used for
	 * populating the sort chooser.
	 */
	protected transient final JToggleButton specify;

	/**
	 * Text field showing the sort class path.
	 */
	protected transient final JTextField textSortPath;
	
	/**
	 * sort routine chooser
	 */
	protected transient final SortChooser sortChooser;

	AbstractSetup(String dialogName) {
		super();
		dialog = new JDialog(STATUS.getFrame(), dialogName, false);
		final String defSortPath = JamProperties
				.getPropString(PropertyKeys.SORT_CLASSPATH);
		final boolean useDefault = (defSortPath
				.equals(JamProperties.DEFAULT_SORTPATH));
		textSortPath = new JTextField(defSortPath);
		sortChooser = new SortChooser(textSortPath);
		specify = new JRadioButton("Specify a classpath", !useDefault);
		specify
				.setToolTipText("Specify a path to load your sort routine from.");
		specify.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent itemEvent) {
				if (specify.isSelected()) {
					bbrowsef.setEnabled(true);
					sortChooser.setChooserDefault(false);
					textSortPath.setEnabled(true);
					textSortPath.setText(lastSortPath);

				}
			}
		});
		if (!useDefault) {
			classPath = new File(defSortPath);
		}
		bbrowsef.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				sortChooser.setSortClassPath(getSortPath());
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
		defaultPath = new JRadioButton(
				"Use help.* and sort.* in default classpath", useDefault);
		defaultPath
				.setToolTipText("Don't include your sort routines in the default"
						+ " classpath if you want to be able to edit, recompile and reload them"
						+ " without first quitting Jam.");
		defaultPath.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				if (defaultPath.isSelected()) {
					bbrowsef.setEnabled(false);
					sortChooser.setChooserDefault(true);
					textSortPath.setEnabled(false);
					lastSortPath = textSortPath.getText();
					textSortPath.setText("default");

				}
			}
		});
	}

	/**
	 * Should be called when OK or Apply is actuated.
	 * 
	 * @param dispose
	 *            whether to dispose of the dialog
	 */
	protected abstract void doApply(boolean dispose);

	/**
	 * Returns the dialog for setting up offline sorting.
	 * 
	 * @return the dialog for setting up offline sorting
	 */
	public final JDialog getDialog() {
		return dialog;
	}

	/**
	 * Get the sort classes using the given file as the class path.
	 * 
	 * @param path
	 *            class path
	 * @return set of available sort routines
	 */
	static final Set<Class<?>> getSortClasses(final File path) {
		return RTSI.getSingletonInstance().find(path, Sorter.class);
	}

	/**
	 * Browses for the sort file.
	 * 
	 * @return the directory to look in for event files
	 */
	protected final File getSortPath() {
		File rval = classPath;
		final JFileChooser chooser = new JFileChooser(classPath);
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
	 * Initializes the sort routine.
	 * 
	 * @throws JamException
	 *             error given with useful error message to the final user
	 */
	protected final void initializeSorter() throws JamException {
		final StringBuffer message = new StringBuffer();
		final SortRoutine sortRoutine = sortChooser.getSortRoutine();
		final String sortName = sortRoutine.getClass().getName();
		try {
			sortRoutine.initialize();
		} catch (Exception thrown) {
			message.append("Exception in SortRoutine: ")
					.append(sortName).append(".initialize(); Message= '")
					.append(thrown.getClass().getName()).append(": ").append(
							thrown.getMessage()).append('\'');
			throw new JamException(message.toString(), thrown);
		} catch (OutOfMemoryError thrown) {
			message.append(sortName).append(
					" attempts to allocate too much memory. ");
			message
					.append("Reduce its requirments or start Jam with more available heap space. ");
			message
					.append("The current maximum amount of memory available to the JVM is ");
			final double megabytes = Runtime.getRuntime().maxMemory()
					/ (1024.0 * 1024.0);
			message.append(megabytes).append(" MB.");
			throw new JamException(message.toString(), thrown);
		} catch (Throwable thrown) {// NOPMD
			message
					.append("Couldn't load ")
					.append(sortName)
					.append("; You probably ")
					.append(
							"need to re-compile it against the current version of Jam.");
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
	 * Sets up the sort.
	 * 
	 * @throws SortException
	 *             if there's a problem
	 * @throws JamException
	 *             if there's a problem
	 */
	protected abstract void setupSort() throws SortException, JamException;
}
