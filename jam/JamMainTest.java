/*
 * Created on Nov 18, 2004
 */
package jam;

import jam.util.ThreadCheckingRepaintManager;

import javax.swing.RepaintManager;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public class JamMainTest extends TestCase {
	
	public static void main(String [] args){
		RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
		JamMain.main(args);
	}

}
