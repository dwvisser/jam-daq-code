/*
 * Created on June 4, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import com.google.inject.Inject;

import jam.io.BatchExport;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 4, 2004
 */
@SuppressWarnings("serial")
final class ShowBatchExport extends AbstractShowDialog {

	@Inject
	ShowBatchExport(final BatchExport batchExport) {
		super("Batch Export\u2026");
		dialog = batchExport;
	}
}
