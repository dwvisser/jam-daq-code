package jam.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 * Panel to select multiple files.
 * 
 * @author Ken Swartz
 */
public final class MultipleFileChooser extends JPanel {

	private static final Logger LOGGER = Logger
			.getLogger(MultipleFileChooser.class.getPackage().getName());

	private transient final DefaultListModel<File> listFilesModel = new DefaultListModel<File>();

	private transient final JList<File> listFiles = new JList<File>(
			listFilesModel);

	/* last file referred to in a JFileChooser */
	private transient File lastFile;

	private transient final JPanel pButtons = new JPanel(new GridLayout(0, 1,
			5, 2));

	private transient final JButton bSaveList = new JButton("Save List");

	private transient final JButton bLoadList = new JButton("Load List");

	private transient final JButton bAddfile = new JButton("Add File");

	private transient final JButton bAddDir = new JButton("Add Directory");

	private transient final JButton bRemove = new JButton("Remove File");

	private transient final JButton bRemoveAll = new JButton("Remove All");

	private transient FileFilter fileFilter;

	/** Main Frame */
	private transient final Frame frame;

	/**
	 * Constructs a multiple file chooser panel.
	 * 
	 * @param frame
	 *            parent frame
	 */
	public MultipleFileChooser(final Frame frame) {
		super(new BorderLayout(5, 5));
		this.frame = frame;
		// Panel with list - center of panel with the list of files
		listFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.add(new JScrollPane(listFiles), BorderLayout.CENTER);
		// Commands Panel - Left side of panel with the buttons
		final JPanel pLeft = new JPanel();
		pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));
		pLeft.setBorder(new EmptyBorder(0, 5, 0, 0));
		this.add(pLeft, BorderLayout.WEST);
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(pButtons);
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(Box.createVerticalGlue());
		pButtons.add(bAddfile);
		bAddfile.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				selectFile(true);
			}
		});
		pButtons.add(bAddDir);
		bAddDir.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				selectFile(false);
			}
		});
		pButtons.add(bRemove);
		bRemove.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				removeFile();
			}
		});
		pButtons.add(bRemoveAll);
		bRemoveAll.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				removeAllFiles();
			}
		});
		bLoadList.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				loadList();
			}
		});
		bSaveList.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				saveList();
			}
		});
	}

	/**
	 * @param frame
	 *            parent frame
	 * @param startFolder
	 *            place to start browsing
	 */
	public MultipleFileChooser(Frame frame, File startFolder) {
		this(frame);
		this.lastFile = startFolder;
	}

	/**
	 * Sets the file filter to browse for
	 * 
	 * @param filter
	 *            type to browse for
	 */
	public void setFileFilter(final FileFilter filter) {
		fileFilter = filter;
	}

	/**
	 * Activate the save and load list buttons
	 * 
	 * @param state
	 */
	public void showListSaveLoadButtons(final boolean state) {
		if (state) {
			pButtons.add(bLoadList);
			pButtons.add(bSaveList);
		} else {
			pButtons.remove(bLoadList);
			pButtons.remove(bSaveList);
		}
	}

	/**
	 * Lock or unlock the buttons.
	 * 
	 * @param state
	 *            <code>true</code> to enable the buttons
	 */
	public void setLocked(final boolean state) {
		final boolean enabled = !state;
		bAddfile.setEnabled(enabled);
		bAddDir.setEnabled(enabled);
		bRemove.setEnabled(enabled);
		bRemoveAll.setEnabled(enabled);
		bLoadList.setEnabled(enabled);
		bSaveList.setEnabled(enabled);
	}

	/**
	 * Get selected file, no selection set first file as selected
	 * 
	 * @return the selected file
	 */
	public File getSelectedFile() {
		File file = listFiles.getSelectedValue();
		if (file == null && listFilesModel.getSize() > 0) {
			listFiles.setSelectedIndex(0);
			file = listFiles.getSelectedValue();
		}
		return file;
	}

	/**
	 * Returns the list of files in the data model.
	 * 
	 * @return the list of files in the data model
	 */
	public List<File> getFileList() {
		final List<?> tempList = Collections.list(listFilesModel.elements());
		final List<File> rval = new ArrayList<File>(tempList.size());
		for (Object object : tempList) {
			rval.add((File) object);
		}
		return Collections.unmodifiableList(rval);
	}

	/**
	 * Add a file to the list.
	 * 
	 * @param file
	 *            to add
	 */
	public void addFile(final File file) {
		listFilesModel.addElement(file);
	}

	/**
	 * save list of items to sort
	 */
	public void saveList() {
		final JFileChooser fileChooser = new JFileChooser(lastFile);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new ExtensionFileFilter(
				new String[] { "lst" }, "List Files (*.lst)"));
		final int option = fileChooser.showSaveDialog(frame);
		// save current values
		if (option == JFileChooser.APPROVE_OPTION
				&& fileChooser.getSelectedFile() != null) {
			lastFile = fileChooser.getSelectedFile(); // save current
			// directory
			FileWriter saveStream = null;
			try {
				saveStream = new FileWriter(lastFile);
				for (int i = 0; i < listFilesModel.size(); i++) {
					final File file = listFilesModel.elementAt(i);
					saveStream.write(file.getAbsolutePath());
					saveStream.write("\n");
				}
			} catch (IOException ioe) {
				LOGGER.log(Level.SEVERE, "Unable to save list to file "
						+ lastFile.getName(), ioe);
			} finally {
				if (saveStream != null) {
					try {
						saveStream.close();
					} catch (IOException ioe) {
						LOGGER.log(Level.SEVERE, "Unable to close file "
								+ lastFile.getName(), ioe);
					}
				}
			}
		}
	}

	/**
	 * load a list of items to sort from a file
	 * 
	 */
	public void loadList() {
		final JFileChooser fileChooser = new JFileChooser(lastFile);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new ExtensionFileFilter(
				new String[] { "lst" }, "List Files (*.lst)"));
		final int option = fileChooser.showOpenDialog(frame);
		if (option == JFileChooser.APPROVE_OPTION
				&& fileChooser.getSelectedFile() != null) {
			readFileList(fileChooser.getSelectedFile());
		}
	}

	protected int readFileList(final File file) {
		int numFiles = 0;
		lastFile = file;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(lastFile));
			String listItem;
			do {
				listItem = reader.readLine();
				if (listItem != null) {
					final File fEvn = new File(listItem);// NOPMD
					listFilesModel.addElement(fEvn);
					numFiles++;
				}
			} while (listItem != null);
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Unable to load list from file " + file,
					ioe);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					LOGGER.log(Level.SEVERE, "Unable to close file " + file,
							ioe);
				}
			}
		}
		return numFiles;
	}

	/*
	 * non-javadoc: Select a file, browse for data files.
	 */
	private void selectFile(final boolean selectFileOnly) {
		int chooserMode;
		final JFileChooser fileChooser = new JFileChooser(lastFile);
		if (selectFileOnly) {
			chooserMode = JFileChooser.FILES_ONLY;
			fileChooser.setMultiSelectionEnabled(true);
		} else {
			chooserMode = JFileChooser.DIRECTORIES_ONLY;
			fileChooser.setMultiSelectionEnabled(false);
		}
		fileChooser.setFileSelectionMode(chooserMode);
		if (selectFileOnly) {
			fileChooser.setFileFilter(fileFilter);
		}
		final int option = fileChooser.showOpenDialog(frame);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
				&& fileChooser.getSelectedFile() != null) {
			if (selectFileOnly) {
				if (fileChooser.isMultiSelectionEnabled()) {
					final File[] files = fileChooser.getSelectedFiles();
					for (File file : files) {
						listFilesModel.addElement(file);
					}
				} else {
					addFile(fileChooser.getSelectedFile());
				}
			} else {
				lastFile = fileChooser.getSelectedFile(); // save current
				// directory
				addDirFiles(lastFile);
			}
			listFiles.setSelectedIndex(0);
		}

	}

	/**
	 * Adds a directory of files
	 * 
	 * @param file
	 *            the directory to add
	 * @return number of files
	 */
	private int addDirFiles(final File file) {
		int numFiles = 0;
		if (file != null && file.exists()) {
			if (file.isFile() && fileFilter.accept(file)) {
				addFile(file);
				numFiles++;
			} else if (file.isDirectory()) {
				final File[] dirArray = file.listFiles();
				for (File aDirArray : dirArray) {
					if (fileFilter.accept(aDirArray)) {
						addFile(aDirArray);
					}
					numFiles++;
				}
			}
		}
		return numFiles;
	}

	/**
	 * remove a file from the list
	 */
	private void removeFile() {
		final List<File> removeList = listFiles.getSelectedValuesList();
		for (File file : removeList) {
			listFilesModel.removeElement(file);
		}
	}

	/**
	 * remove all file from the list
	 * 
	 */
	public void removeAllFiles() {
		listFilesModel.removeAllElements();
	}

	/**
	 * @return the folder that this instance will currently open
	 */
	public File getCurrentFolder() {
		return new JFileChooser(lastFile).getCurrentDirectory();
	}

}
