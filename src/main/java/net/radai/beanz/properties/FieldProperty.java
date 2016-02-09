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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public abstract class FieldProperty extends PropertyBase {
    private final Field field;

    public FieldProperty(BeanDescriptor containingBeanDescriptor, String name, Type type, Field field) {
        super(containingBeanDescriptor, name, type);
        this.field = field;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return ReflectionUtil.isFinal(field);
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
