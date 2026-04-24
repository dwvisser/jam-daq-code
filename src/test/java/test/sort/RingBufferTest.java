package test.sort;

import injection.GuiceInjector;
import jam.sort.RingBuffer;
import jam.sort.RingBufferFactory;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit tests for <code>jam.sort.IRingBuffer</code>.
 *
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @see RingBuffer
 */
public final class RingBufferTest { // NOPMD

  private static final String ARRAYS_NOT_EQUAL = "Arrays should have been equal.";

  private transient RingBuffer ring, emptyRing;

  private final transient RingBufferFactory ringFactory =
      GuiceInjector.getObjectInstance(RingBufferFactory.class);

  private final transient String expectedClassName = "jam.sort.LinkedBlockingDequeRingBuffer";

  private final transient ExecutorService executor =
      GuiceInjector.getObjectInstance(ExecutorService.class);

  /**
   * @param output result of getBuffer
   * @param input passed to putBuffer
   */
  private void assertArraysEqualButNotSame(final byte[] output, final byte[] input) {
    Assertions.assertArrayEquals(input, output, ARRAYS_NOT_EQUAL);
    Assertions.assertNotSame(input, output, "Expect two different arrays.");
  }

  private void assertRingBufferEmpty(final RingBuffer ring2) {
    Assertions.assertTrue(ring2.isEmpty(), "Expected empty ring buffer.");
    Assertions.assertFalse(ring2.isFull(), "Expected ring buffer to not be full.");
    Assertions.assertEquals(
        RingBuffer.NUMBER_BUFFERS,
        ring2.getAvailableBuffers(),
        "Expected all buffers in ring to be available.");
  }

  private void assertRingBufferFull(final RingBuffer ring2) {
    Assertions.assertFalse(ring2.isEmpty(), "Expected ring buffer to not be empty.");
    Assertions.assertTrue(ring2.isFull(), "Expected full ring buffer.");
    Assertions.assertEquals(0, ring2.getAvailableBuffers(), "Expected zero available buffers.");
  }

  /**
   * @param usedBuffers number of buffers used up
   */
  private void assertUsedBuffers(final RingBuffer ring2, final int usedBuffers) {
    Assertions.assertEquals(
        usedBuffers, ring2.getUsedBuffers(), "Expected " + usedBuffers + " buffers used.");
    final int available = RingBuffer.NUMBER_BUFFERS - usedBuffers;
    Assertions.assertEquals(
        available, ring2.getAvailableBuffers(), "Expected " + available + " buffers available.");
  }

  private void clear(final RingBuffer ring2) {
    ring2.clear();
    this.assertRingBufferEmpty(ring2);
  }

  /**
   * @param ring2 an empty ring buffer
   * @param numBuffers number of byte buffers to fill in
   * @return last buffer inserted
   */
  private byte[] fillEmptyRingBuffer(final RingBuffer ring2, final int numBuffers) {
    assertRingBufferEmpty(ring2);
    final byte[] buffer = ringFactory.freshBuffer();
    for (int i = 0; i < numBuffers; i++) {
      Arrays.fill(buffer, (byte) (i + 1));
      putBuffer(ring2, buffer, true);
    }
    return buffer;
  }

  /**
   * @param ring2 a ring buffer
   * @param buffer byte buffer to attempt to put in
   */
  private void putBuffer(
      final RingBuffer ring2, final byte[] buffer, final boolean expectedSuccess) {
    final String message =
        expectedSuccess
            ? "Expected success putting buffer into ring."
            : "Expected failure putting buffer into full ring.";
    if (expectedSuccess) {
      Assertions.assertTrue(ring2.tryPutBuffer(buffer), message);
    } else {
      Assertions.assertFalse(ring2.tryPutBuffer(buffer), message);
    }
  }

  /** Set up the test. */
  @BeforeEach
  public void setUp() {
    ring = ringFactory.create();
    emptyRing = ringFactory.create(true);
  }

  /**
   * Test that we have the expected implementation of the RingBuffer interface given the version of
   * JRE that is present.
   */
  @Test
  public void testCorrectRingBufferClassForJRE() {
    final String actualClassName = ring.getClass().getName();
    Assertions.assertEquals(expectedClassName, actualClassName, "Expected a certain class.");
  }

