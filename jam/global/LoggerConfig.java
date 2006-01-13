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

public class LoggerConfig {
	private static final Logger LOGGER = Logger.getLogger("");
	
	public LoggerConfig(){
		super();
		List<Handler> handlers = Arrays.asList(LOGGER.getHandlers());
		for (Handler handler : handlers) {
			LOGGER.removeHandler(handler);
		}
		LOGGER.addHandler(new ConsoleHandler());
		try {
			LOGGER.addHandler(new FileHandler());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public LoggerConfig(final MessageHandler msgHandler) {
		this();
		List<Handler> handlers = Arrays.asList(LOGGER.getHandlers());
		for (Handler handler : handlers) {
			if (handler instanceof ConsoleHandler) {
				LOGGER.removeHandler(handler);
				break;
			}
		}
		LOGGER.addHandler(new Handler(){
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
