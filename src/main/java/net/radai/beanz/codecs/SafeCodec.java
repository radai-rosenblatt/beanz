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

package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;

import java.lang.reflect.Type;

public class SafeCodec implements Codec {
    private final Codec delegate;
    private final boolean handleNullStrings;
    private final boolean handleNullObjects;
    private final boolean handleEmptyStrings;

    public SafeCodec(Codec delegate, boolean handleNullStrings, boolean handleNullObjects, boolean handleEmptyStrings) {
        this.delegate = delegate;
        this.handleNullStrings = handleNullStrings;
        this.handleNullObjects = handleNullObjects;
        this.handleEmptyStrings = handleEmptyStrings;
    }

    @Override
    public Type getType() {
        return delegate.getType();
    }

    @Override
    public Object decode(String encoded) {
        if (encoded == null) {
            if (handleNullStrings) {
                return null;
            }
        } else {
            if (handleEmptyStrings && encoded.isEmpty()) {
                return null;
            }
        }
        return delegate.decode(encoded);
    }

    @Override
    public String encode(Object object) {
        if (object == null && handleNullObjects) {
            return null;
        }
        return delegate.encode(object);
    }

    @Override
    public String toString() {
        return "SafeCodec around " + delegate;
    }
}
