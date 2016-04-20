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
import net.radai.beanz.api.PropertyDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
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
