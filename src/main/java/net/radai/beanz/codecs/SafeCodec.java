package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;

import java.lang.reflect.Type;

/**
 * Created by Radai Rosenblatt
 */
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
