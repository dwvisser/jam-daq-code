package injection;

import jam.JamInitialization;
import jam.commands.CommandManager;
import jam.global.AcquisitionStatus;
import jam.global.Broadcaster;
import jam.global.CommandFinder;
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
		this.bind(Console.class).toInstance(
				jam.ui.Factory.createConsole(JamInitialization.class
						.getPackage().getName()));
		this.bind(CommandFinder.class).toInstance(
				CommandManager.getInstance().getCommandFinder());
		this.bind(JFrame.class).toInstance(new JFrame("Jam"));
		this.bind(Frame.class).toProvider(FrameProvider.class);
		this.bind(ConsoleLog.class).toProvider(ConsoleLogProvider.class);
		this.bind(Broadcaster.class).toInstance(
				Broadcaster.getSingletonInstance());
		this.bind(AcquisitionStatus.class).to(JamStatus.class);
		this.bind(CurrentPlotAccessor.class).to(PlotDisplay.class);
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
		private transient final Console console;

		@Inject
		protected ConsoleLogProvider(final Console console) {
			this.console = console;
		}

		public ConsoleLog get() {
			return this.console.getLog();
		}
	}
}
