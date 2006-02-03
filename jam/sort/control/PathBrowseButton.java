package jam.sort.control;

import jam.global.JamStatus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.text.JTextComponent;

final class PathBrowseButton extends JButton {
	
	private transient File path;
	private transient final JTextComponent text;
	private static final JamStatus STATUS=JamStatus.getSingletonInstance();

	PathBrowseButton(File file, JTextComponent textComponent){
		super("Browse...");
		assert(file != null);
		assert(textComponent != null);
		path=file;
		text=textComponent;
		text.setText(path.getPath());
		addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				browsePath();
				text.setText(path.getPath());
			}
		});
	}

	/*
	 * non-javadoc: Is the Browse for the Path Name where the events file will
	 * be saved.
	 * 
	 * @author Ken Swartz @author Dale Visser
	 */
	private void browsePath() {
		final JFileChooser fileChooser = new JFileChooser(path);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		final int option = fileChooser.showOpenDialog(STATUS.getFrame());
		final boolean approval = option == JFileChooser.APPROVE_OPTION;
		final boolean selected = fileChooser.getSelectedFile() != null;
		if (approval && selected) {
			path = fileChooser.getSelectedFile();
		}
	}
	
	/**
	 * 
	 * @return most recently selected path
	 */
	public File getPath(){
		return path;
	}

}
