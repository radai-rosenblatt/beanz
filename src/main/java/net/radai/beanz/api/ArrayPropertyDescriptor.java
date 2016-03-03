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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public interface ArrayPropertyDescriptor extends PropertyDescriptor {

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
        if (values == null) {
            set(bean, null);
            return;
        }
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
        Object decoded = strValues != null ? codec.decodeArray(strValues) : null;
        set(bean, decoded);
    }

    default List<Object> getAsList(Object bean) {
        Object array = get(bean);
        if (array == null) {
            return null;
        }
        int length = Array.getLength(array);
        List<Object> result = new ArrayList<>(length);
        for (int i=0; i<length; i++) {
            result.add(Array.get(array, i));
        }
        return result;
    }

    default List<String> getAsStrings(Object bean) {
        ArrayCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Codec elementCodec = codec.getElementCodec();
        if (elementCodec == null) {
            throw new IllegalStateException();
        }
        Object array = get(bean);
        if (array == null) {
            return null;
        }
        int length = Array.getLength(array);
        List<String> result = new ArrayList<>(length);
        for (int i=0; i<length; i++) {
            Object element = Array.get(array, i);
            result.add(elementCodec.encode(element));
        }
        return result;
    }
}
