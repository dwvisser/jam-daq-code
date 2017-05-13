package test.sort;

import injection.GuiceInjector;
import jam.Version;
import jam.sort.RingBuffer;
import jam.sort.RingBufferFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

/**
 * JUnit tests for <code>jam.sort.IRingBuffer</data>.
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @see RingBuffer
 */
public final class RingBufferTest {// NOPMD

    private static final String ARRAYS_NOT_EQUAL = "Arrays should have been equal.";

    private transient RingBuffer ring, emptyRing;

    private transient final RingBufferFactory ringFactory = GuiceInjector
            .getObjectInstance(RingBufferFactory.class);

    private transient final String expectedClassName = GuiceInjector
            .getObjectInstance(Version.class).isJ2SE6() ? "jam.sort.LinkedBlockingDequeRingBuffer"
            : "jam.sort.SimpleRingBuffer";

    private final transient ExecutorService executor = GuiceInjector
            .getObjectInstance(ExecutorService.class);

    /**
     * @param output
     *            result of getBuffer
     * @param input
     *            passed to putBuffer
     * @throws ArrayComparisonFailure
     */
    private void assertArraysEqualButNotSame(final byte[] output,
            final byte[] input) throws ArrayComparisonFailure {
        Assert.assertArrayEquals(ARRAYS_NOT_EQUAL, input, output);
        Assert.assertNotSame("Expect two different arrays.", input, output);
    }

    private void assertRingBufferEmpty(final RingBuffer ring2) {
        Assert.assertTrue("Expected empty ring buffer.", ring2.isEmpty());
        Assert.assertFalse("Expected ring buffer to not be full.",
                ring2.isFull());
        Assert.assertEquals("Expected all buffers in ring to be available.",
                RingBuffer.NUMBER_BUFFERS, ring2.getAvailableBuffers());
    }

    private void assertRingBufferFull(final RingBuffer ring2) {
        Assert.assertFalse("Expected ring buffer to not be empty.",
                ring2.isEmpty());
        Assert.assertTrue("Expected full ring buffer.", ring2.isFull());
        Assert.assertEquals("Expected zero available buffers.", 0,
                ring2.getAvailableBuffers());
    }

    /**
     * @param usedBuffers
     *            number of buffers used up
     */
    private void assertUsedBuffers(final RingBuffer ring2,
            final int usedBuffers) {
        Assert.assertEquals("Expected " + usedBuffers + " buffers used.",
                usedBuffers, ring2.getUsedBuffers());
        final int available = RingBuffer.NUMBER_BUFFERS - usedBuffers;
        Assert.assertEquals("Expected " + available + " buffers available.",
                available, ring2.getAvailableBuffers());
    }

    private void clear(final RingBuffer ring2) {
        ring2.clear();
        this.assertRingBufferEmpty(ring2);
    }

    /**
     * @returns last buffer inserted
     */
    private byte[] fillEmptyRingBuffer(final RingBuffer ring2,
            final int numBuffers) {
        assertRingBufferEmpty(ring2);
        final byte[] buffer = ringFactory.freshBuffer();
        for (int i = 0; i < numBuffers; i++) {
            Arrays.fill(buffer, (byte) (i + 1));
            putBuffer(ring2, buffer, true);
        }
        return buffer;
    }

    /**
     * @param ring2
     * @param buffer
     */
    private void putBuffer(final RingBuffer ring2, final byte[] buffer,
            final boolean expectedSuccess) {
        final String message = expectedSuccess ? "Expected success putting buffer into ring."
                : "Expected failure putting buffer into full ring.";
        if (expectedSuccess) {
            Assert.assertTrue(message, ring2.tryPutBuffer(buffer));
        } else {
            Assert.assertFalse(message, ring2.tryPutBuffer(buffer));
        }
    }

    /**
     * Set up the test.
     */
    @Before
    public void setUp() {
        ring = ringFactory.create();
        emptyRing = ringFactory.create(true);
    }

    /**
     * Test that we have the expected implementation of the RingBuffer interface
     * given the version of JRE that is present.
     */
    @Test
    public void testCorrectRingBufferClassForJRE() {
        final String actualClassName = ring.getClass().getName();
        Assert.assertEquals("Expected a certain class.", actualClassName,
                expectedClassName);
    }

    /**
     * Test bringing the buffer close to full.
     */
    @Test
    public void testCloseToFull() {
        this.clear(ring);
        this.fillEmptyRingBuffer(ring, RingBuffer.NUMBER_BUFFERS
                - RingBuffer.CLOSE_TO_CAPACITY + 1);
        Assert.assertTrue("Expected buffer to be close to full.",
                ring.isCloseToFull());
    }

