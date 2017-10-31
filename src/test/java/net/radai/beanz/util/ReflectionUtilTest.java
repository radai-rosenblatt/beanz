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

package net.radai.beanz.util;


import java.lang.reflect.Type;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Created by Radai Rosenblatt
 */
public class ReflectionUtilTest {
    @SuppressWarnings("unused") //it is, via reflection
    private Map<String, String> map;

    @Test
    public void testErase() throws Exception {
        Type mapType = getClass().getDeclaredField("map").getGenericType();
        Assertions.assertNotEquals(Map.class, mapType);
        Assertions.assertEquals(Map.class, ReflectionUtil.erase(mapType));
    }
}
