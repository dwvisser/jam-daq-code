package test.injection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import injection.Module;

import java.awt.Frame;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test cases for frame dependency injection.
 * @author Dale Visser
 */
public class FrameTest {
    /**
     * Test case for frame dependency injection.
     */
    @Test
    public void injectsCorrectFrame() {
        final Injector injector = Guice.createInjector(new Module());
        final JFrame jframe = injector.getInstance(JFrame.class);
        assertEquals("Jam", jframe.getTitle(), "Frame title should be 'Jam'");
        final Frame frame = injector.getInstance(Frame.class);
        assertEquals(frame, jframe, "jframe and frame should be same object");
    }
}
