package jam.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import jam.data.AbstractHist1D;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
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

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Box;

import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;


/**
 * Panel to select multipel files
 * 
 * @author Ken Swartz
 *
 */
public class MultipleFileChooser extends JPanel {

	private  JList listFiles;
		
	private DefaultListModel listFilesModel;
	
	private File lastFile; //last file referred to in a JFileChooser
	
	JPanel pCommands;
	
	JButton bSaveList;
	
	JButton bLoadList;
	
	private String fileExtension="*";
	
	/** Main Frame */
	Frame frame;
	/** Messages output */
	private MessageHandler msgHandler;
	
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
		
		pCommands = new JPanel();
		pCommands.setLayout(new BoxLayout(pCommands, BoxLayout.Y_AXIS));

		
		pCommands.setBorder(new EmptyBorder(0, 5, 0, 0));
		this.add(pCommands, BorderLayout.WEST);
		
		JPanel pButtons = new JPanel(new GridLayout(0, 1, 5, 2));
		
		pCommands.add(Box.createVerticalGlue());				
		pCommands.add(Box.createVerticalGlue());		
		pCommands.add(Box.createVerticalGlue());
		pCommands.add(pButtons);
		pCommands.add(Box.createVerticalGlue());
		pCommands.add(Box.createVerticalGlue());
		pCommands.add(Box.createVerticalGlue());		
		
		JButton bAddfile = new JButton("Add File");
		pButtons.add(bAddfile);
		bAddfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				addFile();
			}
		});
	
		JButton bAddDir = new JButton("Add Directory");
		pButtons.add(bAddDir);
		bAddDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				addDirectory();
			}
		});
	
		JButton bRemove = new JButton("Remove File");
		pButtons.add(bRemove);
		bRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removeFile();
			}
		});
		
		JButton bRemoveAll = new JButton("Remove All");
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
	
	public void setFileExtension(String extension) {
		fileExtension=extension;
	}
	/**
	 * Activate the save and load list buttons
	 * @param state
	 */
	public void activeListSaveLoadButtons(boolean state){
		if (state) {
			pCommands.add(bLoadList);
			pCommands.add(bSaveList);
		} else {
			pCommands.remove(bLoadList);
			pCommands.remove(bSaveList);
		}
	}
	//public List getList() {
	//	return listFilesModel.
	//}
	
	public void setSelectionIndex(int index) {
		listFiles.setSelectedIndex(index); 
	}
	public File getSelectedFile() {
		File file = (File)listFiles.getSelectedValue();
		if (file ==null && listFilesModel.getSize()>0) {
			listFiles.setSelectedIndex(0);
			file = (File)listFiles.getSelectedValue();
		}
		return file;
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
	
	/**
	 * browse for data files
	 */
	private void addFile() {
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(new ExtensionFileFilter(new String[] { fileExtension },
				"Data Files (*."+fileExtension+")"));
		int option = fd.showOpenDialog(frame);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			listFilesModel.addElement(fd.getSelectedFile());
		}
	}
	
	/**
	 * add all files in a directory to list
	 *  
	 */
	private void addDirectory() {
		final JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int option = fd.showOpenDialog(frame);
		/* save current values */
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
			lastFile = fd.getSelectedFile(); //save current directory
			addDirFiles(lastFile);
		}
	}
	/**
	 * Adds a directory of files
	 * @param f
	 * @return
	 */
	int addDirFiles(File f) {
		int numFiles = 0;
		if (f != null && f.exists()) {
			final ExtensionFileFilter ff = new ExtensionFileFilter(
					new String[] { fileExtension }, "Data Files(*."+fileExtension+")");
			if (f.isFile() && ff.accept(f)) {
				listFilesModel.addElement(f);
				numFiles++;
			}
			if (f.isDirectory()) {
				File[] dirArray = f.listFiles();
				for (int i = 0; i < dirArray.length; i++) {
					if (ff.accept(dirArray[i]))
						listFilesModel.addElement(dirArray[i]);
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
