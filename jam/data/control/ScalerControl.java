package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
import javax.swing.*;

/**
 * Reads and displays the scaler values.
 *
 * @version	0.5 April 98
 * @author 	Ken Swartz
 * @since       JDK1.1
 */

public class ScalerControl extends DataControl implements ActionListener, ItemListener, Observer {

    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler messageHandler;

    //scaler display dialog box
    private JDialog ddisp;
    private JPanel [] ps;
    private JLabel [] labelScaler;
    private JTextField [] textScaler;
    private JPanel pb;
    private JCheckBox checkDisabled;
    private JButton bzero;


    //scaler zero dialog box
    private JDialog dzero;
    private JCheckBox checkDisabled2;
    private JButton bzero2;

    private boolean sortScalers;	    //have scalers been added by sort

    /** Creates the dialog box for reading and zeroing scalers.
     * @param frame main window for application that this dialog is attached to
     * @param broadcaster hub of communications for message passin around Jam
     * @param messageHandler object to send text output to user to
     */
    public ScalerControl(Frame frame, Broadcaster broadcaster, MessageHandler messageHandler){
        super();
        this.frame=frame;
        this.broadcaster=broadcaster;
        this.messageHandler=messageHandler;
        sortScalers=false;
        //zeroDisabled=true;
        ddisp=new JDialog(frame,"Scalers",false);// dialog box to display scalers
        Container cddisp = ddisp.getContentPane();
        //ddisp.setResizable(false);
        ddisp.setLocation(20,50);
        cddisp.setLayout(new GridLayout(0,1,5,5));
        pb= new JPanel();// buttons for display dialog
        pb.setLayout(new FlowLayout(FlowLayout.CENTER,10,5));
        JButton bupdate = new JButton ("Read");
        bupdate.setActionCommand("scalerread");
        bupdate.addActionListener(this);
        bzero =  new JButton("Zero");
        bzero.setActionCommand("scalzero");
        bzero.addActionListener(this);
        bzero.setEnabled(false);
        checkDisabled =new JCheckBox("Disable Zero", true );
        checkDisabled.addItemListener(this);
        pb.add(bupdate);
        pb.add(bzero);
        pb.add(checkDisabled);
        ddisp.addWindowListener(
        new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                ddisp.dispose();
            }
        }
        );
        dzero=new JDialog(frame, "Scalers Zero",true);// dialog to zero scalers
        Container dzc=dzero.getContentPane();
        dzero.setResizable(false);
        dzero.setLocation(20,50);
        //dzero.setSize(250, 100);
        dzc.setLayout(new FlowLayout(FlowLayout.CENTER,10,20));
        bzero2 =  new JButton("Zero ");
        bzero2.setActionCommand("scalzero2");
        bzero2.addActionListener(this);
        bzero2.setEnabled(false);
        checkDisabled2 =new JCheckBox("Disable Zero", true );
        checkDisabled2.addItemListener(this);
        dzc.add(bzero2);
        dzc.add(checkDisabled2);
        dzero.addWindowListener(
        new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dzero.dispose();
            }
        }
        );
        dzero.pack();
        setup();
    }

    /** Action either read scalers or zero scalers.
     * @param ae event from dialog box
     */
    public void actionPerformed(ActionEvent ae) {
        String command=ae.getActionCommand();
        try {
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
                if (JamStatus.isOnLine()){
                    zero();
                } else {
                    throw new DataException("Can't Zero Scalers not Online");
                }
            }  else {
                System.err.println("Error Unregonized command [Scaler Control]");
            }
        } catch (DataException je){
            messageHandler.errorOutln(je.getMessage());
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".actionPerformed(): "+ge);
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
     * Setup the display dialog box.  Needs to be called if the list of <code>Scaler</code> objects
     * changes, such as after opening a file, or initializing a sort routine.
     */
    public void setup(){
        sortScalers=true;
        int numberScalers=Scaler.getScalerList().size();
        Container cddisp=ddisp.getContentPane();
        //ddisp.setResizable(true);
        cddisp.removeAll();
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
                ps[count]= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));//right justified, hgap, vgap
                //ps[count].setLayout(
                labelScaler[count]=new JLabel (currentScaler.getName(),JLabel.RIGHT);
                textScaler[count] =new JTextField("  ");
                textScaler[count].setColumns(12);
                textScaler[count].setEditable(false);
                textScaler[count].setBackground(Color.white);
                textScaler[count].setText( String.valueOf(currentScaler.getValue()) );
                ps[count].add(labelScaler[count]);
                ps[count].add(textScaler[count]);
                cddisp.add(ps[count]);
                count++;
            }
        }
        cddisp.add(pb);	//buttons panel
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
    public void read() throws GlobalException {
        if (JamStatus.isOnLine()){
            broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
        } else {
            displayScalers();
        }
    }

    /**
     * zero scalers, call broadcast which will sent it to
     * the class that will zero the camac crate scalers.
     */
    public void zero() throws GlobalException{
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
