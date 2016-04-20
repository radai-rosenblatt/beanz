/*
 * Copyright (c) 2016 Radai Rosenblatt.
 * This file is part of Beanz.
 *
 * Beanz is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beanz is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beanz.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.radai.beanz.properties;

import net.radai.beanz.api.BeanDescriptor;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
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
