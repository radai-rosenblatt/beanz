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

import org.apache.commons.lang3.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static net.radai.beanz.util.ReflectionUtil.erase;

public class BeanDescriptor {
    private final Class<?> beanClass;
    private final Map<String, PropertyDescriptor> properties;
    private final Map<Type, Codec> codecs;

    public BeanDescriptor(Class<?> beanClass) {
        this.beanClass = beanClass;
        this.properties = new HashMap<>();
        this.codecs = new HashMap<>();
    }

    public void addProperty(PropertyDescriptor prop) {
        String name = prop != null ? prop.getName() : null;
        if (prop == null || name == null || name.isEmpty() || properties.containsKey(name)) {
            throw new IllegalArgumentException();
        }
        properties.put(name, prop);
    }

    public PropertyDescriptor getProperty(String propName) {
        return properties.get(propName);
    }

    public Map<String, PropertyDescriptor> getProperties() {
        return properties;
    }

    public void addCodec(Type type, Codec codec) {
        if (codec == null || type == null || !ClassUtils.isAssignable(erase(codec.getType()), erase(type), true) || codecs.containsKey(type)) {
            throw new IllegalArgumentException();
        }
        codecs.put(type, codec);
    }

    public Codec getCodec(Type type) {
        return codecs.get(type);
    }

    public <A extends Annotation> A[] getAnnotations(Class<A> annotationClass) {
        return beanClass.getAnnotationsByType(annotationClass);
    }

    @Override
    public String toString() {
        return beanClass.getSimpleName();
    }
}
