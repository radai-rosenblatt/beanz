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

package net.radai.beanz;

import net.radai.beanz.api.*;
import net.radai.beanz.codecs.*;
import net.radai.beanz.properties.*;
import net.radai.beanz.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Radai Rosenblatt
 */
public class Beanz {

    public static BeanDescriptor parse(Object instance) {
        return parse(instance.getClass());
    }

    public static BeanDescriptor parse(Class clazz) {
        BeanDescriptor bean = new BeanDescriptor();

        Map<String, Property> properties = new HashMap<>();
        Map<Type, Codec> codecs = new HashMap<>(Codecs.BUILT_INS);

        //methods 1st
        for (Method method : clazz.getMethods()) {
            if (ReflectionUtil.isGetter(method) || ReflectionUtil.isSetter(method)) {
                String propName = ReflectionUtil.propNameFrom(method);
                if (!properties.containsKey(propName)) {
                    try {
                        properties.put(propName, resolve(bean, clazz, propName, codecs));
                    } catch (AmbiguousPropertyException e) {
                        //not a property
                    }
                }
            }
        }

        //fields later
        Class c = clazz;
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                int modifiers = f.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                String fieldName = f.getName();
                if (properties.containsKey(fieldName)) {
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

    private static Property resolve(BeanDescriptor bean, Class clazz, String propName, Map<Type, Codec> codecs) {
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
        MethodProperty methodProperty = buildMethodProperty(bean, propName, getter, setter, type, propertyType);
        if (field == null) {
            //and no field
            return methodProperty; //one of them is != null;
        }
        FieldProperty fieldProperty = buildFieldProperty(bean, propName, field, type, propertyType);

        return buildCompositeProperty(bean, propName, type, propertyType, methodProperty, fieldProperty);
    }

    private static MethodProperty buildMethodProperty(BeanDescriptor bean, String propName, Method getter, Method setter, Type type, PropertyType propertyType) {
        switch (propertyType) {
            case SIMPLE:
                return new SimpleMethodProperty(bean, propName, type, getter, setter);
            case ARRAY:
                return new ArrayMethodProperty(bean, propName, type, getter, setter);
            case COLLECTION:
                return new CollectionMethodProperty(bean, propName, type, getter, setter);
            case MAP:
                return new MapMethodProperty(bean, propName, type, getter, setter);
            default:
                throw new IllegalStateException("unhandled " + propertyType);
        }
    }

    private static FieldProperty buildFieldProperty(BeanDescriptor bean, String propName, Field field, Type type, PropertyType propertyType) {
        switch (propertyType) {
            case SIMPLE:
                return new SimpleFieldProperty(bean, propName, type, field);
            case ARRAY:
                return new ArrayFieldProperty(bean, propName, type, field);
            case COLLECTION:
                return new CollectionFieldProperty(bean, propName, type, field);
            case MAP:
                return new MapFieldProperty(bean, propName, type, field);
            default:
                throw new IllegalStateException("unhandled " + propertyType);
        }
    }

    private static CompositeProperty buildCompositeProperty(BeanDescriptor bean, String propName, Type type, PropertyType propertyType, Property ... delegates) {
        switch (propertyType) {
            case SIMPLE:
                return new SimpleCompositeProperty(bean, propName, type, delegates);
            case ARRAY:
                return new ArrayCompositeProperty(bean, propName, type, delegates);
            case COLLECTION:
                return new CollectionCompositeProperty(bean, propName, type, delegates);
            case MAP:
                return new MapCompositeProperty(bean, propName, type, delegates);
            default:
                throw new IllegalStateException("unhandled " + propertyType);
        }
    }

    private static Type resolvePropertyType(Class clazz, String propName, Method getter, Method setter, Field field) {
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
            Class clazz, String propName, Type type,
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
        } else {
            //see if this type has toString + valueOf(String)
            Class erased = ReflectionUtil.erase(type);
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
