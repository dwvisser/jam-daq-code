package jam.plot;
/**
 * Layout constants for plotgraphics, has default size for some things.
 * 
 * @version 0.5
 * @author Ken Swartz 
 */
interface PlotGraphicsLayout {

    //border outside of plot
    static final int BORDER_TOP=40;
    static final int BORDER_LEFT=60;
    static final int BORDER_BOTTOM=40;
    static final int BORDER_RIGHT=60;

    //tickmark stuff
    static final int TICK_SIZE=10;
    static final int TICK_MINOR=5;
    static final int TICK_MAJOR=10;
    
    //title stuff
    static final int TITLE_OFFSET=10;
    static final int TITLE_OFFSET_TOP=10;
    static final int TITLE_OFFSET_BOTTOM=25;
    static final int TITLE_OFFSET_DATE=25;
    
    //tickmarks
    static final int LABEL_OFFSET=5;
    static final int LABEL_OFFSET_TOP=5;
    static final int LABEL_OFFSET_BOTTOM=3;
    static final int LABEL_OFFSET_LEFT=3;
    
    //axis labels
    static final int AXIS_LABEL_OFFSET_TOP=20;    
    static final int AXIS_LABEL_OFFSET_BOTTOM=20;
    static final int AXIS_LABEL_OFFSET_LEFT=50;    
    static final int AXIS_LABEL_OFFSET_RIGHT=20;        
    
    //stuff for channel marker
    static final int MARK_MIN_LENGTH=20;    
    static final int MARK_OFFSET=3;
    //fonts
    static final int SCREEN_FONT_SIZE=12;

    //stuff for two d color scale 
    static final int COLOR_SCALE_OFFSET=10;	    // distance from right side of plot
    static final int COLOR_SCALE_LABEL_OFFSET=5;
    static final int COLOR_SCALE_SIZE=15;	    // size of a color swatch

    // stuff for printing, margins, font
    static final double MARGIN_TOP=0.5;
    static final double MARGIN_BOTTOM=0.5;
    static final double MARGIN_LEFT=0.5;
    static final double MARGIN_RIGHT=0.5;

    static final int PRINT_FONT_SIZE=12;
    
}
