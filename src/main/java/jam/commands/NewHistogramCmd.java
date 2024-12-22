package jam.commands;

import injection.GuiceInjector;
import jam.data.Factory;
import jam.data.Group;
import jam.data.HistogramType;
import jam.global.JamStatus;

/**
 * Command to create a histogram
 * @author Ken Swartz
 */

final class NewHistogramCmd extends AbstractCommand {

    NewHistogramCmd() {
        super();
    }

    /**
     * Execute the command
     */
    @Override
    protected void execute(final Object[] cmdParams) {
        final String name = (String) cmdParams[0];
        final String title = (String) cmdParams[1];
        final int type = (Integer) cmdParams[2];
        final HistogramType hType = type == 1 ? HistogramType.ONE_D_DOUBLE
                : HistogramType.TWO_D_DOUBLE;
        final int sizeX = (Integer) cmdParams[3];
        final int sizeY = (Integer) cmdParams[4];
        final Group currentGroup = (Group) GuiceInjector.getObjectInstance(
                JamStatus.class).getCurrentGroup();
        Factory.createHistogram(currentGroup,
                hType.getSampleArray(sizeX, sizeY), name, title);
    }

    /**
     * Execute the command
     */
    @Override
    protected void executeParse(final String[] cmdParams) {
        final String name = cmdParams[0];
        final String title = cmdParams[1];
        final int type = Integer.parseInt(cmdParams[2]);
        final HistogramType hType = type == 1 ? HistogramType.ONE_D_DOUBLE
                : HistogramType.TWO_D_DOUBLE;
        final int sizeX = Integer.parseInt(cmdParams[3]);
        final int sizeY = Integer.parseInt(cmdParams[4]);
        final Group workingGroup = Factory.createGroup("Working",
                Group.Type.FILE);
        Factory.createHistogram(workingGroup,
                hType.getSampleArray(sizeX, sizeY), name, title);
    }

}