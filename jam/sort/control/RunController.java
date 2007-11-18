package jam.sort.control;

import jam.global.JamException;
import jam.sort.SortException;

interface RunController {
	void beginRun() throws JamException, SortException;
	void endRun();
}
