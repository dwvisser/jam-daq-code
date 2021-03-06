/*---------------------------------------------------------------
 * Nuclear Simulation Java Class Libraries
 * Copyright (C) 2003 Yale University
 * 
 * Original Developer
 *     Dale Visser (dwvisser@users.sourceforge.net)
 * 
 * OSI Certified Open Source Software
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the University of Illinois/NCSA 
 * Open Source License.
 * 
 * This program is distributed in the hope that it will be 
 * useful, but without any warranty; without even the implied 
 * warranty of merchantability or fitness for a particular 
 * purpose. See the University of Illinois/NCSA Open Source 
 * License for more details.
 * 
 * You should have received a copy of the University of 
 * Illinois/NCSA Open Source License along with this program; if 
 * not, see http://www.opensource.org/
 *-------------------------------------------------------------*/
package jam.util;

import com.google.inject.Inject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Scans YaleCAEN event files for scaler blocks.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 */
public class YaleCAENgetScalers {
    private static final Logger LOGGER = Logger
            .getLogger(YaleCAENgetScalers.class.getPackage().getName());

    private transient String fileName;

    private final transient Frame frame;

    private transient ProgressMonitor pBstatus;

    private transient String strScalerText;

    private final transient ExecutorService executor;

    /**
     * Constructs an object that can scan YaleCAEN event files for scaler
     * blocks.
     * @param frame
     *            application frame
     * @param executor
     *            provides threads for tasks
     */
    @Inject
    public YaleCAENgetScalers(final Frame frame, final ExecutorService executor) {
        super();
        this.frame = frame;
        this.executor = executor;
    }

    private void display() {
        new TextDisplayDialog(frame, fileName, strScalerText);
    }

    /**
     * Takes an event file, searches for scaler blocks in it and creates
     * tab-delimited text listing each scaler block on one row of text.
     * @param events
     *            the file to search
     * @return whether we were successful
     */
    private boolean doIt(final File events, final StringBuilder strError) {
        final int mega = 1024 * 1024;
        final long fileLength = events.length();
        final int lengthMB = (int) (fileLength / mega);
        pBstatus = new ProgressMonitor(frame, "Scanning " + events.getName()
                + " for scaler blocks", "Initializing", 0, lengthMB);
        boolean rtnState = true;
        final StringBuffer strBuff = new StringBuffer();
        final int SCALER_HEADER = 0x01cccccc;
        DataInputStream dis = null;
        strError.delete(0, strError.length());
        int counter = 0;
        int megaCounter = 0;
        try {
            dis = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(events)));
            counter += dis.skipBytes(256);
            while (true) {
                final int readVal = dis.readInt();
                counter += 4;
                if (readVal == SCALER_HEADER) {
                    final int numScalers = dis.readInt();
                    counter += 4;
                    counter = appendScalerValues(strBuff, dis, counter,
                            numScalers);
                    strBuff.append('\n');
                }
                if (counter >= mega) {
                    counter -= mega;
                    megaCounter++;
                    updateProgressBar(megaCounter + " of " + lengthMB
                            + " MB read.", megaCounter);
                }
            }
            // End of file reached
        } catch (EOFException eofe) {
            // Bury close exception
            try {
                dis.close();
                updateProgressBar("Done.", lengthMB);
                rtnState = true;
            } catch (Exception e) {
                strError.append(e.getMessage());
                rtnState = false;
            }
        } catch (IOException ioe) {
            strError.append("Reading file: ").append(ioe.getMessage());
            rtnState = false;
        }
        fileName = events.getName();
        strScalerText = strBuff.toString();
        return rtnState;
    }

    private int appendScalerValues(final StringBuffer strBuff,
            final DataInputStream dis, final int counter, final int numScalers)
            throws IOException {
        int rval = counter;
        for (int i = 1; i <= numScalers; i++) {
            strBuff.append(dis.readInt());
            rval += 4;
            if (i < numScalers) {
                strBuff.append('\t');
            }
        }
        return rval;
    }

    /**
     * Scans the given event file for scaler blocks.
     * @param events
     *            file to scan
     */
    public void processEventFile(final File events) {
        final Runnable runnable = () -> {
            StringBuilder error = new StringBuilder();
            if (doIt(events, error)) {
                display();
            } else {
                LOGGER.severe("Reading Yale CAEN Scalers "
                        + error.toString());
            }
        };
        this.executor.submit(runnable);
    }

    private void updateProgressBar(final String text, final int value) {
        pBstatus.setNote(text);
        pBstatus.setProgress(value);
    }

}
