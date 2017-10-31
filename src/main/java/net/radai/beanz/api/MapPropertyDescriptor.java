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

import net.radai.beanz.codecs.MapCodec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public interface MapPropertyDescriptor extends PropertyDescriptor {

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
        Map<?, ?> decoded = strValues != null ? codec.decodeMap(strValues) : null;
        set(bean, decoded);
    }

    default Map<?, ?> getMap(Object bean) {
        return (Map<?, ?>) get(bean);
    }

    default Map<String, String> getAsStrings(Object bean) {
        MapCodec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Codec keyCodec = codec.getKeyCodec();
        Codec valueCodec = codec.getValueCodec();
        if (keyCodec == null || valueCodec == null) {
            throw new IllegalStateException();
        }
        Map<?, ?> rawValues = getMap(bean);
        if (rawValues == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawValues.entrySet()) {
            result.put(keyCodec.encode(entry.getKey()), valueCodec.encode(entry.getValue()));
        }
        return result;
    }
}