    /**
     * Test getting the number of available buffers.
     */
    @Test
    public void testGetAvailableBuffers() {
        Assert.assertEquals("Expected all buffers to be available.",
                RingBuffer.NUMBER_BUFFERS, ring.getAvailableBuffers());
        final byte[] buffer = ringFactory.freshBuffer();
        for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
            putBuffer(ring, buffer, true);
            assertUsedBuffers(ring, i + 1);
        }
        assertRingBufferFull(ring);
        Assert.assertEquals("Expect no available buffers in empty ring.", 0,
                emptyRing.getAvailableBuffers());
        this.clear(ring);
    }

    /**
     * Test the properties of 'null' rings.
     */
    @Test
    public void testIsNull() {
        Assert.assertTrue("emptyRing explicitly 'null'", emptyRing.isNull());
        Assert.assertFalse("Allocated ring not null.", ring.isNull());
        Assert.assertTrue("'null' rings are full.", emptyRing.isFull());
        Assert.assertTrue("'null' rings are nearly full.",
                emptyRing.isCloseToFull());
        Assert.assertTrue("'null' rings are empty.", emptyRing.isEmpty());
        Assert.assertEquals("'null' rings never have used buffers.", 0,
                emptyRing.getUsedBuffers());
    }

    /**
     * Test for <code>putBuffer()</code>.
     * @throws InterruptedException
     *             if a get buffer operation fails
     * @see RingBuffer#tryPutBuffer(byte [])
     */
    @Test
    public void testPut() throws InterruptedException {
        final byte[] out = ringFactory.freshBuffer();
        byte[] buffer = ringFactory.freshBuffer();
        this.clear(ring);
        for (int i = 0; i < RingBuffer.NUMBER_BUFFERS / 2; i++) {
            Arrays.fill(buffer, (byte) i);
            putBuffer(ring, buffer, true);
            this.assertUsedBuffers(ring, 1);
            ring.getBuffer(out);
            this.assertRingBufferEmpty(ring);
            assertArraysEqualButNotSame(out, buffer);
        }
        buffer = fillEmptyRingBuffer(ring, RingBuffer.NUMBER_BUFFERS);
        assertRingBufferFull(ring);
        ring.getBuffer(out);
        /* Ring buffer is FIFO. */
        Assert.assertFalse("Expected arrays to not be equal.",
                Arrays.equals(buffer, out));
        Assert.assertFalse("Expected ring buffer to not be full.",
                ring.isFull());
        putBuffer(ring, buffer, true);
        assertRingBufferFull(ring);
        putBuffer(ring, buffer, false);
    }

    /**
     * Tests that get waits on put successfully.
     */
    @Test
    public void testGetWaitingOnPut() {
        ring.clear();
        final long sleepMilliseconds = 10L;
        final long timeoutMsec = 2 * sleepMilliseconds;
        final Putter putter = new Putter(ring, sleepMilliseconds);
        final Getter getter = new Getter(ring);
        final Future<Boolean> putFuture = executor.submit(putter);
        final Future<byte[]> getFuture = executor.submit(getter);
        try {
            Assert.assertTrue("Expected put to return true.",
                    putFuture.get(timeoutMsec, TimeUnit.MILLISECONDS));
            getFuture.get(timeoutMsec, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ie) {
            Assert.fail("Test interrupted.\n" + ie.getMessage());
        } catch (TimeoutException te) {
            Assert.fail("Test timed out.\n" + te.getMessage());
        } catch (ExecutionException ee) {
            Assert.fail("Worker threw an exception.\n" + ee.getMessage());
        }
    }

    static class Getter implements Callable<byte[]> {
        private transient final RingBuffer ring;
        private static final byte[] buffer = GuiceInjector.getObjectInstance(
                RingBufferFactory.class).freshBuffer();

        Getter(final RingBuffer ring) {
            this.ring = ring;
        }

        public byte[] call() throws InterruptedException {
            ring.getBuffer(buffer);
            return buffer; // NOPMD
        }
    }

    static class Putter implements Callable<Boolean> {

        private transient final RingBufferFactory ringFactory = GuiceInjector
                .getObjectInstance(RingBufferFactory.class);

        private transient final RingBuffer ring;

        private transient final long msecToSleep;

        Putter(final RingBuffer ring, final long milliseconds) {
            this.ring = ring;
            this.msecToSleep = milliseconds;
        }

        public Boolean call() {
            Boolean result = Boolean.FALSE;
            try {
                Thread.sleep(this.msecToSleep);
                result = ring.tryPutBuffer(ringFactory
                        .freshBuffer());
            } catch (InterruptedException ie) {
                // failure
            }
            return result;
        }
    }
}
