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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Bean<T> {
    private final BeanDescriptor descriptor;
    private final T bean;
    private final Map<String, Property> properties;

    public Bean(BeanDescriptor descriptor, T bean) {
        this.descriptor = descriptor;
        this.bean = bean;
        Map<String, Property> temp = new HashMap<>();
        for (Map.Entry<String, PropertyDescriptor> propEntry : descriptor.getProperties().entrySet()) {
            String name = propEntry.getKey();
            PropertyDescriptor desc = propEntry.getValue();
            switch (desc.getType()) {
                case SIMPLE:
                    temp.put(name, new Property(desc, this));
                    break;
                case ARRAY:
                    temp.put(name, new ArrayProperty((ArrayPropertyDescriptor) desc, this));
                    break;
                case COLLECTION:
                    temp.put(name, new CollectionProperty((CollectionPropertyDescriptor) desc, this));
                    break;
                case MAP:
                    temp.put(name, new MapProperty((MapPropertyDescriptor) desc, this));
                    break;
                default:
                    throw new IllegalStateException("unhandled: " + desc.getType());
            }
        }
        this.properties = Collections.unmodifiableMap(temp);
    }

    public BeanDescriptor getDescriptor() {
        return descriptor;
    }

    public T getBean() {
        return bean;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public Property getProperty(String propName) {
        return properties.get(propName);
    }

    public <A extends Annotation> A[] getAnnotations(Class<A> annotationClass) {
        return descriptor.getAnnotations(annotationClass);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        A[] annotations = getAnnotations(annotationClass);
        if (annotations == null || annotations.length == 0) {
            return null;
        }
        if (annotations.length > 1) {
            throw new IllegalStateException("found " + annotations.length + " annotations of type " + annotationClass + ", but only one requested");
        }
        return annotations[0];
    }

    @Override
    public String toString() {
        return descriptor + "@" + Integer.toHexString(System.identityHashCode(bean));
    }
}
