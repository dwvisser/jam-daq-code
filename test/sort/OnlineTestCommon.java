package test.sort;

import static org.junit.Assert.fail;
import injection.GuiceInjector;
import jam.script.Session;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;
import jam.sort.stream.YaleInputStream;
import jam.sort.stream.YaleOutputStream;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import test.sort.mockfrontend.MessageReceiver;
import test.sort.mockfrontend.TestFrontEnd;

/**
 * Common utility methods for testing online acquisition.
 * @author Dale Visser
 */
public final class OnlineTestCommon {

    private OnlineTestCommon() {
        // make no instance
    }

    /**
     * Scripting object to be used by tests.
     */
    public static Session session = GuiceInjector
            .getObjectInstance(Session.class);

    private static final ExecutorService EXECUTOR = GuiceInjector
            .getObjectInstance(ExecutorService.class);

    /**
     * GUI mock front end application.
     */
    private static TestFrontEnd testFrontEnd = new TestFrontEnd();

    protected static MessageReceiver getMessageReceiver() {
        return testFrontEnd.getMessageReceiver();
    }

    private static Future<?> submitSetupOnline(final String sortName,
            final Class<? extends AbstractEventInputStream> inputStream,
            final Class<? extends AbstractEventOutputStream> outputStream) {
        return EXECUTOR.submit(createSetupRunner(sortName, inputStream,
                outputStream));
    }

    private static Runnable createSetupRunner(final String sortName,
            final Class<? extends AbstractEventInputStream> inputStream,
            final Class<? extends AbstractEventOutputStream> outputStream) {
        return new Runnable() {
            public void run() {
                session.online.setup(sortName, inputStream, outputStream);
            }
        };
    }

    /**
     * Setup online sorting within 20 seconds or fail, using YaleInputStream and
     * YaleOutputStream.
     * @param sortName
     *            name of the sort routine to set up.
     */
    public static void setupWithinTimeoutPeriod(final String sortName) {
        setupWithinTimeoutPeriod(sortName, YaleInputStream.class,
                YaleOutputStream.class);
    }

    /**
     * Setup online sorting within 20 seconds or fail.
     * @param sortName
     *            name of the sort routine to set up.
     * @param inputStream
     *            event input stream to use
     * @param outputStream
     *            event output stream to use
     */
    public static void setupWithinTimeoutPeriod(final String sortName,
            final Class<? extends AbstractEventInputStream> inputStream,
            final Class<? extends AbstractEventOutputStream> outputStream) {
        final Future<?> setupOnlineFuture = submitSetupOnline(sortName,
                inputStream, outputStream);
        try {
            setupOnlineFuture.get(10L, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            fail(te.getMessage());
        } catch (ExecutionException ee) {
            fail(ee.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getMessage());
        }
    }

}
