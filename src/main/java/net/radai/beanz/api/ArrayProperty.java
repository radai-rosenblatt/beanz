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

import net.radai.beanz.codecs.ArrayCodec;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * Created by Radai Rosenblatt
 */
public class ArrayProperty extends Property {

    public ArrayProperty(ArrayPropertyDescriptor descriptor, Bean<?> containingBean) {
        super(descriptor, containingBean);
    }

    @Override
    public ArrayPropertyDescriptor getDescriptor() {
        return (ArrayPropertyDescriptor) super.getDescriptor();
    }

    public Type getElementType() {
        return getDescriptor().getElementType();
    }

    @Override
    public ArrayCodec getCodec() {
        return (ArrayCodec) super.getCodec();
    }

    public void setArray(Collection<?> values) {
        getDescriptor().setArray(containingBean.getBean(), values);
    }

    public void setFromStrings(Collection<String> strValues) {
        getDescriptor().setFromStrings(containingBean.getBean(), strValues);
    }

    public List<?> getAsList() {
        return getDescriptor().getAsList(containingBean.getBean());
    }

    public List<String> getAsStrings() {
        return getDescriptor().getAsStrings(containingBean.getBean());
    }
}
