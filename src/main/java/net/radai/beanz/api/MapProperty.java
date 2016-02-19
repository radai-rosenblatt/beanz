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

import net.radai.beanz.codecs.MapCodec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
public interface MapProperty extends Property {

    @Override
    default PropertyType getType() {
        return PropertyType.MAP;
    }

    @Override
    default MapCodec getCodec() {
        return (MapCodec) getContainingBeanDescriptor().getCodec(getValueType());
    }

    default Type getKeyType() {
        return ReflectionUtil.getKeyType(getValueType());
    }

    default Type getElementType() { //getValueType is taken
        return ReflectionUtil.getElementType(getValueType());
    }

    default void setFromStrings(Object bean, Map<String, String> strValues) {
        MapCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Map<?, ?> decoded = codec.decodeMap(strValues);
        set(bean, decoded);
    }
}