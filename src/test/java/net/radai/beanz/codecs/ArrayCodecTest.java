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

import org.testng.annotations.Test;
import org.testng.internal.junit.ArrayAsserts;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class ArrayCodecTest {
    @SuppressWarnings("unused") //used via reflection
    private int[] intArray;

    @Test
    public void testIntArrayCodec() throws Exception {
        Type arrayType = getClass().getDeclaredField("intArray").getGenericType();
        ArrayCodec codec = new ArrayCodec(arrayType, int.class, Codecs.BUILT_INS.get(int.class));
        List<String> strings = Arrays.asList("1", "2", "3");
        int[] decoded = (int[]) codec.decodeArray(strings);
        ArrayAsserts.assertArrayEquals(new int[] {1, 2, 3}, decoded);
        decoded = (int[]) codec.decode("[1, 2, 3]");
        ArrayAsserts.assertArrayEquals(new int[] {1, 2, 3}, decoded);
    }
}
