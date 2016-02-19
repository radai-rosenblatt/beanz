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

package net.radai.beanz.api;

/**
 * Created by Radai Rosenblatt
 */
public class Pod {
    private final BeanDescriptor descriptor;
    private final Object bean;

    public Pod(BeanDescriptor descriptor, Object bean) {
        this.descriptor = descriptor;
        this.bean = bean;
    }

    public Property resolve(String propName) {
        return descriptor.getProperty(propName);
    }

    public void set(Property prop, String value) {
        Codec codec = prop.getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        set(prop, codec.decode(value));
    }

    public void set(Property prop, Object value) {
        if (resolve(prop.getName()) != prop) {
            throw new IllegalArgumentException();
        }
        prop.set(bean, value);
    }

    public Object getBean() {
        return bean;
    }
}
