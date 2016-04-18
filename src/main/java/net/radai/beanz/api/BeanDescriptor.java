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

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static net.radai.beanz.util.ReflectionUtil.erase;

/**
 * Created by Radai Rosenblatt
 */
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

    @Override
    public String toString() {
        return beanClass.getSimpleName();
    }
}
