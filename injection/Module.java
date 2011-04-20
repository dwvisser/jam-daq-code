package injection;

import jam.comm.FrontEndCommunication;
import jam.comm.FrontEndVMECommunicator;
import jam.comm.ScalerCommunication;
import jam.comm.ScalerVMECommunicator;
import jam.commands.CommandManager;
import jam.global.AcquisitionStatus;
import jam.global.CommandFinder;
import jam.global.CommandListener;
import jam.global.JamStatus;
import jam.plot.CurrentPlotAccessor;
import jam.plot.PlotDisplay;
import jam.plot.PlotSelectListener;

import java.awt.Frame;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;

import com.google.inject.AbstractModule;

/**
 * Guice dependency injection module.
 * @author Dale Visser
 */
public final class Module extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(JFrame.class).toInstance(new JFrame("Jam"));
        this.bind(ExecutorService.class).toInstance(
                Executors.newCachedThreadPool());
        this.bind(Frame.class).to(JFrame.class);
        this.bind(AcquisitionStatus.class).to(JamStatus.class);
        this.bind(CurrentPlotAccessor.class).to(PlotDisplay.class);
        this.bind(PlotSelectListener.class).to(PlotDisplay.class);
        this.bind(FrontEndCommunication.class).to(
                FrontEndVMECommunicator.class);
        this.bind(ScalerCommunication.class).to(ScalerVMECommunicator.class);
        this.bind(CommandFinder.class).toProvider(CommandFinderProvider.class);
        this.bind(CommandListener.class).annotatedWith(MapListener.class)
                .to(CommandManager.class);
    }
}
