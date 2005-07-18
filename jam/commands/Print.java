package jam.commands;

import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.ComponentPrintable;
import jam.global.MessageHandler;
import jam.plot.PlotDisplay;

import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Command for Page Setup 
 * @author Ken Swartz
 *
 */
final class Print extends AbstractPrintingCommand implements Observer {
	
	private boolean firstTime=true;
	private PlotDisplay display=null;
	
	Print(){
		super();
		putValue(NAME,"Print\u2026");
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_P, 
		CTRL_MASK));
	    final Icon iPrint = loadToolbarIcon("jam/ui/Print.png");
	    putValue(Action.SMALL_ICON, iPrint);
		putValue(Action.SHORT_DESCRIPTION, "Print histogram");	    
		
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		if (firstTime){
			msghdlr.warningOutln("On some systems, it will be necessary to first "+
			"use 'Page Setup\u2026' for your hardcopy to have correct size and margins.");
			display=STATUS.getDisplay();
			firstTime=false;
		}
		final PrinterJob pj = PrinterJob.getPrinterJob();
		final ComponentPrintable cp = display.getComponentPrintable();
		pj.setPrintable(cp, mPageFormat);
		if (pj.printDialog()) {
			String name =((Histogram)STATUS.getCurrentHistogram()).getFullName();
			msghdlr.messageOut("Preparing to send histogram '" + 
					name+"' to printer\u2026",
			MessageHandler.NEW);
			try {
				display.setRenderForPrinting(true, mPageFormat);
				pj.print();
				msghdlr.messageOut("sent.", MessageHandler.END);
				display.setRenderForPrinting(false, null);
			} catch (PrinterException e) {
				final StringBuffer mess=new StringBuffer(getClass().getName());
				final String colon=": ";
				mess.append(colon);
				mess.append(e.getMessage());
				msghdlr.errorOutln(mess.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) throws CommandListenerException {
		execute(null);
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if ( (command==BroadcastEvent.Command.GROUP_SELECT) || 
			 (command==BroadcastEvent.Command.ROOT_SELECT) ) {
			setEnabled(false);			
		} else if ( (command==BroadcastEvent.Command.HISTOGRAM_SELECT) || 
				    (command==BroadcastEvent.Command.GATE_SELECT) ) {
			setEnabled(true);
		} 
	}			
	
}
