package injection;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Injects objects.
 * @author Dale Visser
 */
public final class GuiceInjector {
    private static final Injector INJECTOR = Guice
            .createInjector(new Module());

    private GuiceInjector() {
        // static class
    }

    /**
     * @param <T>
     *            type parameter of class to instantiate
     * @param clazz
     *            class to instantiate
     * @return an instance of the requested class
     */
    public static <T> T getObjectInstance(final Class<T> clazz) {
        return INJECTOR.getInstance(clazz);
    }
}
