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

package net.radai.beanz.properties;

import net.radai.beanz.api.BeanDescriptor;
import net.radai.beanz.api.PropertyDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public abstract class CompositePropertyDescriptor extends PropertyDescriptorBase {
    private final PropertyDescriptor[] delegates;

    public CompositePropertyDescriptor(BeanDescriptor containingBeanDescriptor, String name, Type type, PropertyDescriptor[] delegates) {
        super(containingBeanDescriptor, name, type);
        if (delegates == null || delegates.length < 2) {
            throw new IllegalArgumentException();
        }
        for (PropertyDescriptor delegate : delegates) {
            if (!delegate.getType().equals(getType())) {
                throw new IllegalArgumentException();
            }
        }
        this.delegates = delegates;
    }

    @Override
    public boolean isReadable() {
        for (PropertyDescriptor delegate : delegates) {
            if (delegate.isReadable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isWritable() {
        for (PropertyDescriptor delegate : delegates) {
            if (delegate.isWritable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object bean) {
        for (PropertyDescriptor delegate : delegates) {
            if (delegate.isReadable()) {
                return delegate.get(bean);
            }
        }
        throw new IllegalStateException(); //not readable
    }

    @Override
    public void set(Object bean, Object value) {
        for (PropertyDescriptor delegate : delegates) {
            if (delegate.isWritable()) {
                delegate.set(bean, value);
                return;
            }
        }
        throw new IllegalStateException(); //not writable
    }

    @Override
    public <A extends Annotation> A[] getAnnotations(Class<A> annotationClass) {
        A[] results = null;
        for (PropertyDescriptor delegate : delegates) {
            if ((results = delegate.getAnnotations(annotationClass)) != null) {
                break;
            }
        }
        return results;
    }
}
