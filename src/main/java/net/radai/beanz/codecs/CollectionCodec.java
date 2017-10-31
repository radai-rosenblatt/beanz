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
import java.util.Collection;

import static net.radai.beanz.util.ReflectionUtil.erase;

public class CollectionCodec implements Codec {
    private Type type;
    private Type elementType;
    private Codec elementCodec;

    public CollectionCodec(Type type, Type elementType, Codec elementCodec) {
        if (type == null || elementType == null || elementCodec == null
                || !ClassUtils.isAssignable(erase(elementCodec.getType()), erase(elementType), true)
                || !ReflectionUtil.isCollection(type)) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.elementType = elementType;
        this.elementCodec = elementCodec;
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
        if (!(encoded.startsWith("[") && encoded.endsWith("]"))) {
            throw new IllegalArgumentException("unable to parse a collection out of " + encoded);
        }
        String[] elements = encoded.substring(1, encoded.length()-1).split("\\s*,\\s*");
        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) ReflectionUtil.instantiateCollection(ReflectionUtil.erase(type));
        for (String element : elements) {
            //noinspection unchecked
            collection.add(elementCodec.decode(element));
        }
        return collection;
    }

    @Override
    public String encode(Object object) {
        if (object == null) {
            return null;
        }
        if (!ReflectionUtil.isCollection(object.getClass())) {
            throw new IllegalArgumentException();
        }
        Collection<?> collection = (Collection<?>) object;
        if (collection.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object element : collection) {
            sb.append(elementCodec.encode(element)).append(", ");
        }
        sb.delete(sb.length()-2, sb.length()); //last ", "
        sb.append("]");
        return sb.toString();
    }

    public Collection<?> decodeCollection(Collection<String> strCollection) {
        @SuppressWarnings("unchecked")
        Collection<Object> result = (Collection<Object>) ReflectionUtil.instantiateCollection(erase(getType()));
        for (String strValue : strCollection) {
            Object value = elementCodec.decode(strValue);
            result.add(value);
        }
        return result;
    }

    public Type getElementType() {
        return elementType;
    }

    public Codec getElementCodec() {
        return elementCodec;
    }

    @Override
    public String toString() {
        return ReflectionUtil.prettyPrint(getType()) + " codec: via " + elementCodec;
    }
}
