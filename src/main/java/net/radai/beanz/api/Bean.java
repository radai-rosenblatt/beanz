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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
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
}
