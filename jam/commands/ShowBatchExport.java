/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowBatchExport extends AbstractShowDialog {
	
	public void initCommand(){
		putValue(NAME, "Batch Export\u2026");
		dialog=new jam.io.BatchExport();
	}
}
