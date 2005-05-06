package jam.commands;

import jam.global.CommandListenerException;
import jam.global.ComponentPrintable;
import jam.global.MessageHandler;
import jam.plot.PlotDisplay;

import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Command for Page Setup 
 * @author Ken Swartz
 *
 */
final class Print extends AbstractPrintingCommand {
	
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
			String name =STATUS.getCurrentHistogram().getFullName();
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
}
