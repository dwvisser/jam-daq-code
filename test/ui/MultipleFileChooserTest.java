package test.ui;

import static org.junit.Assert.assertEquals;
import jam.ui.MultipleFileChooser;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import org.junit.Test;

/**
 * Writing regression tests of some behavior of MultipleFileChooser, for bug
 * fixing purposes.
 * 
 * @author Dale
 * 
 */
public final class MultipleFileChooserTest {
	/**
	 * Test the default behavior of MultipleFileChooser is to be in the user's
	 * home folder.
	 * 
	 * @throws IOException
	 *             if there is a file access problem
	 */
	@Test
	public void testInitialFolderDefault() throws IOException {
		final File userDefault = (new JFileChooser()).getCurrentDirectory()
				.getCanonicalFile();
		MultipleFileChooser mfc = new MultipleFileChooser(null);
		final File currentFolder = mfc.getCurrentFolder().getCanonicalFile();
		assertEquals(currentFolder, userDefault);
	}

	/**
	 * Test the behavior of MultipleFileChooser if a start folder is specified.
	 * 
	 * @throws IOException
	 *             if file access has a problem
	 */
	@Test
	public void testInitialFolderSpecified() throws IOException {
		final File specified = File.createTempFile("jam", null).getParentFile()
				.getCanonicalFile();
		MultipleFileChooser mfc = new MultipleFileChooser(null, specified);
		final File currentFolder = mfc.getCurrentFolder().getCanonicalFile();
		assertEquals(currentFolder, specified);
	}
}
