package jam.util; 
import java.awt.*;
/** 
 * Contains useful methods for manipulating AWT classes.
 *
 * @version 0.5
 * @author Dale Visser
 */ 
public class AwtUtilities  {

    /**
     * Helper method for GridBagConstains 
     * 
     * @exception   AWTException	    thrown if unrecoverable error occurs
     */   
    static public void addComponent(Container container, Component component, int gridx, int gridy) 
					throws AWTException{

	addComponent(container, component, gridx, gridy, 1, 1);	
    }

    /**
     * Helper method for GridBagConstains 
     * 
     * @exception   AWTException	    thrown if unrecoverable error occurs
     */   
    static public void addComponent(Container container, Component component, int gridx, int gridy, 
		    int width, int height) throws AWTException {
		    
		LayoutManager layout;
		GridBagLayout gbl;
		
		layout = container.getLayout();
		if (layout instanceof GridBagLayout) {
		    gbl = (GridBagLayout)layout;
		} else {
		    throw new AWTException("addComponent() requires container with GridBagLayout!");
		}
		GridBagConstraints gbc=new GridBagConstraints ();
		gbc.gridx=gridx;
		gbc.gridy=gridy;
		gbc.ipadx=0;		
		gbc.ipady=0;		
		gbc.gridwidth=width;
		gbc.gridheight=height;
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.anchor=GridBagConstraints.EAST;
		
		gbl.setConstraints(component, gbc);
		container.add (component);
    }
}
