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

  private static List controllers= new Vector(5);

  public DataControl() {
    controllers.add(this);
  }

  /**
   * setup all data controls
   */
  public  static void setupAll() {
    for (int i=0; i<controllers.size(); i++) {
      ((DataControl)controllers.get(i)).setup();
    }
  }

  /**
   * setup
   */
  public  abstract void setup();
}
