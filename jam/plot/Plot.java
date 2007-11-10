package jam.plot;

import jam.data.Histogram;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;

interface Plot extends CountsContainer {
	Histogram getHistogram();

	boolean isPrinting();

	void mouseMoved(MouseEvent mouseEvent);

	void paintFit(Graphics graphics);

	void paintGate(Graphics graphics);

	void paintHeader(Graphics graphics);

	void paintHistogram(Graphics graphics);

	void paintMarkArea(Graphics graphics);

	void paintMarkedChannels(Graphics graphics);

	void paintMouseMoved(Graphics graphics);

	void paintOverlay(Graphics graphics);

	void paintSetGatePoints(Graphics graphics);

	void setView(PageFormat format);

	void setViewSize(Dimension size);

	void update(Graphics graph);
}
