package io.lbry.browser.utils;

import junit.framework.TestCase;

public class HelperTest extends TestCase {

    public void testSHA256() {
        // Using a fake user id, which is a long.
        assertEquals("de9edb2044d012f04553e49b04d54cbec8e8a46a40ad5a19bc5dcce1da00ecfd", Helper.SHA256(String.valueOf(12345678912345L)));
    }
}