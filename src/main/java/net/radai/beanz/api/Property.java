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

package net.radai.beanz.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public class Property {
    protected final PropertyDescriptor descriptor;
    protected final Bean containingBean;

    public Property(PropertyDescriptor descriptor, Bean containingBean) {
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

    public Bean getContainingBean() {
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
