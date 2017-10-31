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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class SimpleCodec implements Codec {
    private Type type;
    private Method encodeMethod = null;
    private Method decodeMethod = null;

    public SimpleCodec(Type type, Method encodeMethod, Method decodeMethod) {
        if (type == null || encodeMethod == null || decodeMethod == null || !ReflectionUtil.isStatic(decodeMethod)) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.encodeMethod = encodeMethod;
        this.decodeMethod = decodeMethod;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            return decodeMethod.invoke(null, encoded);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String encode(Object object) {
        if (object == null) {
            return null;
        }
        try {
            if (ReflectionUtil.isStatic(encodeMethod)) {
                return (String) encodeMethod.invoke(null, object);
            } else {
                return (String) encodeMethod.invoke(object);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.prettyPrint(getType()) + " codec: " + ReflectionUtil.prettyPrint(encodeMethod) + " / " + ReflectionUtil.prettyPrint(decodeMethod);
    }
}
