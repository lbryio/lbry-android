package io.lbry.browser.utils;

import org.junit.Before;
import org.junit.Test;

import io.lbry.browser.exceptions.LbryUriException;

import static org.junit.Assert.assertEquals;

public class LbryUriTest {
    private LbryUri expected;

    /*
     * Create an LbryUri object and assign fields manually using class methods. This object will be
     * compared with LbryUri.parse() returned object on each test.
     */
    @Before
    public void createExpected() {
        expected = new LbryUri();
        expected.setChannelName("@lbry");
        expected.setStreamName("lbryturns4");

        try {
            LbryUri.UriModifier primaryMod = LbryUri.UriModifier.parse("#", "3f");
            LbryUri.UriModifier secondaryMod = LbryUri.UriModifier.parse("#", "6");
            expected.setChannelClaimId(primaryMod.getClaimId());
            expected.setStreamClaimId(secondaryMod.getClaimId());
        } catch (LbryUriException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void parseOpenLbryComWithChannel() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("https://open.lbry.com/@lbry:3f/lbryturns4:6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expected, obtained);
    }

    @Test
    public void parseLbryTvWithChannel() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("https://lbry.tv/@lbry:3f/lbryturns4:6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expected, obtained);
    }

    @Test
    public void parseLbryAlterWithChannel() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("https://lbry.lat/@lbry:3f/lbryturns4:6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expected, obtained);
    }

    @Test
    public void parseLbryProtocolWithChannel() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("lbry://@lbry#3f/lbryturns4#6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expected, obtained);
    }
}