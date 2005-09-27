/*
 * Created on Dec 21, 2004
 */
package jam;

import jam.data.Group;
import jam.data.control.AbstractControl;
import jam.global.Broadcaster;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.RTSI;
import jam.global.Sorter;
import jam.sort.SortException;
import jam.sort.SortRoutine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
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
 * @see jam.SetupSortOn
 * @see jam.SetupSortOff
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
			final Collection collection, final String defInStream) {
		final Iterator iter = collection.iterator();
		while (iter.hasNext()) {
			final Class clazz = (Class) iter.next();
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
	 * Name of class. Ultimately used to create messages.
	 */
	protected transient final String classname;

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
	 * Combo box for selecting a sort routine.
	 * 
	 * @see jam.sort.SortRoutine
	 */
	protected transient final JComboBox sortChoice = new JComboBox();

	private transient Class sortClass;

	/**
	 * User sort routine must extend this abstract class
	 */
	protected transient SortRoutine sortRoutine;// the actual sort routine

	/**
	 * When toggled, means that a user-supplied path should be used for
	 * populating the sort chooser.
	 */
	protected transient final JToggleButton specify;

	/**
	 * Text field showing the sort class path.
	 */
	protected transient final JTextField textSortPath;

	AbstractSetup(String dialogName) {
		super();
		dialog = new JDialog(STATUS.getFrame(), dialogName, false);
		sortChoice.setToolTipText("Select sort routine class");
		sortChoice.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				sortClass = (Class) sortChoice.getSelectedItem();
			}
		});
		final String defSortPath = JamProperties
				.getPropString(JamProperties.SORT_CLASSPATH);
		final boolean useDefault = (defSortPath
				.equals(JamProperties.DEFAULT_SORT_CLASSPATH));
		specify = new JRadioButton("Specify a classpath", !useDefault);
		specify
				.setToolTipText("Specify a path to load your sort routine from.");
		specify.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent itemEvent) {
				if (specify.isSelected()) {
					bbrowsef.setEnabled(true);
					setChooserDefault(false);
				}
			}
		});
		classname = getClass().getName() + "--";
		if (!useDefault) {
			classPath = new File(defSortPath);
		}
		bbrowsef.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				setSortClassPath(getSortPath());
			}
		});
		textSortPath = new JTextField(defSortPath);
		textSortPath.setToolTipText("Use Browse button to change. \n"
				+ "May fail if classes have unresolvable references."
				+ "\n* use the sort.classpath property in your JamUser.ini "
				+ "file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEditable(false);
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
					setChooserDefault(true);
				}
			}
		});
	}

	/**
	 * Should be called when OK or Apply is actuated.
	 * 
	 * @param dispose whether to dispose of the dialog
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
	protected final Set<Class<?>> getSortClasses(final File path) {
		return RTSI.find(path, Sorter.class);
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
		final String sortName = sortRoutine.getClass().getName();
		try {
			sortRoutine.initialize();
		} catch (Exception thrown) {
			message.append(classname).append("Exception in SortRoutine: ")
					.append(sortName).append(".initialize(); Message= '")
					.append(thrown.getClass().getName()).append(": ").append(
							thrown.getMessage()).append('\'');
			throw new JamException(message.toString(), thrown);
		} catch (OutOfMemoryError thrown) {
			message.append(classname).append(
					" attempts to allocate too much memory. ");
			message
					.append("Reduce its requirments or start Jam with more available heap space. ");
			message
					.append("The current maximum amount of memory available to the JVM is ");
			final double megabytes = Runtime.getRuntime().maxMemory()
					/ (1024.0 * 1024.0);
			message.append(megabytes).append(" MB.");
			throw new JamException(message.toString(), thrown);
		} catch (Throwable thrown) {
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
	 * Resolves the String objects into class names and loads the sorting class
	 * and event streams.
	 * 
	 * @throws JamException
	 *             if there's a problem
	 */
	protected final void loadSorter() throws JamException {
		if (sortClass == null) {
			sortClass = (Class) sortChoice.getSelectedItem();
		}
		// FIXME maybe we should do DataBase.clearAll(); here
		Group.clearList();
		final String sortName = Group.parseSortClassName(sortClass.getName());
		Group.createGroup(sortName, Group.Type.SORT);
		try {
			if (specify.isSelected()) {
				/* we call loadClass() in order to guarantee latest version */
				synchronized (this) {
					sortRoutine = (SortRoutine) RTSI.loadClass(classPath,
							sortClass.getName()).newInstance();// create sort
					// class
				}
			} else {// use default loader
				synchronized (this) {
					sortRoutine = (SortRoutine) sortClass.newInstance();
				}
			}
		} catch (InstantiationException ie) {
			throw new JamException(classname
					+ "Cannot instantiate sort routine: " + sortClass.getName());
		} catch (IllegalAccessException iae) {
			throw new JamException(classname + "Cannot access sort routine: "
					+ sortClass.getName());
		}
	}

	/**
	 * Locks up the setup so the fields cannot be edited.
	 * 
	 * @param lock is true if the fields are to be locked
	 */
	protected abstract void lockMode(boolean lock);
	
	/**
	 * Sets whether to use the default classpath or a user-specified one.
	 * 
	 * @param isDefault
	 *            <code>true</code> to use the default classpath
	 * @return a list of the available sort routines
	 */
	protected final List<Class<?>> setChooserDefault(final boolean isDefault) {
		final List<Class<?>> list = new ArrayList<Class<?>>();
		if (isDefault) {
			final Set<Class<?>> set = new LinkedHashSet<Class<?>>();
			set.addAll(RTSI.find("help", Sorter.class, true));
			set.addAll(RTSI.find("sort", Sorter.class, true));
			list.addAll(set);
		} else {
			list.addAll(getSortClasses(classPath));
		}
		sortChoice.setModel(new DefaultComboBoxModel(list.toArray()));
		return list;
	}
	
	/**
	 * Sets the class path for loading sort routines.
	 * 
	 * @param file
	 *            path to classes
	 */
	protected final void setSortClassPath(final File file) {
		if (file.exists()) {
			classPath = file;
			sortChoice.setModel(new DefaultComboBoxModel(getSortClasses(
					classPath).toArray()));
			if (sortChoice.getModel().getSize() > 0) {
				sortChoice.setSelectedIndex(0);
			}
			textSortPath.setText(classPath.getAbsolutePath());
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
