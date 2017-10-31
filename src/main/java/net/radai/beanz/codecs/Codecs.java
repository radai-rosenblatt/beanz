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
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public static final Codec DATE_CODEC = new Codec() {
        private final SimpleDateFormat timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS Z");
        private final SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");

        {
            timestamp.setTimeZone(TimeZone.getTimeZone("GMT"));
            date.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        @Override
        public Type getType() {
            return Date.class;
        }

        @Override
        public Object decode(String encoded) {
            if (encoded == null || encoded.isEmpty()) {
                return null;
            }
            try {
                return timestamp.parse(encoded);
            } catch (ParseException e) {
                try {
                    return date.parse(encoded);
                } catch (ParseException e2) {
                    throw new IllegalArgumentException(e2);
                }
            }
        }

        @Override
        public String encode(Object object) {
            if (object == null) {
                return null;
            }
            Date date = (Date) object;
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            calendar.setTime(date);
            if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0
                && calendar.get(Calendar.SECOND) == 0 && calendar.get(Calendar.MILLISECOND) == 0) {
                return this.date.format(date);
            } else {
                return timestamp.format(date);
            }
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
        BUILT_INS.put(Date.class,      DATE_CODEC);
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
