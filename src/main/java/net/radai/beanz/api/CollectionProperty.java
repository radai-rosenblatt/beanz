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
import java.util.Collection;

/**
 * Created by Radai Rosenblatt
 */
public interface CollectionProperty extends Property {

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
        Collection<?> decoded = codec.decodeCollection(strValues);
        set(bean, decoded);
    }
}