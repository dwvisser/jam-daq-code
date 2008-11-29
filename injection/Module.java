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
import jam.ui.Console;
import jam.ui.ConsoleLog;

import java.awt.Frame;

import javax.swing.JFrame;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Guice dependency injection module.
 * 
 * @author Dale Visser
 */
public final class Module extends AbstractModule {

	@Override
	protected void configure() {
		this.bind(JFrame.class).toInstance(new JFrame("Jam"));
		this.bind(Frame.class).toProvider(FrameProvider.class);
		this.bind(ConsoleLog.class).toProvider(ConsoleLogProvider.class);
		this.bind(AcquisitionStatus.class).to(JamStatus.class);
		this.bind(CurrentPlotAccessor.class).to(PlotDisplay.class);
		this.bind(FrontEndCommunication.class)
				.to(FrontEndVMECommunicator.class);
		this.bind(ScalerCommunication.class).to(ScalerVMECommunicator.class);
		this.bind(CommandFinder.class).toProvider(CommandFinderProvider.class);
		this.bind(CommandListener.class).annotatedWith(MapListener.class).to(
				CommandManager.class);
	}

	class FrameProvider implements Provider<Frame> {
		private transient final JFrame frame;

		@Inject
		protected FrameProvider(final JFrame frame) {
			this.frame = frame;
		}

		public Frame get() {
			return this.frame;
		}
	}

	class ConsoleLogProvider implements Provider<ConsoleLog> {
		private transient final ConsoleLog consoleLog;

		@Inject
		protected ConsoleLogProvider(final Console console) {
			this.consoleLog = console.getLog();
		}

		public ConsoleLog get() {
			return this.consoleLog;
		}
	}

	class CommandFinderProvider implements Provider<CommandFinder> {
		private transient final CommandFinder commandFinder;

		@Inject
		protected CommandFinderProvider(final CommandManager commandManager) {
			this.commandFinder = commandManager.getCommandFinder();
		}

		public CommandFinder get() {
			return this.commandFinder;
		}
	}
}