  /** Test bringing the buffer close to full. */
  @Test
  public void testCloseToFull() {
    this.clear(ring);
    this.fillEmptyRingBuffer(ring, RingBuffer.NUMBER_BUFFERS - RingBuffer.CLOSE_TO_CAPACITY + 1);
    Assertions.assertTrue(ring.isCloseToFull(), "Expected buffer to be close to full.");
  }

  /** Test getting the number of available buffers. */
  @Test
  public void testGetAvailableBuffers() {
    Assertions.assertEquals(
        RingBuffer.NUMBER_BUFFERS,
        ring.getAvailableBuffers(),
        "Expected all buffers to be available.");
    final byte[] buffer = ringFactory.freshBuffer();
    for (int i = 0; i < RingBuffer.NUMBER_BUFFERS; i++) {
      putBuffer(ring, buffer, true);
      assertUsedBuffers(ring, i + 1);
    }
    assertRingBufferFull(ring);
    Assertions.assertEquals(
        0, emptyRing.getAvailableBuffers(), "Expect no available buffers in empty ring.");
    this.clear(ring);
  }

  /** Test the properties of 'null' rings. */
  @Test
  public void testIsNull() {
    Assertions.assertTrue(emptyRing.isNull(), "emptyRing explicitly 'null'");
    Assertions.assertFalse(ring.isNull(), "Allocated ring not null.");
    Assertions.assertTrue(emptyRing.isFull(), "'null' rings are full.");
    Assertions.assertTrue(emptyRing.isCloseToFull(), "'null' rings are nearly full.");
    Assertions.assertTrue(emptyRing.isEmpty(), "'null' rings are empty.");
    Assertions.assertEquals(0, emptyRing.getUsedBuffers(), "'null' rings never have used buffers.");
  }

  /**
   * Test for <code>putBuffer()</code>.
   *
   * @throws InterruptedException if a get buffer operation fails
   * @see RingBuffer#tryPutBuffer(byte[])
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
    Assertions.assertFalse(Arrays.equals(buffer, out), "Expected arrays to not be equal.");
    Assertions.assertFalse(ring.isFull(), "Expected ring buffer to not be full.");
    putBuffer(ring, buffer, true);
    assertRingBufferFull(ring);
    putBuffer(ring, buffer, false);
  }

  /** Tests that get waits on put successfully. */
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
      Assertions.assertTrue(
          putFuture.get(timeoutMsec, TimeUnit.MILLISECONDS), "Expected put to return true.");
      getFuture.get(timeoutMsec, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ie) {
      Assertions.fail("Test interrupted.\n" + ie.getMessage());
    } catch (TimeoutException te) {
      Assertions.fail("Test timed out.\n" + te.getMessage());
    } catch (ExecutionException ee) {
      Assertions.fail("Worker threw an exception.\n" + ee.getMessage());
    }
  }

  static class Getter implements Callable<byte[]> {
    private final transient RingBuffer ring;
    private static final byte[] buffer =
        GuiceInjector.getObjectInstance(RingBufferFactory.class).freshBuffer();

    Getter(final RingBuffer ring) {
      this.ring = ring;
    }

    public byte[] call() throws InterruptedException {
      ring.getBuffer(buffer);
      return buffer; // NOPMD
    }
  }

  static class Putter implements Callable<Boolean> {

    private final transient RingBufferFactory ringFactory =
        GuiceInjector.getObjectInstance(RingBufferFactory.class);

    private final transient RingBuffer ring;

    private final transient long msecToSleep;

    Putter(final RingBuffer ring, final long milliseconds) {
      this.ring = ring;
      this.msecToSleep = milliseconds;
    }

    public Boolean call() {
      Boolean result = Boolean.FALSE;
      try {
        Thread.sleep(this.msecToSleep);
        result = ring.tryPutBuffer(ringFactory.freshBuffer());
      } catch (InterruptedException ie) {
        // failure
      }
      return result;
    }
  }
}
