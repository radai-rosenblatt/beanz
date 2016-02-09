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

package net.radai.beanz;

import net.radai.beanz.api.Pod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Radai Rosenblatt
 */
public class BeanzTest {

    @Test
    public void testSimpleBeanWrapping() {
        Bean1 bean = new Bean1();
        Pod pod = Beanz.wrap(bean);
        int g = 7;
    }

    public static enum Enum1 {
        V1, V2;
    }

    public static class Bean1 {
        private String f1;
        private double f2;
        private Integer f3;
        public UUID f4;
        private byte[] f5;
        private List<Long> f6;
        private Map<Enum1, Short> f7;
    }
}
