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

package net.radai.beanz.api;

import net.radai.beanz.codecs.ArrayCodec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
