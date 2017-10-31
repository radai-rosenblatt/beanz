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

package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;
import net.radai.beanz.util.ReflectionUtil;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Type;
import java.util.Map;

import static net.radai.beanz.util.ReflectionUtil.erase;

public class MapCodec implements Codec {
    private Type type;
    private Type keyType;
    private Type valueType;
    private Codec keyCodec;
    private Codec valueCodec;

    public MapCodec(Type type, Type keyType, Type valueType, Codec keyCodec, Codec valueCodec) {
        if (type == null || keyType == null || valueType == null || keyCodec == null || valueCodec == null
                || !ReflectionUtil.isMap(type)
                || !ClassUtils.isAssignable(erase(keyType), erase(ReflectionUtil.getKeyType(type)), true)
                || !ClassUtils.isAssignable(erase(valueType), erase(ReflectionUtil.getElementType(type)), true)
                || !ClassUtils.isAssignable(erase(keyCodec.getType()), erase(keyType), true)
                || !ClassUtils.isAssignable(erase(valueCodec.getType()), erase(valueType), true)) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.keyType = keyType;
        this.valueType = valueType;
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object decode(String encoded) {
        if (encoded == null || (encoded = encoded.trim()).isEmpty()) {
            return null;
        }
        if (!(encoded.startsWith("{") && encoded.endsWith("}"))) {
            throw new IllegalArgumentException();
        }
        String[] elements = encoded.substring(1, encoded.length()-1).split("\\s*,\\s*");
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = (Map<Object, Object>) ReflectionUtil.instantiateMap(erase(type));
        for (String element : elements) {
            String[] kvPair = element.split("\\s*=\\s*");
            Object key = keyCodec.decode(kvPair[0]);
            Object value = valueCodec.decode(kvPair[1]);
            //noinspection unchecked
            map.put(key, value);
        }
        return map;
    }

    @Override
    public String encode(Object object) {
        if (object == null) {
            return null;
        }
        if (!ReflectionUtil.isMap(object.getClass())) {
            throw new IllegalArgumentException();
        }
        Map<?, ?> map = (Map) object;
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<?, ?> entry  : map.entrySet()) {
            sb.append(keyCodec.encode(entry.getKey())).append("=").append(valueCodec.encode(entry.getValue())).append(", ");
        }
        sb.delete(sb.length()-2, sb.length()); //last ", "
        sb.append("}");
        return sb.toString();
    }

    public Map<?, ?> decodeMap(Map<String, String> strMap) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> result = (Map<Object, Object>) ReflectionUtil.instantiateMap(erase(getType()));
        for (Map.Entry<String, String> pair : strMap.entrySet()) {
            String keyStr = pair.getKey();
            String valueStr = pair.getValue();
            Object key = keyCodec.decode(keyStr);
            Object value = valueCodec.decode(valueStr);
            result.put(key, value);
        }
        return result;
    }

    public Type getKeyType() {
        return keyType;
    }

    public Type getValueType() {
        return valueType;
    }

    public Codec getKeyCodec() {
        return keyCodec;
    }

    public Codec getValueCodec() {
        return valueCodec;
    }

    @Override
    public String toString() {
        return ReflectionUtil.prettyPrint(getType()) + " codec: via " + keyCodec + " and " + valueCodec;
    }
}
