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

import net.radai.beanz.codecs.ArrayCodec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by Radai Rosenblatt
 */
public interface ArrayProperty extends Property {

    @Override
    default PropertyType getType() {
        return PropertyType.ARRAY;
    }

    @Override
    default ArrayCodec getCodec() {
        return (ArrayCodec) getContainingBeanDescriptor().getCodec(getValueType());
    }

    default Type getElementType() {
        return ReflectionUtil.getElementType(getValueType());
    }

    default void setArray(Object bean, Collection<?> values) {
        Type elementType = getElementType();
        Object array = ReflectionUtil.instatiateArray(elementType, values.size());
        int i=0;
        for (Object value : values) {
            Array.set(array, i++, value);
        }
        set(bean, array);
    }

    default void setFromStrings(Object bean, Collection<String> strValues) {
        ArrayCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Object decoded = codec.decodeArray(strValues);
        set(bean, decoded);
    }
}
