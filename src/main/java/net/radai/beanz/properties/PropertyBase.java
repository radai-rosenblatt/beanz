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

import net.radai.beanz.api.Bean;
import net.radai.beanz.api.Property;

import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
public abstract class PropertyBase implements Property {
    private final Bean containingBean;
    private final String name;
    private final Type type;

    public PropertyBase(Bean containingBean, String name, Type type) {
        this.containingBean = containingBean;
        this.name = name;
        this.type = type;
    }

    @Override
    public Bean getContainingBean() {
        return containingBean;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getValueType() {
        return type;
    }
}
