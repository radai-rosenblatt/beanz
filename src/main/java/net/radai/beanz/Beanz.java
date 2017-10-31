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

package net.radai.beanz;

import net.radai.beanz.api.*;
import net.radai.beanz.codecs.*;
import net.radai.beanz.properties.*;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

public class Beanz {
    public static final Set<String> DEFAULT_IGNORE = Collections.unmodifiableSet(new HashSet<>(Collections.singletonList("class")));

    /**
     * wraps around an existing bean instance
     * @param instance existing bean instance
     * @return a Bean wrapper around the given instance
     */
    public static <T> Bean<T> wrap(T instance) {
        if (instance == null || instance instanceof Class) {
            throw new IllegalArgumentException();
        }
        BeanDescriptor descriptor = parse(instance.getClass());
        return new Bean<>(descriptor, instance);
    }

    /**
     * creates a new Bean wrapper around a newly-created instance of
     * the given class
     * @param clazz class to create and wrap an instance of
     * @return a bean wrapper around an instance of the given class
     */
    public static <T> Bean<T> create(Class<T> clazz) {
        try {
            return wrap(clazz.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("unable to instantiate class " + clazz, e);
        }
    }

    public static BeanDescriptor parse(Object instance) {
        return parse(instance.getClass());
    }

    public static BeanDescriptor parse(Class<?> clazz) {
        return parse(clazz, DEFAULT_IGNORE);
    }

    public static BeanDescriptor parse(Class<?> clazz, Set<String> ignore) {
        if (clazz == null) {
            throw new IllegalArgumentException("got null class");
        }
        if (ignore == null) {
            ignore = Collections.emptySet();
        }
        BeanDescriptor bean = new BeanDescriptor(clazz);

        Map<String, PropertyDescriptor> properties = new HashMap<>();
        Map<Type, Codec> codecs = new HashMap<>(Codecs.BUILT_INS);

        //methods 1st
        for (Method method : clazz.getMethods()) {
            if (ReflectionUtil.isGetter(method) || ReflectionUtil.isSetter(method)) {
                String propName = ReflectionUtil.propNameFrom(method);
                if (!properties.containsKey(propName) && !ignore.contains(propName)) {
                    try {
                        properties.put(propName, resolve(bean, clazz, propName, codecs));
                    } catch (AmbiguousPropertyException e) {
                        //not a property
                    }
                }
            }
        }

        //fields later
        Class<?> c = clazz;
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                int modifiers = f.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                String fieldName = f.getName();
                if (properties.containsKey(fieldName) || ignore.contains(fieldName)) {
                    continue;
                }
                try {
                    properties.put(fieldName, resolve(bean, clazz, fieldName, codecs));
                } catch (AmbiguousPropertyException e) {
                    //not a property
                }
            }
            c = c.getSuperclass();
        }

