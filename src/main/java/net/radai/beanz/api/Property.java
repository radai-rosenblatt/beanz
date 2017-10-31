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

public class Property {
    protected final PropertyDescriptor descriptor;
    protected final Bean<?> containingBean;

    public Property(PropertyDescriptor descriptor, Bean<?> containingBean) {
        if (descriptor == null || containingBean == null
                || containingBean.getDescriptor().getProperty(descriptor.getName()) != descriptor) {
            throw new IllegalArgumentException();
        }
        this.descriptor = descriptor;
        this.containingBean = containingBean;
    }

    public PropertyDescriptor getDescriptor() {
        return descriptor;
    }

    public Bean<?> getContainingBean() {
        return containingBean;
    }

    public String getName() {
        return descriptor.getName();
    }

    public PropertyType getType() {
        return descriptor.getType();
    }

    public Type getValueType() {
        return descriptor.getValueType();
    }

    public boolean isReadable() {
        return descriptor.isReadable();
    }

    public boolean isWritable() {
        return descriptor.isWritable();
    }

    public Codec getCodec() {
        return descriptor.getCodec();
    }

    public void setFromString(String value) {
        descriptor.setFromString(containingBean.getBean(), value);
    }

    public void set(Object value) {
        descriptor.set(containingBean.getBean(), value);
    }

    public Object get() {
        return descriptor.get(containingBean.getBean());
    }

    public String getAsString() {
        return descriptor.getAsString(containingBean.getBean());
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
        return containingBean + "." + descriptor.getName();
    }
}
