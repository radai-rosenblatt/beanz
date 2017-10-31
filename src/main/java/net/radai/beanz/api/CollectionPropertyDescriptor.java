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

import net.radai.beanz.codecs.CollectionCodec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

public interface CollectionPropertyDescriptor extends PropertyDescriptor {

    @Override
    default PropertyType getType() {
        return PropertyType.COLLECTION;
    }

    @Override
    default CollectionCodec getCodec() {
        return (CollectionCodec) getContainingBeanDescriptor().getCodec(getValueType());
    }

    default Type getElementType() {
        return ReflectionUtil.getElementType(getValueType());
    }

    default void setCollection(Object bean, Collection<?> values) {
        if (values == null) {
            set(bean, null);
            return;
        }
        Class<?> collectionClass = ReflectionUtil.erase(getValueType());
        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) ReflectionUtil.instantiateCollection(collectionClass);
        collection.addAll(values);
        set(bean, collection);
    }

    default void setFromStrings(Object bean, Collection<String> strValues) {
        CollectionCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Collection<?> decoded = strValues != null ? codec.decodeCollection(strValues) : null;
        set(bean, decoded);
    }

    default Collection<?> getCollection(Object bean) {
        return (Collection<?>) get(bean);
    }

    default Collection<String> getAsStrings(Object bean) {
        CollectionCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Codec elementCodec = codec.getElementCodec();
        if (elementCodec == null) {
            throw new IllegalStateException();
        }
        Collection<?> rawValues = getCollection(bean);
        if (rawValues == null) {
            return null;
        }
        Collection<String> result = new ArrayList<>(rawValues.size());
        for (Object element : rawValues) {
            result.add(elementCodec.encode(element));
        }
        return result;
    }
}