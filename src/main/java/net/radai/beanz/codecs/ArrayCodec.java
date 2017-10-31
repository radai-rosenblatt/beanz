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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;

import static net.radai.beanz.util.ReflectionUtil.erase;
import static net.radai.beanz.util.ReflectionUtil.prettyPrint;

public class ArrayCodec implements Codec {
    private Type type;
    private Type elementType;
    private Codec elementCodec;

    public ArrayCodec(Type type, Type elementType, Codec elementCodec) {
        if (type == null || elementType == null || elementCodec == null
                || !ClassUtils.isAssignable(erase(elementCodec.getType()), erase(elementType), true)
                || !ReflectionUtil.isArray(type)) {
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
            throw new IllegalArgumentException();
        }
        String[] elements = encoded.substring(1, encoded.length()-1).split("\\s*,\\s*");
        Class<?> erased = erase(getElementType());
        Object array = Array.newInstance(erased, elements.length);
        for (int i=0; i<elements.length; i++) {
            Array.set(array, i, elementCodec.decode(elements[i]));
        }
        return array;
    }

    @Override
    public String encode(Object object) {
        if (object == null) {
            return null;
        }
        if (!ReflectionUtil.isArray(object.getClass())) {
            throw new IllegalArgumentException();
        }
        int size = Array.getLength(object);
        if (size == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0; i<size; i++) {
            Object element = Array.get(object, i);
            String encoded = elementCodec.encode(element);
            sb.append(encoded).append(", ");
        }
        sb.delete(sb.length()-2, sb.length()); //last ", "
        sb.append("]");
        return sb.toString();
    }

    public Object decodeArray(Collection<String> strCollection) {
        Object result = ReflectionUtil.instatiateArray(getElementType(), strCollection.size());
        int i=0;
        for (String strValue : strCollection) {
            Object value = elementCodec.decode(strValue);
            Array.set(result, i++, value); //this is primitive-safe
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
        return prettyPrint(getType()) + " codec: via " + elementCodec;
    }
}
