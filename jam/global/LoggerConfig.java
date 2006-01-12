package jam.global;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerConfig {
	private static final Logger LOGGER = Logger.getLogger("");

	
	public LoggerConfig(){
		super();
		LOGGER.addHandler(new ConsoleHandler());
		try {
			LOGGER.addHandler(new FileHandler());
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}

}
