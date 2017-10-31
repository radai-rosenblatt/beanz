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

package net.radai.beanz.api;

import net.radai.beanz.codecs.MapCodec;

import java.lang.reflect.Type;
import java.util.Map;

public class MapProperty extends Property {

    public MapProperty(MapPropertyDescriptor descriptor, Bean<?> containingBean) {
        super(descriptor, containingBean);
    }

    @Override
    public MapPropertyDescriptor getDescriptor() {
        return (MapPropertyDescriptor) super.getDescriptor();
    }

    public Type getKeyType() {
        return getDescriptor().getKeyType();
    }

    public Type getElementType() {
        return getDescriptor().getElementType();
    }

    @Override
    public MapCodec getCodec() {
        return (MapCodec) super.getCodec();
    }

    public void setFromStrings(Map<String, String> strValues) {
        getDescriptor().setFromStrings(containingBean.getBean(), strValues);
    }

    public Map<?, ?> getMap() {
        return getDescriptor().getMap(containingBean.getBean());
    }

    public Map<String, String> getAsStrings() {
        return getDescriptor().getAsStrings(containingBean.getBean());
    }
}
