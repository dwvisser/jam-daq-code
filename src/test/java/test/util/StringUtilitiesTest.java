/*
 * Created on Feb 10, 2005
 */
package test.util;

import injection.GuiceInjector;
import jam.util.StringUtilities;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("Failed to lengthen String properly.",
                util.makeLength(test1, testLength), answer1);
        final String test2 = "Dale Visser";
        final String answer2 = "Dale Vis";
        Assert.assertEquals("Failed to shorten String properly.",
                util.makeLength(test2, testLength), answer2);
    }

    /**
     * Tests whether getASCIIstring() works properly.
     */
    @Test
    public final void testGetASCIIstring() {
        final byte[] hell = {0x48, 0x65, 0x6c, 0x6c };
        final String sHell = "Hell";
        Assert.assertEquals("Failed to convert ASCII byte array properly.",
                util.getASCIIstring(hell), sHell);
    }

}