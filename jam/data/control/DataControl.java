/*
 */
package jam.data.control;
import java.util.*;
import jam.data.*;
/**
 * A class to do overall control of the jam data bases.
 *
 * @author Ken Swartz
 */

public  abstract class DataControl  {
  GateControl gateControl;
  HistogramControl histogramControl;
  MonitorControl monitorControl;
  ParameterControl parameterControl;
  ScalerControl scalerControl;

  private static Vector controllers= new Vector(5);

  public DataControl() {
    controllers.addElement(this);
  }

  /**
   * setup all data controls
   */
  public  static void setupAll() {
    for (int i=0; i<controllers.size(); i++) {
      ((DataControl)controllers.elementAt(i)).setup();
    }
  }

  /**
   * setup
   */
  public  abstract void setup();
}
