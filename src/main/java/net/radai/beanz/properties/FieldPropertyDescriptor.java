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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public abstract class FieldPropertyDescriptor extends PropertyDescriptorBase {
    private final Field field;

    public FieldPropertyDescriptor(BeanDescriptor containingBeanDescriptor, String name, Type type, Field field) {
        super(containingBeanDescriptor, name, type);
        if (field == null) {
            throw new IllegalArgumentException();
        }
        this.field = field;
        if (!this.field.isAccessible()) {
            try {
                field.setAccessible(true);
            } catch (SecurityException e) {
                //TODO - log a warning
            }
        }
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return field.isAccessible() && !ReflectionUtil.isFinal(field);
    }

    @Override
    public Object get(Object bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            //todo - support using private fields
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void set(Object bean, Object value) {
        if (!isWritable()) {
            throw new IllegalStateException();
        }
        try {
            field.set(bean, value);
        }  catch (IllegalAccessException e) {
            //todo - support using private fields
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <A extends Annotation> A[] getAnnotations(Class<A> annotationClass) {
        return field.getAnnotationsByType(annotationClass);
    }

    @Override
    public String toString() {
        //todo - move most of this to ReflectionUtil.prettyPrint(field)
        String typeName = ReflectionUtil.prettyPrint(getValueType());
        int modifiers = field.getModifiers();
        String accessModifier = "";
        if (Modifier.isPrivate(modifiers)) {
            accessModifier = "private";
        } else if (Modifier.isProtected(modifiers)) {
            accessModifier = "protected";
        } else if (Modifier.isPublic(modifiers)) {
            accessModifier = "public";
        }
        if (Modifier.isFinal(modifiers)) {
            accessModifier += " final";
            accessModifier = accessModifier.trim();
        }
        return typeName + " " + getName() + ": " + accessModifier + " " + ReflectionUtil.prettyPrint(field.getGenericType()) + " " + field.getName();
    }
}
