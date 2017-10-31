/*
 * Copyright (c) 2016 Radai Rosenblatt.
 * This file is part of Beanz.
 *
 * Beanz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beanz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beanz.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Created by Radai Rosenblatt
 */
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
