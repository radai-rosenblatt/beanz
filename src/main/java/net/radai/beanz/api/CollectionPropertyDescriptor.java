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

import net.radai.beanz.codecs.CollectionCodec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Radai Rosenblatt
 */
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
        //noinspection unchecked
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