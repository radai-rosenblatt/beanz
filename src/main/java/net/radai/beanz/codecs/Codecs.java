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

package net.radai.beanz.codecs;

import net.radai.beanz.api.Codec;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
public class Codecs {
    public static final Codec NOP_CODEC = new Codec() {
        @Override
        public Type getType() {
            return String.class;
        }

        @Override
        public Object decode(String encoded) {
            return encoded;
        }

        @Override
        public String encode(Object object) {
            return (String) object;
        }

        @Override
        public String toString() {
            return "String codec: NOP";
        }
    };

    public static final Codec CHAR_CODEC = new Codec() {
        @Override
        public Type getType() {
            return Character.class;
        }

        @Override
        public Object decode(String encoded) {
            if (encoded == null || encoded.length() != 1) {
                throw new IllegalArgumentException();
            }
            return encoded.charAt(0);
        }

        @Override
        public String encode(Object object) {
            char c = (Character) object;
            return String.valueOf(c);
        }

        @Override
        public String toString() {
            return "Character codec : special sauce";
        }
    };

    public static Codec safe(Codec underlying) {
        if (underlying == null) {
            throw new IllegalArgumentException("passed null argument");
        }
        if (ReflectionUtil.isPrimitive(underlying.getType())) {
            throw new IllegalArgumentException("cannot make primitive codecs safe"); //because they'll get an NPE unboxing
        }
        boolean handleNullStrings = false;
        boolean handleNullObjects = false;
        boolean handleEmptyStrings = false;

        try {
            underlying.decode(null);
        } catch (Exception e) {
            handleNullStrings = true;
        }

        try {
            underlying.decode("");
        } catch (Exception e) {
            handleEmptyStrings = true;
        }

        try {
            underlying.encode(null);
        } catch (Exception e) {
            handleNullObjects = true;
        }

        if (handleNullStrings || handleNullObjects || handleEmptyStrings) {
            return new SafeCodec(underlying, handleNullStrings, handleNullObjects, handleEmptyStrings);
        } {
            return underlying; //doesnt need protection
        }
    }

    public static final Map<Type, Codec> BUILT_INS = new HashMap<>();

    static {
        //start with the primitive. since reflection (un)boxes anyway, use the wrapper codecs.
        BUILT_INS.put(boolean.class, codecFor(Boolean.class));
        BUILT_INS.put(byte.class,    codecFor(Byte.class));
        BUILT_INS.put(char.class,    CHAR_CODEC); //has to be special
        BUILT_INS.put(short.class,   codecFor(Short.class));
        BUILT_INS.put(int.class,     codecFor(Integer.class));
        BUILT_INS.put(long.class,    codecFor(Long.class));
        BUILT_INS.put(float.class,   codecFor(Float.class));
        BUILT_INS.put(double.class,  codecFor(Double.class));

        //for the wrappers, use safe versions of the above
        //(most of thd jdk built-in conversion methods dont like nulls/empties)
        BUILT_INS.put(Boolean.class,   safe(BUILT_INS.get(boolean.class)));
        BUILT_INS.put(Byte.class,      safe(BUILT_INS.get(byte.class)));
        BUILT_INS.put(Character.class, safe(BUILT_INS.get(char.class)));
        BUILT_INS.put(Short.class,     safe(BUILT_INS.get(short.class)));
        BUILT_INS.put(Integer.class,   safe(BUILT_INS.get(int.class)));
        BUILT_INS.put(Long.class,      safe(BUILT_INS.get(long.class)));
        BUILT_INS.put(Float.class,     safe(BUILT_INS.get(float.class)));
        BUILT_INS.put(Double.class,    safe(BUILT_INS.get(double.class)));
        BUILT_INS.put(String.class,    NOP_CODEC);
    }

    private static SimpleCodec codecFor(Class<?> clazz) {
        try {
            //noinspection unchecked
            return new SimpleCodec(clazz, clazz.getMethod("toString"), clazz.getMethod("valueOf", String.class));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
