/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;
import jam.data.control.*;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
public class ShowMonitorConfig extends AbstractShowDataControlCmd {

	protected void initCommand(){
		putValue(NAME,"Configure Monitors\u2026");
		dataControl=MonitorControl.getSingletonInstance(msghdlr);
	}
}
