package jam.sort.control;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

final class PathBrowseButton extends JButton {

	private transient File path;
	private transient final JTextComponent text;
	private transient final JFrame frame;

	PathBrowseButton(final File file, final JTextComponent textComponent,
			final JFrame frame) {
		super("Browse...");
		assert (file != null);
		assert (textComponent != null);
		assert (frame != null);
		this.frame = frame;
		path = file;
		text = textComponent;
		text.setText(path.getPath());
		addActionListener(actionEvent -> {
            browsePath();
            text.setText(path.getPath());
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
		final int option = fileChooser.showOpenDialog(this.frame);
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
	public File getPath() {
		return path;
	}

}
