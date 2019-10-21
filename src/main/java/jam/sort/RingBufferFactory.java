package jam.sort;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import jam.Version;

/**
 * Generates RingBuffer instances appropriate to whether J2SE 6
 * java.util.concurrent is on the classpath or not.
 * @author Dale Visser
 */
@Singleton
public final class RingBufferFactory {

    private static final Logger LOGGER = Logger
            .getLogger(RingBufferFactory.class.getPackage().getName());

    private transient final Constructor<? extends RingBuffer> ringConstructor;

    @Inject
    public RingBufferFactory() {
        Constructor<? extends RingBuffer> result = null;
        final ClassLoader loader = RingBufferFactory.class.getClassLoader();
        try {
            final Class<?> clazz = loader.loadClass("jam.sort.LinkedBlockingDequeRingBuffer");
            final Class<? extends RingBuffer> ringClass = clazz.asSubclass(RingBuffer.class);
            result = (Constructor<? extends RingBuffer>) ringClass.getDeclaredConstructor(boolean.class);
        } catch (ClassNotFoundException | IllegalArgumentException | NoSuchMethodException e) {
            final String warning = "Could not load expected RingBuffer implementation. Loading an alternate implemetation instead.";
            LOGGER.log(Level.WARNING, warning, e);
        }
        this.ringConstructor = result;
    }

    /**
     * Allocates a fresh buffer array of the correct size, for use by clients of
     * this class.
     * @return a fresh byte array equal in size to one of the buffers
     */
    public byte[] freshBuffer() {
        return new byte[RingBuffer.BUFFER_SIZE];
    }

    /**
     * @return a new ring buffer with a backing store
     */
    public RingBuffer create() {
        return create(false);
    }

    /**
     * Creates a new ring buffer with or without a backing store.
     * @param empty
     *            whether this is a no-capacity ring buffer
     * @return a RingBuffer implementation instance
     */
    public RingBuffer create(final boolean empty) {
        RingBuffer result = null;
        if (this.ringConstructor != null) {
            final String warning = "Could not instantiate the expected RingBuffer implementation. Instantiating an alternate implementation instead.";
            try {
                result = ringConstructor.newInstance(empty);
            } catch (IllegalArgumentException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
                LOGGER.log(Level.WARNING, warning, e);
            }
        }

        if (result == null) {
            result = new SimpleRingBuffer(empty);
        }

        return result;
    }
}
