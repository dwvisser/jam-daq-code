package jam.plot;

import java.awt.*;

/**
 * Represents where on a plot is selected.
 *
 * @author Dale Visser
 */
public final class PlotSelection {

  PlotSelection() {
    super();
  }

  /** selection start point in plot coordinates */
  final transient Bin start = Bin.create(); // NOPMD

  /** Repaint clip to use when repainting during area selection. */
  final transient Rectangle areaClip = new Rectangle(); // NOPMD
}
