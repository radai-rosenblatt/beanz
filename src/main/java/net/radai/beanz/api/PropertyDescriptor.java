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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface PropertyDescriptor {
    BeanDescriptor getContainingBeanDescriptor();
    String getName();
    PropertyType getType();
    Type getValueType();
    boolean isReadable();
    boolean isWritable();
    Object get(Object bean);
    default String getAsString(Object bean) {
        Codec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Object rawValue = get(bean);
        return rawValue != null ? codec.encode(rawValue) : null;
    }
    void set(Object bean, Object value);
    default void setFromString(Object bean, String strValue) {
        Codec codec = getCodec();
        if (codec == null) {
            throw new IllegalStateException();
        }
        Object value = strValue != null ? codec.decode(strValue) : null;
        set(bean, value);
    }
    default Codec getCodec() {
        return getContainingBeanDescriptor().getCodec(getValueType());
    }
    <A extends Annotation> A[] getAnnotations(Class<A> annotationClass);
}
