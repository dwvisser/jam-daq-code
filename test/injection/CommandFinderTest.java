package test.injection;

import static org.junit.Assert.assertNotNull;
import injection.Module;
import jam.global.CommandFinder;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test proper injection of CommandFinder object.
 * @author Dale Visser
 */
public class CommandFinderTest {
    /**
     * Test proper injection of CommandFinder object.
     */
    @Test
    public void testCommandFinderInjection() {
        final Injector injector = Guice.createInjector(new Module());
        final CommandFinder finder = injector.getInstance(CommandFinder.class);
        assertNotNull("Want non-null CommandFinder injected.", finder);
    }
}
