package jam.plot;
/**
 * Layout constants for plotgraphics, has default size for some things.
 * 
 * @version 0.5
 * @author Ken Swartz 
 */
interface PlotGraphicsLayout {

    //border outside of plot
    final int BORDER_TOP=40;
	final int BORDER_LEFT=60;
	final int BORDER_BOTTOM=40;
	final int BORDER_RIGHT=60;

    //tickmark stuff
	final int TICK_SIZE=10;
	final int TICK_MINOR=5;
	final int TICK_MAJOR=10;
    
    //title stuff
	final int TITLE_OFFSET=10;
	final int TITLE_OFFSET_TOP=10;
	final int TITLE_OFFSET_BOTTOM=25;
	final int TITLE_OFFSET_DATE=25;
    
    //tickmarks
	final int LABEL_OFFSET=5;
	final int LABEL_OFFSET_TOP=5;
	final int LABEL_OFFSET_BOTTOM=3;
	final int LABEL_OFFSET_LEFT=3;
    
    //axis labels
	final int AXIS_LABEL_OFFSET_TOP=20;    
	final int AXIS_LABEL_OFFSET_BOTTOM=20;
	final int AXIS_LABEL_OFFSET_LEFT=35;    
	final int AXIS_LABEL_OFFSET_RIGHT=20;        
    
    //stuff for channel marker
	final int MARK_MIN_LENGTH=20;    
	final int MARK_OFFSET=3;
    //fonts
	final float SCREEN_FONT_SIZE=12;
	final float TITLE_SCREEN_SIZE=SCREEN_FONT_SIZE+2;

    //stuff for two d color scale 
	final int COLOR_SCALE_OFFSET=10;	    // distance from right side of plot
	final int COLOR_SCALE_LABEL_OFFSET=5;
	final int COLOR_SCALE_SIZE=15;	    // size of a color swatch

    // stuff for printing, margins, font
	final double MARGIN_TOP=0.5;
	final double MARGIN_BOTTOM=0.5;
	final double MARGIN_LEFT=0.5;
	final double MARGIN_RIGHT=0.5;

	final int PRINT_FONT_SIZE=12;
	final int TILE_PRINT_SIZE=PRINT_FONT_SIZE+2;
    
}
