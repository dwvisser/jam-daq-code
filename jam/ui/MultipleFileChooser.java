package jam.ui;

import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;

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
import java.util.Collections;
import java.util.List;

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

	private  JList listFiles;
		
	private DefaultListModel listFilesModel;
	
	private File lastFile; //last file referred to in a JFileChooser
	
	private JPanel pButtons;
	
	private JButton bSaveList;
	
	private JButton bLoadList;
	
	private JButton bAddfile, bAddDir, bRemove, bRemoveAll;
	
	private FileFilter fileFilter;
	
	/** Main Frame */
	private final Frame frame;
	/** Messages output */
	private MessageHandler msgHandler;
	
	/**
	 * Constructs a multiple file chooser panel.
	 * @param frame parent frame
	 * @param msgHandler where to print messages
	 */
	public MultipleFileChooser(Frame frame, MessageHandler msgHandler) {
		this.frame =frame;
		this.msgHandler=msgHandler;		
	
		this.setLayout(new BorderLayout(5,5));
		//Panel with list 
		listFilesModel = new DefaultListModel();
		listFiles = new JList(listFilesModel);
		//listFiles.setBounds()
		listFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.add(new JScrollPane(listFiles), BorderLayout.CENTER);

		//Commands Panel		
		JPanel pLeft = new JPanel();
		pLeft.setLayout(new BoxLayout(pLeft, BoxLayout.Y_AXIS));		
		pLeft.setBorder(new EmptyBorder(0, 5, 0, 0));
		this.add(pLeft, BorderLayout.WEST);
		
		pButtons = new JPanel(new GridLayout(0, 1, 5, 2));
		
		pLeft.add(Box.createVerticalGlue());				
		pLeft.add(Box.createVerticalGlue());		
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(pButtons);
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(Box.createVerticalGlue());
		pLeft.add(Box.createVerticalGlue());		
		
		bAddfile = new JButton("Add File");
		pButtons.add(bAddfile);
		bAddfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				selectFile(true);
			}
		});
	
		bAddDir = new JButton("Add Directory");
		pButtons.add(bAddDir);
		bAddDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				selectFile(false);
			}
		});
	
		bRemove = new JButton("Remove File");
		pButtons.add(bRemove);
		bRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removeFile();
			}
		});
		
		bRemoveAll = new JButton("Remove All");
		pButtons.add(bRemoveAll);		
		bRemoveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removeAllFiles();
			}
		});
		
		bLoadList = new JButton("Load List");
		bLoadList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				loadList();
			}
		});

		bSaveList = new JButton("Save List");
		bSaveList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				saveList();
			}
		});
		
	}
	

	/**
	 * Sets the file filter to browse for
	 * @param filter type to browse for
	 */
	public void setFileFilter(FileFilter filter) {
		fileFilter=filter;
	}
	
	/**
	 * Sets the file extension to browse for, creates a default
	 * file filter
	 * @param extension file extension
	 * @param extensionName label for file type
	 */
	public void setFileFilter(String extensionName, String extension) {	
		fileFilter =new ExtensionFileFilter(extension ,
						extensionName);
	}

	/**
	 * Activate the save and load list buttons
	 * @param state
	 */
	public void showListSaveLoadButtons(boolean state){
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
	 * @param state <code>true</code> to enable the buttons
	 */
	public void setLocked(boolean state){
		bAddfile.setEnabled(state);
		bAddDir.setEnabled(state);
		bRemove.setEnabled(state);
		bRemoveAll.setEnabled(state);
		bLoadList.setEnabled(state);
		bSaveList.setEnabled(state);
	}
	
	/**
	 * Get selected file, no selection set first file
	 * as selected
	 * @return the selected file
	 */
	public File getSelectedFile() {
		File file = (File)listFiles.getSelectedValue();
		if (file ==null && listFilesModel.getSize()>0) {
			listFiles.setSelectedIndex(0);
			file = (File)listFiles.getSelectedValue();
		}
		return file;
	}
	
	/**
	 * Returns the list of files in the data model.
	 * @return the list of files in the data model
	 */
	public List getFileList() {
		return Collections.list(listFilesModel.elements());
	}	
	
	/**
	 * Add a file to the list.
	 * 
	 * @param file to add
	 */
	public void addFile(File file){
		listFilesModel.addElement(file);
	}
	
	/**
	 * save list of items to sort
	 */
	public void saveList() {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(new ExtensionFileFilter(new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showSaveDialog(frame);
		// save current values 
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			try {
				FileWriter saveStream = new FileWriter(lastFile);
				for (int i = 0; i < listFilesModel.size(); i++) {
					final File f = (File) listFilesModel.elementAt(i);
					saveStream.write(f.getAbsolutePath());
					saveStream.write("\n");
				}
				saveStream.close();
			} catch (IOException ioe) {
				msgHandler
						.errorOutln("Unable to save list to file "
								+ lastFile.getName());
			}
		}
	}
	/**
	 * load a list of items to sort from a file
	 *  
	 */
	public void loadList() {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(new ExtensionFileFilter(new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showOpenDialog(frame);
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
			readFileList(fd.getSelectedFile());
		}
	}
	
	int readFileList(File f) {
		int numFiles = 0;
		lastFile = f;
		try {
			BufferedReader br = new BufferedReader(new FileReader(lastFile));
			String listItem;
			do {
				listItem = br.readLine();
				if (listItem != null) {
					final File fEvn = new File(listItem);
					listFilesModel.addElement(fEvn);
					numFiles++;
				}
			} while (listItem != null);
			br.close();
		} catch (IOException ioe) {
			msgHandler.errorOutln("Unable to load list from file "
					+ f);
		}
		return numFiles;
	}
	
	/* non-javadoc:
	 * Select a file, browse for data files.
	 */
	private void selectFile(boolean selectFileOnly) {
		int chooserMode;

		final JFileChooser fd = new JFileChooser(lastFile);		
		if (selectFileOnly) {
			chooserMode=JFileChooser.FILES_ONLY;
			fd.setMultiSelectionEnabled(true);			
		} else {
			chooserMode=JFileChooser.DIRECTORIES_ONLY;
			fd.setMultiSelectionEnabled(false);			
		}		
		fd.setFileSelectionMode(chooserMode);

		//fd.setFileFilter(fileFilter));
		if (selectFileOnly){
		    fd.setFileFilter(fileFilter);
		}
		final int option = fd.showOpenDialog(frame);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
		    if (selectFileOnly){				
		    	if (fd.isMultiSelectionEnabled()) {
		    		File [] files =fd.getSelectedFiles();
		    		for (int i=0; i<files.length;i++ ) {
		    			listFilesModel.addElement(files[i]);
		    		}
		    	}else {
		    		addFile(fd.getSelectedFile());
		    	}
		    } else {
				lastFile = fd.getSelectedFile(); //save current directory
				addDirFiles(lastFile);
		    }
		    listFiles.setSelectedIndex(0);
		}
		
	}
	
	/**
	 * Adds a directory of files
	 * @param f the directory to add
	 * @return number of files
	 */
	private int addDirFiles(File f) {
		int numFiles = 0;
		if (f != null && f.exists()) {
			if (f.isFile() && fileFilter.accept(f)) {
				addFile(f);
				numFiles++;
			} else if (f.isDirectory()) {
				File[] dirArray = f.listFiles();
				for (int i = 0; i < dirArray.length; i++) {
					if (fileFilter.accept(dirArray[i])){
						addFile(dirArray[i]);
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
		Object[] removeList = listFiles.getSelectedValues();
		for (int i = 0; i < removeList.length; i++) {
			listFilesModel.removeElement(removeList[i]);
		}
	}
	
	/**
	 * remove all file from the list
	 *  
	 */
	private void removeAllFiles() {
		listFilesModel.removeAllElements();
	}
	
}
