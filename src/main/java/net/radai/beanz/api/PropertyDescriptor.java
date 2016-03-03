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

import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public interface PropertyDescriptor {
    BeanDescriptor getContainingBeanDescriptor();
    String getName();
    PropertyType getType();
    Type getValueType();
    boolean isReadable();
    boolean isWritable();
    Object get(Object bean);
    default String getAsString(Object bean) {
        Codec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Object rawValue = get(bean);
        return rawValue != null ? codec.encode(rawValue) : null;
    }
    void set(Object bean, Object value);
    default void setFromString(Object bean, String strValue) {
        Codec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Object value = strValue != null ? codec.decode(strValue) : null;
        set(bean, value);
    }
    default Codec getCodec() {
        return getContainingBeanDescriptor().getCodec(getValueType());
    }
}
