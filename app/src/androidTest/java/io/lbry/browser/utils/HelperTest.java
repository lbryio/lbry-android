package io.lbry.browser.utils;

import androidx.test.filters.SmallTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SmallTest
public class HelperTest {
    @Test
    public void SHA256() {
        assertEquals("de9edb2044d012f04553e49b04d54cbec8e8a46a40ad5a19bc5dcce1da00ecfd", Helper.SHA256(String.valueOf(12345678912345L)));
    }
}
