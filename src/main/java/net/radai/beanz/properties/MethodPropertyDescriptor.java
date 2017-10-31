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
import net.radai.beanz.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public abstract class MethodPropertyDescriptor extends PropertyDescriptorBase {
    private final Method getter;
    private final Method setter;

    public MethodPropertyDescriptor(BeanDescriptor containingBeanDescriptor, String name, Type type, Method getter, Method setter) {
        super(containingBeanDescriptor, name, type);
        if (getter == null && setter == null) {
            throw new IllegalArgumentException();
        }
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public boolean isReadable() {
        return getter != null;
    }

    @Override
    public boolean isWritable() {
        return setter != null;
    }

    @Override
    public Object get(Object bean) {
        if (!isReadable()) {
            throw new IllegalStateException();
        }
        try {
            return getter.invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            //todo - support using private methods
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void set(Object bean, Object value) {
        if (!isWritable()) {
            throw new IllegalStateException();
        }
        try {
            setter.invoke(bean, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            //todo - support using private methods
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <A extends Annotation> A[] getAnnotations(Class<A> annotationClass) {
        if (getter == null) {
            return null;
        }
        return getter.getAnnotationsByType(annotationClass);
    }

    @Override
    public String toString() {
        String typeName = ReflectionUtil.prettyPrint(getValueType());
        String result = typeName + " " + getName() + ": ";
        if (getter != null) {
            result += getter.getName() + "()";
        } else {
            result += "-";
        }
        result += " / ";
        if (setter != null) {
            result += setter.getName() + "(" + getName() + ")";
        } else {
            result += "-";
        }
        return result;
    }
}
