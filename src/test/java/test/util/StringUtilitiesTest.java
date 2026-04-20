/*
 * Created on Feb 10, 2005
 */
package test.util;

import injection.GuiceInjector;
import jam.util.StringUtilities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser </a>
 */
public class StringUtilitiesTest {// NOPMD

    private transient final StringUtilities util = GuiceInjector
            .getObjectInstance(StringUtilities.class);

    /**
     * Tests whether makeLength() works properly.
     */
    @Test
    public final void testMakeLength() {
        final int testLength = 8;
        final String test1 = "Dale";
        final String answer1 = "Dale    ";
        Assertions.assertEquals(answer1, util.makeLength(test1, testLength),
                "Failed to lengthen String properly.");
        final String test2 = "Dale Visser";
        final String answer2 = "Dale Vis";
        Assertions.assertEquals(answer2, util.makeLength(test2, testLength),
                "Failed to shorten String properly.");
    }

    /**
     * Tests whether getASCIIstring() works properly.
     */
    @Test
    public final void testGetASCIIstring() {
        final byte[] hell = {0x48, 0x65, 0x6c, 0x6c };
        final String sHell = "Hell";
        Assertions.assertEquals(sHell, util.getASCIIstring(hell),
                "Failed to convert ASCII byte array properly.");
    }

}