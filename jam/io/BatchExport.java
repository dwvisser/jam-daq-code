package jam.io;

import jam.JamMain;
import jam.data.Histogram;
import jam.global.MessageHandler;
import jam.global.RTSI;
import jam.global.JamStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * Dialog for exporting lists of histograms.  Searches 
 * <code>jam.io</code> for
 * all classes extending <code>jam.io.ImpExp</code>.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public class BatchExport extends JDialog implements ActionListener {

	private ImpExp[] exportClasses;
	private JRadioButton[] exportChoice;
	private JTextField t_addName, t_directory;
	private JList l_hists;
	private JButton b_export;
	private MessageHandler console;
	private JamMain jam;

	public BatchExport(JamMain jam, MessageHandler console) {
		super(jam);
		this.jam = jam;
		this.console = console;
		setTitle("Batch Histogram Export");
		getClasses();
		buildGUI();
		this.pack();
	}

	private void buildGUI() {
		Container contents = this.getContentPane();
		contents.setLayout(new BorderLayout());

		JPanel p_north = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p_north.add(new JLabel("Directory"));
		t_directory = new JTextField(System.getProperty("user.home"),40);
		t_directory.setToolTipText("Where exported spectra are written.");
		p_north.add(t_directory);
		JButton b_browse = new JButton("Browse");
		b_browse.setActionCommand("browse");
		b_browse.setToolTipText("Browse filesystem.");
		b_browse.addActionListener(this);
		p_north.add(b_browse);
		contents.add(p_north, BorderLayout.NORTH);

		JPanel p_buttons = new JPanel(new GridLayout(0, 2));
		JButton b_addHist = new JButton("Add Histogram");
		b_addHist.setToolTipText("Add currently selected histogram.");
		b_addHist.setActionCommand("addhist");
		b_addHist.addActionListener(this);
		p_buttons.add(b_addHist);
		p_buttons.add(new JLabel("<- Use Main Chooser"));
		JButton b_addName = new JButton("Add Name");
		b_addName.setToolTipText("Add all histograms with names starting with...");
		b_addName.setActionCommand("addname");
		b_addName.addActionListener(this);
		p_buttons.add(b_addName);
		t_addName = new JTextField();
		p_buttons.add(t_addName);
		JButton b_removeName = new JButton("Remove Selected");
		b_removeName.setToolTipText("Remove selected names.");
		b_removeName.setActionCommand("removename");
		b_removeName.addActionListener(this);
		p_buttons.add(b_removeName);
		JButton b_removeAll = new JButton("Remove All");
		b_removeAll.setToolTipText("Remove all names.");
		b_removeAll.setActionCommand("removeall");
		b_removeAll.addActionListener(this);
		p_buttons.add(b_removeAll);
		JButton b_loadList = new JButton("Load List");
		b_loadList.setToolTipText("Load list of names from file.");
		b_loadList.setActionCommand("loadlist");
		b_loadList.addActionListener(this);
		p_buttons.add(b_loadList);
		JButton b_saveList = new JButton("Save List");
		b_saveList.setToolTipText("Save list of names to file.");
		b_saveList.setActionCommand("savelist");
		b_saveList.addActionListener(this);
		p_buttons.add(b_saveList);
		contents.add(p_buttons, BorderLayout.WEST);

		JPanel p_list = new JPanel();
		p_list.setToolTipText("List of histograms to export.");
		l_hists = new JList(new DefaultListModel());
		l_hists.setSelectionMode(
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		l_hists.setBackground(Color.white);
		l_hists.setForeground(Color.black);
		p_list.add(new JScrollPane(l_hists));
		contents.add(p_list, BorderLayout.CENTER);

		JPanel options = new JPanel(new GridLayout(0, 1));
		ButtonGroup optionButtons = new ButtonGroup();
		exportChoice = new JRadioButton[exportClasses.length];
		for (int i = 0; i < exportClasses.length; i++) {
			exportChoice[i] =
				new JRadioButton(
					exportClasses[i].getFormatDescription()
						+ " ("
						+ exportClasses[i].getFileExtension()
						+ ")");
			exportChoice[i].setToolTipText("Select to export in "
			+exportClasses[i].getFormatDescription()+" format.");
			exportChoice[i].setActionCommand("format" + i);
			exportChoice[i].addActionListener(this);
			optionButtons.add(exportChoice[i]);
			options.add(exportChoice[i]);
		}
		contents.add(options, BorderLayout.EAST);

		JPanel p_south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		b_export = new JButton("Export");
		b_export.setToolTipText("Export selected histograms.");
		b_export.setEnabled(false);
		b_export.setActionCommand("export");
		b_export.addActionListener(this);
		p_south.add(b_export);
		JButton b_cancel = new JButton("Cancel");
		b_cancel.setToolTipText("Close this dialog.");
		b_cancel.setActionCommand("cancel");
		b_cancel.addActionListener(this);
		p_south.add(b_cancel);
		contents.add(p_south, BorderLayout.SOUTH);
	}

	private void getClasses() {
		Class temp;

		Set set = RTSI.find("jam.io", ImpExp.class);
		set.remove(ImpExp.class);
		exportClasses = new ImpExp[set.size()];
		int i = 0;
		for (Iterator it = set.iterator(); it.hasNext(); i++) {
			temp = (Class) it.next();
			try {
				exportClasses[i] = (ImpExp) temp.newInstance();
			} catch (InstantiationException e) {
				System.err.println(e);
			} catch (IllegalAccessException e) {
				System.err.println(e);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		System.out.println(command);
		if (command.substring(0, 6).equals("format")) {
			boolean selected = false;
			for (int i = 0; i < exportChoice.length; i++) {
				selected |= exportChoice[i].isSelected();
			}
			b_export.setEnabled(selected);
		} else if (command.equals("addname")) {
			addName();
		} else if (command.equals("cancel")) {
			dispose();
		} else if (command.equals("export")) {
			export();
		} else if (command.equals("removename")) {
			removeItem();
		} else if (command.equals("removeall")) {
			removeAllItems();
		} else if (command.equals("browse")) {
			browseForDir();
		} else if (command.equals("loadlist")) {
			loadList();
		} else if (command.equals("savelist")) {
			saveList();
		} else if (command.equals("addhist")){
			addHist();
		}
	}

	private void addHist(){
		Histogram hist=Histogram.getHistogram(JamStatus.instance().getCurrentHistogramName());
		HashSet goodHists = new HashSet();
		goodHists.add(hist);
		//now combine this with stuff already in list
		ListModel lm = l_hists.getModel();
		for (int i = 0; i < lm.getSize(); i++) {
			goodHists.add(lm.getElementAt(i));
		}
		l_hists.setListData(goodHists.toArray());
	}

	private void addName() {
		String start = t_addName.getText().trim();
		HashSet goodHists = new HashSet();
		for (Iterator it = Histogram.getHistogramList().iterator();
			it.hasNext();
			) {
			Histogram hist = (Histogram) it.next();
			String name = hist.getName();
			if (name.startsWith(start)) {
				goodHists.add(hist);
			}
		}
		//now combine this with stuff already in list
		ListModel lm = l_hists.getModel();
		for (int i = 0; i < lm.getSize(); i++) {
			goodHists.add(lm.getElementAt(i));
		}
		l_hists.setListData(goodHists.toArray());
	}

	private void export() {
		//select the format
		ImpExp out = null;
		for (int i = 0; i < exportChoice.length; i++) {
			if (exportChoice[i].isSelected()) {
				out = exportClasses[i];
			}
		}
		File dir = new File(t_directory.getText().trim());
		if (dir.exists()) {
			if (dir.isDirectory()) {
				//look for any files that might be overwritten
				ListModel lm = l_hists.getModel();
				File[] files = new File[lm.getSize()];
				Histogram[] hist = new Histogram[lm.getSize()];
				boolean already = false;
				for (int i = 0; i < files.length; i++) {
					hist[i] = (Histogram) lm.getElementAt(i);
					files[i] =
						new File(
							dir,
							hist[i].getName().trim() + out.getFileExtension());
					already |= files[i].exists();
				}
				if (already) {
					console.errorOutln(
						"At least one file to export already exists. Delete or try a"
							+ " different directory.");
				} else { //go ahead and write
					console.messageOut(
						"Exporting to " + dir.getPath() + ": ",
						MessageHandler.NEW);
					for (int i = 0; i < files.length; i++) {
						console.messageOut(
							files[i].getName(),
							MessageHandler.CONTINUE);
						if (i < files.length - 1)
							console.messageOut(", ", MessageHandler.CONTINUE);
						try {
							out.saveFile(files[i], hist[i]);
						} catch (ImpExpException e) {
							console.errorOutln(
								"Error while trying to write files: "
									+ e.getMessage());
						}
					}
					console.messageOut(".", MessageHandler.END);
				}
			} else { //not a directory
				console.errorOutln(
					"The specified directory is not really a directory.");
			}
		} else { //directory doesn't exist
			console.errorOutln("The specified directory does not exist.");
		}
	}

	/**
	* remove a item from sort list
	*/
	private void removeItem() {
		Object[] removeList = l_hists.getSelectedValues();
		ListModel lm = l_hists.getModel();
		Vector v = new Vector();
		for (int i = 0; i < lm.getSize(); i++) {
			v.add(lm.getElementAt(i));
		}
		for (int i = 0; i < removeList.length; i++) {
			v.removeElement(removeList[i]);
		}
		l_hists.setListData(v);
	}

	/**
	 * remove all items from sort list 
	 *
	 */
	private void removeAllItems() {
		l_hists.setListData(new Vector());
	}

	/**
	 * add all files in a directory to sort
	 *
	 */
	private void browseForDir() {
		JFileChooser fd =
			new JFileChooser(new File(t_directory.getText().trim()));
		fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int option = fd.showOpenDialog(this);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			t_directory.setText(fd.getSelectedFile().getPath());
		}
	}

	File lastListFile = null;
	/**
	 * Load a list of histograms to export from a file.
	 *
	 */
	private void loadList() {
		Object listItem;

		JFileChooser fd = new JFileChooser(lastListFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showOpenDialog(this);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastListFile = fd.getSelectedFile(); //save current directory
			Vector v = new Vector();
			try {
				BufferedReader br =
					new BufferedReader(new FileReader(lastListFile));
				do {
					listItem = Histogram.getHistogram(br.readLine());
					if (listItem != null) {
						v.addElement(listItem);
					}
				} while (listItem != null);
				br.close();
			} catch (IOException ioe) {
				console.errorOutln(ioe.getMessage());
			}
			l_hists.setListData(v);
		}
	}

	/**
	 * Save list of items to export.
	 */
	private void saveList() {
		JFileChooser fd = new JFileChooser(lastListFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(
			new ExtensionFileFilter(
				new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showSaveDialog(this);
		//save current values
		if (option == JFileChooser.APPROVE_OPTION
			&& fd.getSelectedFile() != null) {
			lastListFile = fd.getSelectedFile(); //save current directory
		}
		try {
			ListModel lm = l_hists.getModel();
			FileWriter saveStream = new FileWriter(fd.getSelectedFile());
			for (int i = 0; i < lm.getSize(); i++) {
				saveStream.write(lm.getElementAt(i) + "\n");
			}
			saveStream.close();
		} catch (IOException ioe) {
			console.errorOutln(ioe.getMessage());
		}
	}

}
