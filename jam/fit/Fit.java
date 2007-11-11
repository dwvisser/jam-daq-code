package jam.fit;

import jam.global.MessageHandler;

import java.util.List;

interface Fit {
	Parameter getParameter(String which);
	List<Parameter> getParameters();
	MessageHandler getTextInfo();
}
