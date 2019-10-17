package jam.global;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.*;

/**
 * Configures the handlers for the "root" logger.
 * 
 * @author Dale Visser
 * 
 */
public class LoggerConfig {
	private transient final Logger logger;// NOPMD

	/**
	 * Default Constructor.
	 * 
	 * @param name
	 *            package name of logger
	 */
	public LoggerConfig(final String name) {
		super();
		logger = Logger.getLogger(name);
		logger.setLevel(Level.FINEST);
		final Collection<Handler> handlers = Arrays
				.asList(logger.getHandlers());
		for (Handler handler : handlers) {
			logger.removeHandler(handler);
		}
		logger.addHandler(new ConsoleHandler());
		try {
			logger.addHandler(new FileHandler("%h/jam%u.log"));
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	/**
	 * Configures logger to use message handler to output messages to user.
	 * 
	 * @param name
	 *            package name of logger
	 * @param msgHandler
	 *            for user readable screen output
	 */
	public LoggerConfig(final String name, final MessageHandler msgHandler) {
		this(name);
		final Collection<Handler> handlers = Arrays
				.asList(logger.getHandlers());
		for (Handler handler : handlers) {
			if (handler instanceof ConsoleHandler) {
				logger.removeHandler(handler);
				break;
			}
		}
		logger.addHandler(new Handler() {
			@Override
			public void close() {
				// do-nothing
			}

			@Override
			public void flush() {
				// do-nothing
			}

			@Override
			public void publish(final LogRecord record) {
				final int level = record.getLevel().intValue();
				final String message = record.getMessage();
				if (level >= Level.SEVERE.intValue()) {
					msgHandler.errorOutln(message);
				} else if (level >= Level.WARNING.intValue()) {
					msgHandler.warningOutln(message);
				} else if (level >= Level.INFO.intValue()) {
					msgHandler.messageOutln(message);
				}
			}
		});
	}

}
