package io.lbry.browser.utils;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import io.lbry.browser.exceptions.LbryUriException;

import static org.junit.Assert.assertEquals;

public class LbryUriTest {
    private LbryUri expected;
    private LbryUri expectedOctoshape;

    /*
     * Create an LbryUri object and assign fields manually using class methods. This object will be
     * compared with LbryUri.parse() returned object on each test.
     */
    @Before
    public void createExpected() {
        expected = new LbryUri();
        expectedOctoshape = new LbryUri();
        expected.setChannelName("@lbry");
        expected.setStreamName("lbryturns4");
        expectedOctoshape.setChannelName("@lbry");
        expectedOctoshape.setStreamName("lbryturns4");

        try {
            LbryUri.UriModifier primaryMod = LbryUri.UriModifier.parse(":", "3f");
            LbryUri.UriModifier secondaryMod = LbryUri.UriModifier.parse(":", "6");
            LbryUri.UriModifier primaryModOctoshape = LbryUri.UriModifier.parse("#", "3f");
            LbryUri.UriModifier secondaryModOctoshape = LbryUri.UriModifier.parse("#", "6");
            expected.setChannelClaimId(primaryMod.getClaimId());
            expected.setStreamClaimId(secondaryMod.getClaimId());
            expectedOctoshape.setChannelClaimId(primaryModOctoshape.getClaimId());
            expectedOctoshape.setStreamClaimId(secondaryModOctoshape.getClaimId());
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
    public void parseLbryProtocolWithChannelOctoshape() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("lbry://@lbry#3f/lbryturns4#6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expectedOctoshape, obtained);
    }

    @Test
    public void parseLbryProtocolOnlyChannel() {
        LbryUri expectedForChannel = sinthesizeExpectedChannelOctoshape();

        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("lbry://@UCBerkeley#d",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expectedForChannel, obtained);
    }

    @Test
    public void parseLbryTvProtocolOnlyChannel() {
        LbryUri expectedForChannel = sinthesizeExpectedChannelOctoshape();

        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("https://lbry.tv/@UCBerkeley:d",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expectedForChannel, obtained);
    }

    @Test
    public void parseLbryTvWithEncodedChars() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("https://lbry.tv/@Content_I_Like:1/DR.-ASTRID-ST%C3%9CCKELBERGER:2",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        expected = new LbryUri();
        expected.setChannelName("@Content_I_Like");
        expected.setStreamName("DR.-ASTRID-STÜCKELBERGER");

        try {
            LbryUri.UriModifier primaryMod = LbryUri.UriModifier.parse("#", "1");
            LbryUri.UriModifier secondaryMod = LbryUri.UriModifier.parse("#", "2");
            expected.setChannelClaimId(primaryMod.getClaimId());
            expected.setStreamClaimId(secondaryMod.getClaimId());
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals(expected, obtained);
    }

    @Test
    public void lbryToTvString() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("lbry://@lbry#3f/lbryturns4#6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals("https://lbry.tv/@lbry:3f/lbryturns4:6", obtained.toTvString());
    }

    @Test
    public void lbryToTvStringWithEncodedChars() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("lbry://La-Peur,-Nos-Attentats,-c'est-VOTRE-Sécurité!-Les-Guignols#6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals("https://lbry.tv/La-Peur%2C-Nos-Attentats%2C-c%27est-VOTRE-Se%CC%81curite%CC%81%21-Les-Guignols:6", obtained.toTvString());
    }

    @Test
    public void lbryToTvStringWithChannelAndEncodedChars() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("lbry://@test#1/La-Peur,-Nos-Attentats,-c'est-VOTRE-Sécurité!-Les-Guignols#6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals("https://lbry.tv/@test:1/La-Peur%2C-Nos-Attentats%2C-c%27est-VOTRE-Se%CC%81curite%CC%81%21-Les-Guignols:6", obtained.toTvString());
    }

    @Test
    public void lbryToOdyseeString() {
        LbryUri obtained = new LbryUri();

        try {
            obtained = LbryUri.parse("lbry://@lbry#3f/lbryturns4#6",false);
        } catch (LbryUriException e) {
            e.printStackTrace();
        }

        assertEquals("https://odysee.com/@lbry:3f/lbryturns4:6", obtained.toOdyseeString());
    }

    @NotNull
    private LbryUri sinthesizeExpectedChannelOctoshape() {
        LbryUri expectedForChannel = new LbryUri();
        expectedForChannel.setChannelName("@UCBerkeley");
        expectedForChannel.setChannel(true);

        try {
            LbryUri.UriModifier primaryMod = LbryUri.UriModifier.parse("#", "d");
            expectedForChannel.setChannelClaimId(primaryMod.getClaimId());
        } catch (LbryUriException e) {
            e.printStackTrace();
        }
        return expectedForChannel;
    }
}