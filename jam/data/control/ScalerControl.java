package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;


/**
 * Reads and displays the scaler values.
 *
 * @version	0.5 April 98
 * @author 	Ken Swartz
 * @since       JDK1.1
 */

public final class ScalerControl extends DataControl implements ActionListener, ItemListener, Observer {

    private final Frame frame;
    private final Broadcaster broadcaster=Broadcaster.getSingletonInstance();
    private final MessageHandler messageHandler;

    //scaler display dialog box
    private final JDialog ddisp;
    private JPanel [] ps;
    private JLabel [] labelScaler;
    private JTextField [] textScaler;
    private final JPanel plower;
    private final JPanel pScalers;
    private final JCheckBox checkDisabled;
    private final JButton bzero;


    //scaler zero dialog box
    private final JDialog dzero;
    private final JCheckBox checkDisabled2;
    private final JButton bzero2;

    private boolean sortScalers;	    //have scalers been added by sort
	private final JamStatus status= JamStatus.instance();

    /** Creates the dialog box for reading and zeroing scalers.
     * @param frame main window for application that this dialog is attached to
     * @param messageHandler object to send text output to user to
     */
    public ScalerControl(Frame frame, MessageHandler messageHandler){
        super();
        this.frame=frame;
        this.messageHandler=messageHandler;
        sortScalers=false;
        //zeroDisabled=true;

        // dialog box to display scalers
        ddisp=new JDialog(frame,"Scalers",false);
        Container cddisp = ddisp.getContentPane();
        ddisp.setLocation(20,50);
        cddisp.setLayout(new BorderLayout());


		pScalers = new JPanel(new GridLayout(0,1,10,5));
        Border borderScalers = new EmptyBorder(10,10,10,10);
        pScalers.setBorder(borderScalers);

		cddisp.add(pScalers, BorderLayout.CENTER);

        plower = new JPanel(new FlowLayout(FlowLayout.CENTER,10,10));
        JPanel pb= new JPanel();// buttons for display dialog
        pb.setLayout(new GridLayout(1,0,10,10));
		cddisp.add(plower, BorderLayout.SOUTH);

        JButton bupdate = new JButton ("Read");
        bupdate.setActionCommand("scalerread");
        bupdate.addActionListener(this);
        pb.add(bupdate);
        bzero =  new JButton("Zero");
        bzero.setActionCommand("scalzero");
        bzero.addActionListener(this);
        bzero.setEnabled(false);
        pb.add(bzero);
		plower.add(pb);

        checkDisabled =new JCheckBox("Disable Zero", true );
        checkDisabled.addItemListener(this);
        plower.add(checkDisabled);

        ddisp.addWindowListener(
        new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                ddisp.dispose();
            }
        }
        );

        // dialog to zero scalers
        dzero=new JDialog(frame, "Zero Scalers",true);
        Container dzc=dzero.getContentPane();
        dzero.setResizable(false);
        dzero.setLocation(20,50);
        //dzc.setLayout(new GridLayout(1,0));
        final JPanel pZero = new JPanel(new GridLayout(1,0,20,20));
        Border border = new EmptyBorder(20,20,20,20);
        pZero.setBorder(border);

        bzero2 =  new JButton("Zero");
        bzero2.setActionCommand("scalzero2");
        bzero2.addActionListener(this);
        bzero2.setEnabled(false);
        pZero.add(bzero2);
        checkDisabled2 =new JCheckBox("Disable Zero", true );
        checkDisabled2.addItemListener(this);
        pZero.add(checkDisabled2);
        dzero.addWindowListener(
        new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dzero.dispose();
            }
        }
        );
        dzc.add(pZero);
        dzero.pack();
        setup();
    }

    /** Action either read scalers or zero scalers.
     * @param ae event from dialog box
     */
    public void actionPerformed(ActionEvent ae) {
        String command=ae.getActionCommand();
            if (command=="scalerread") {
                read();
            } else if ((command=="scalzero")||(command=="scalzero2")) {
                if (command=="scalzero") {
                    checkDisabled.setSelected(true);
                    bzero.setEnabled(false);
                } else {
                    checkDisabled2.setSelected(true);
                    bzero2.setEnabled(false);
                    dzero.dispose();
                }
                if (status.isOnLine()){
                    zero();
                } else {
                    throw new IllegalStateException("Can't Zero Scalers when not in Online mode.");
                }
            }  else {
                throw new UnsupportedOperationException("Error Unregonized command: "+command);
            }
    }

    /** Handles events from checkboxes.
     *
     * @param ie a changed checkbox state
     */
    public void itemStateChanged(ItemEvent ie){
        if (ie.getItemSelectable()==checkDisabled) {
            if(checkDisabled.isSelected()){
                bzero.setEnabled(false);
            } else {
                bzero.setEnabled(true);
            }
        } else if (ie.getItemSelectable()==checkDisabled2) {
            if(checkDisabled2.isSelected()){
                bzero2.setEnabled(false);
            } else {
                bzero2.setEnabled(true);
            }
        }
    }

    /**
     * Show the Display dialog box
     *
     */
    public void showDisplay(){
        displayScalers();
        ddisp.show();
    }

    /**
     * Show the zeroing dialog box
     *
     */
    public void showZero(){
        dzero.show();
    }
	/**
	 * Default show dialog, shows the display dialog
	 */
	public void show() {
		displayScalers();
		ddisp.show();
		
	}

    /**
     * Setup the display dialog box.  Needs to be called if the list of <code>Scaler</code> objects
     * changes, such as after opening a file, or initializing a sort routine.
     */
    public void setup(){
        sortScalers=true;
        int numberScalers=Scaler.getScalerList().size();
        //Container cddisp=ddisp.getContentPane();
        //ddisp.setResizable(true);

        //cddisp.removeAll();
        pScalers.removeAll();
        //ddisp.setSize(250, 100+35*numberScalers);
        if (numberScalers!=0){// we have some elements in the scaler list
            //gui widgets for each scaler
            ps = new JPanel[numberScalers];
            labelScaler=new JLabel[numberScalers];
            textScaler =new JTextField[numberScalers];
            Iterator enumScaler=Scaler.getScalerList().iterator();
            int count=0;
            while(enumScaler.hasNext()) {
                Scaler currentScaler=(Scaler)enumScaler.next();
                //right justified, hgap, vgap
                ps[count]= new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
                //ps[count].setLayout(
                labelScaler[count]=new JLabel (currentScaler.getName().trim(),JLabel.RIGHT);
                textScaler[count] =new JTextField("  ");
                textScaler[count].setColumns(12);
                textScaler[count].setEditable(false);
                textScaler[count].setText( String.valueOf(currentScaler.getValue()) );
                ps[count].add(labelScaler[count]);
                ps[count].add(textScaler[count]);
                pScalers.add(ps[count]);
                count++;
            }
        }
        ddisp.pack();
        displayScalers();
    }

    /** Sets the text fields in the scaler display dialog box.
     * @param scalValue array of scaler values, which <b>must</b> map to the array of text fields in the dialog box
     */
    public void setScalers(int []scalValue){
        for (int i=0;i<scalValue.length;i++ ) {
            textScaler[i].setText(String.valueOf(scalValue[i]));
        }
    }

    /**
     * Read the scaler values send out command to read scalers
     * which sould be recieved by VMECommunication
     * VME should then send a command to CAMAC to read the
     * scalers, when VME recieves back the scaler values it
     * calls Distribute event which will call our update method.
     */
    public void read() {
        if (status.isOnLine()){
            broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
        } else {
            displayScalers();
        }
    }

    /**
     * zero scalers, call broadcast which will sent it to
     * the class that will zero the camac crate scalers.
     */
    public void zero() {
        broadcaster.broadcast(BroadcastEvent.SCALERS_CLEAR);
        broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
    }

    /** Implementation of Observable interface.
     * @param observable not sure
     * @param o not sure
     */
    public void update(Observable observable, Object o){
        BroadcastEvent be=(BroadcastEvent)o;
        if(be.getCommand()==BroadcastEvent.SCALERS_UPDATE){
            displayScalers();
        }
    }

    /**
     * Get the values from the Scalers and
     * display them
     */
    public void displayScalers(){
        // we have some elements in the scaler list
        if (Scaler.getScalerList().size() != 0){
            Iterator enumScaler=Scaler.getScalerList().iterator();
            sortScalers=true;
            int count=0;
            while(enumScaler.hasNext()) {
                Scaler currentScaler=(Scaler)enumScaler.next();
                textScaler[count].setText(String.valueOf(
                currentScaler.getValue()));
                count++;
            }
        }
    }
}
