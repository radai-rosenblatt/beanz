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

import net.radai.beanz.api.Bean;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Created by Radai Rosenblatt
 */
public class BeanzTest {

    @Test
    public void testSimpleBeanWrapping() {
        BeanClass instance = new BeanClass();
        Bean<BeanClass> bean = Beanz.wrap(instance);
        Assertions.assertNotNull(bean);
    }

    public enum Enum1 {
        V1, V2;
    }

    public static class BeanClass {
        private String f1;
        private double f2;
        private Integer f3;
        public UUID f4;
        private byte[] f5;
        private List<Long> f6;
        private Map<Enum1, Short> f7;
    }
}
