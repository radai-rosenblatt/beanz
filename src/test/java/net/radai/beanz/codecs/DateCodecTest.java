/*
 * Copyright 2017 Radai Rosenblat
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateCodecTest {
    private SimpleDateFormat fullFormat;
    private SimpleDateFormat shortFormat;
    private Codec codec;

    @BeforeEach
    public void before() {
        fullFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS Z");
        fullFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        shortFormat = new SimpleDateFormat("dd/MM/yyyy");
        shortFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        codec = Codecs.BUILT_INS.get(Date.class);
    }

    @Test
    public void testDate() throws Exception {
        Date date = shortFormat.parse("01/02/1956");
        String encoded = codec.encode(date);
        Object decoded = codec.decode(encoded);
        Assertions.assertEquals(date, decoded);
    }

    @Test
    public void testTimestamp() throws Exception {
        Date date = fullFormat.parse("01/02/1956 01:02:03.456 +0800");
        String encoded = codec.encode(date);
        Object decoded = codec.decode(encoded);
        Assertions.assertEquals(date, decoded);
    }

    @Test
    public void testNulls() throws Exception {
        Assertions.assertNull(codec.encode(null));
        Assertions.assertNull(codec.decode(null));
        Assertions.assertNull(codec.decode(""));
    }
}