        properties.forEach((s, property) -> bean.addProperty(property));
        codecs.forEach(bean::addCodec);
        return bean;
    }

    private static PropertyDescriptor resolve(BeanDescriptor bean, Class<?> clazz, String propName, Map<Type, Codec> codecs) {
        //look for a getter/setter pair
        Method getter = ReflectionUtil.findGetter(clazz, propName);
        Method setter = ReflectionUtil.findSetter(clazz, propName);
        Field field = ReflectionUtil.findField(clazz, propName);
        if (getter == null && setter == null && field == null) {
            return null;
        }

        Type type = resolvePropertyType(clazz, propName, getter, setter, field);
        PropertyType propertyType = ReflectionUtil.typeOf(type);
        Codec codec = resolvePropertyCodec(clazz, propName, type, getter, setter, field, codecs);

        if (getter != null && setter != null) { //getter + setter
            return buildMethodProperty(bean, propName, getter, setter, type, propertyType);
        }
        if (getter == null && setter == null) { //just a field
            return buildFieldProperty(bean, propName, field, type, propertyType);
        }
        //we have either a getter or a setter
        MethodPropertyDescriptor methodProperty = buildMethodProperty(bean, propName, getter, setter, type, propertyType);
        if (field == null) {
            //and no field
            return methodProperty; //one of them is != null;
        }
        FieldPropertyDescriptor fieldProperty = buildFieldProperty(bean, propName, field, type, propertyType);

        return buildCompositeProperty(bean, propName, type, propertyType, methodProperty, fieldProperty);
    }

    private static MethodPropertyDescriptor buildMethodProperty(BeanDescriptor bean, String propName, Method getter, Method setter, Type type, PropertyType propertyType) {
        switch (propertyType) {
            case SIMPLE:
                return new SimpleMethodPropertyDescriptor(bean, propName, type, getter, setter);
            case ARRAY:
                return new ArrayMethodPropertyDescriptor(bean, propName, type, getter, setter);
            case COLLECTION:
                return new CollectionMethodPropertyDescriptor(bean, propName, type, getter, setter);
            case MAP:
                return new MapMethodPropertyDescriptor(bean, propName, type, getter, setter);
            default:
                throw new IllegalStateException("unhandled " + propertyType);
        }
    }

    private static FieldPropertyDescriptor buildFieldProperty(BeanDescriptor bean, String propName, Field field, Type type, PropertyType propertyType) {
        switch (propertyType) {
            case SIMPLE:
                return new SimpleFieldPropertyDescriptor(bean, propName, type, field);
            case ARRAY:
                return new ArrayFieldPropertyDescriptor(bean, propName, type, field);
            case COLLECTION:
                return new CollectionFieldPropertyDescriptor(bean, propName, type, field);
            case MAP:
                return new MapFieldPropertyDescriptor(bean, propName, type, field);
            default:
                throw new IllegalStateException("unhandled " + propertyType);
        }
    }

    private static CompositePropertyDescriptor buildCompositeProperty(BeanDescriptor bean, String propName, Type type, PropertyType propertyType, PropertyDescriptor... delegates) {
        switch (propertyType) {
            case SIMPLE:
                return new SimpleCompositePropertyDescriptor(bean, propName, type, delegates);
            case ARRAY:
                return new ArrayCompositePropertyDescriptor(bean, propName, type, delegates);
            case COLLECTION:
                return new CollectionCompositePropertyDescriptor(bean, propName, type, delegates);
            case MAP:
                return new MapCompositePropertyDescriptor(bean, propName, type, delegates);
            default:
                throw new IllegalStateException("unhandled " + propertyType);
        }
    }

    private static Type resolvePropertyType(Class<?> clazz, String propName, Method getter, Method setter, Field field) {
        Type getterType = getter != null ? getter.getGenericReturnType() : null;
        Type setterType = setter != null ? setter.getGenericParameterTypes()[0] : null;
        Type fieldType = field != null ? field.getGenericType() : null;

        if (getter != null && setter != null) {
            if (!getterType.equals(setterType)) {
                //TODO - be smart about boxing?
                //TODO - also be smart about missing generics? (List vs List<Something>)
                throw new AmbiguousPropertyException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter is " + getter + " while setter is " + setter);
            }
            return getterType;
        }
        if (getter == null && setter == null) {
            return fieldType;
        }
        //we have just one method
        Type methodType = getterType != null ? getterType : setterType;
        if (field == null) {
            return methodType;
        }
        if (!fieldType.equals(methodType)) {
            //TODO - be smart about boxing?
            throw new AmbiguousPropertyException("ambiguous property " + clazz.getSimpleName() + "." + propName + ": getter/setter type is " + methodType + " while field type is " + fieldType);
        }
        return methodType;
    }

    private static Codec resolvePropertyCodec (
            Class<?> clazz, String propName, Type type,
            Method getter, Method setter, Field field,
            Map<Type, Codec> codecs) {
        //TODO - check for overrides in annotations on methods > field > class > type
        Codec forType = codecs.get(type);
        if (forType != null) {
            return forType;
        }
        Codec result = null;
        if (ReflectionUtil.isArray(type) || ReflectionUtil.isCollection(type)) {
            Type elementType = ReflectionUtil.getElementType(type);
            Codec elementCodec = resolvePropertyCodec(null, null, elementType, null, null, null, codecs);
            if (elementCodec == null) {
                return null; //cant handle the elements == cant handle the collection/array
            }
            if (ReflectionUtil.isArray(type)) {
                result = new ArrayCodec(type, elementType, elementCodec);
            } else {
                result = new CollectionCodec(type, elementType, elementCodec);
            }
        } else if (ReflectionUtil.isMap(type)) {
            Type keyType = ReflectionUtil.getKeyType(type);
            Type valueType = ReflectionUtil.getElementType(type);
            Codec keyCodec = resolvePropertyCodec(null, null, keyType, null, null, null, codecs);
            Codec valueCodec = resolvePropertyCodec(null, null, valueType, null, null, null, codecs);
            if (keyCodec == null || valueCodec == null) {
                return null;
            }
            result = new MapCodec(type, keyType, valueType, keyCodec, valueCodec);
        } else if (ReflectionUtil.isEnum(type)) {
            Class<?> erased = ReflectionUtil.erase(type);
            try {
                Method encodeMethod = erased.getMethod("name");
                Method decodeMethod = erased.getMethod("valueOf", String.class);
                result = new SimpleCodec(type, encodeMethod, decodeMethod);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("while preparing codec for enum " + type, e);
            }
        } else {
            //see if this type has toString + valueOf(String) defined DIRECTLY on the type itself
            Class<?> erased = ReflectionUtil.erase(type);
            Method encodeMethod = null;
            Method decodeMethod = null;
            for (Method method : erased.getMethods()) {
                int modifiers = method.getModifiers();
                Type[] argumentTypes = method.getGenericParameterTypes();
                String methodName = method.getName();
                if ("toString".equals(methodName)
                        && !Modifier.isStatic(modifiers)
                        && method.getParameterCount() == 0
                        && method.getGenericReturnType().equals(String.class)
                        && method.getDeclaringClass() == erased) {
                    //public String toString() defined directly on the target type (not inherited)
                    if (encodeMethod != null) {
                        throw new IllegalStateException();
                    }
                    encodeMethod = method;
                    continue;
                }
                if (("fromString".equals(methodName) || "valueOf".equals(methodName))
                        && Modifier.isStatic(modifiers)
                        && method.getParameterCount() == 1
                        && String.class.equals(argumentTypes[0])
                        && method.getDeclaringClass() == erased) {
                    if (decodeMethod != null) {
                        throw new IllegalStateException();
                    }
                    decodeMethod = method;
                    //noinspection UnnecessaryContinue
                    continue;
                }
            }
            if (encodeMethod != null && decodeMethod != null) {
                result = new SimpleCodec(type, encodeMethod, decodeMethod);
            }
        }
        if (result != null) {
            codecs.put(type, result);
        }
        return result;
    }
}
