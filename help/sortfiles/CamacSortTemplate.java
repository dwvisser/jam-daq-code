/**
 * Template sort routine for Jam
 *
 * @author Ken Swartz
 * @version 1 June 99
 */
 package help.sortfiles;
 import jam.data.Gate;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.sort.SortRoutine;

public class CamacSortTemplate extends SortRoutine {

        /** variables declarations */
        Histogram myHist;               //declare histogram myHist
        Histogram myHistGated;          //declare histogram myHistGated

        Gate myGate;                    //declare gate myGate;  
        Scaler myScal;                  //declare scaler myScal;                         
                        
        int idEvnt;                     //id number for event word from cnaf
        int idScal;                     //id number for scaler from cnaf
     
        /** The initialization method 
         *  code to define camac commands, variables and classes
         */
        public void initialize() throws Exception {
             /* uncomment to setup camac commands here               
             cnafCommands.init(c,n,a,f);//initialize crate cnafs
             idEvnt=cnafCommands.eventRead(c,n,a,f);//event cnafs
		     cnafCommands.eventCommand(c,n,a,f);//non-read command to issue
             idScal=cnafCommands.scaler(c,n,a,f);//scaler read cnafs
             cnafCommands.clear(c,n,a,f);//scaler clear cnaf
             */  
             
             /* comment out setEventSize() if you actually put in the 
              * CAMAC stuff
              */
             setEventSize(2);
                          
             /* initialize histograms, gates, and scalers */
             myHist = new Histogram("detector1", HIST_1D, 1024,"my detector");
             myHistGated = new Histogram("detecGated", HIST_1D, 1024,"my detector gated");
                                
             myGate=new Gate("detector1", myHist);
                                
             myScal=new Scaler("scaler1", idScal);
    }
    /** 
     * The sort method called for each event with
     * eventData having the event data
     */
    public void sort(int [] eventData) throws Exception {  
        myHist.inc(eventData[idEvnt]);          //increment myHist with word idHist;        
        if (myGate.inGate(eventData[idEvnt])) {      //if event word is in myGate
            myHistGated.inc(eventData[idEvnt]);   //increment myHistGate                 
        }
    }
}
