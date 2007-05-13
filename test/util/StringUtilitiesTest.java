/*
 * Created on Feb 10, 2005
 */
package test.util;

import jam.util.StringUtilities;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public class StringUtilitiesTest extends TestCase {//NOPMD

    private transient final StringUtilities util = StringUtilities.getInstance();

    /**
     * Tests whether makeLength() works properly.
     */
    public final void testMakeLength() {
        final int testLength = 8;
        final String test1 = "Dale";
        final String test2 = "Dale Visser";
        final String answer1 = "Dale    ";
        final String answer2 = "Dale Vis";
        assertEquals("Failed to lengthen String properly.", util.makeLength(
                test1, testLength), answer1);
        assertEquals("Failed to shorten String properly.", util.makeLength(
                test2, testLength), answer2);
    }

    /**
     * Tests whether getASCIIstring() works properly.
     */
    public final void testGetASCIIstring() {
        final byte[] hell = { 0x48, 0x65, 0x6c, 0x6c };
        final String sHell = "Hell";
        assertEquals("Failed to convert ASCII byte array properly.", util
                .getASCIIstring(hell), sHell);
    }

}