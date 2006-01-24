package jam.global;

import java.util.List;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Configures the handlers for the "root" logger.
 * @author Dale Visser
 *
 */
public class LoggerConfig {
	private transient final Logger logger;//NOPMD
	
	/**
	 * Default Constructor.
	 *
	 * @param name package name of logger
	 */
	public LoggerConfig(String name){
		super();
		logger = Logger.getLogger(name);
		List<Handler> handlers = Arrays.asList(logger.getHandlers());
		for (Handler handler : handlers) {
			logger.removeHandler(handler);
		}
		logger.addHandler(new ConsoleHandler());
		try {
			logger.addHandler(new FileHandler());
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	/**
	 * Configures logger to use message handler to output messages to user.
	 * 
	 * @param name package name of logger
	 * @param msgHandler for user readable screen output
	 */
	public LoggerConfig(String name, final MessageHandler msgHandler) {
		this(name);
		List<Handler> handlers = Arrays.asList(logger.getHandlers());
		for (Handler handler : handlers) {
			if (handler instanceof ConsoleHandler) {
				logger.removeHandler(handler);
				break;
			}
		}
		logger.addHandler(new Handler(){
			public void close() {
				//do-nothing
			}
			
			public void flush() {
				//do-nothing
			}
			
			public void publish(final LogRecord record){
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
		}
		);
	}

}
