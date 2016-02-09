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

package net.radai.beanz.util;

import net.radai.beanz.api.PropertyType;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Radai Rosenblatt
 */
public class ReflectionUtil {
    public static Class erase(Type type) {
        if (type instanceof GenericArrayType) {
            return Object.class; //TODO - get more info?
        }
        if (type instanceof WildcardType) {
            throw new UnsupportedOperationException();
        }
        if (type instanceof TypeVariable) {
            throw new UnsupportedOperationException();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class) parameterizedType.getRawType();
        }
        return (Class) type;
    }

    public static Object instantiate(Type type) {
        Class erased = erase(type);
        if (Collection.class.isAssignableFrom(erased)) {
            return instantiateCollection(erased);
        }
        try {
            return erased.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Collection instantiateCollection (Class collectionClass) {
        if (!Collection.class.isAssignableFrom(collectionClass)) {
            throw new IllegalArgumentException();
        }
        if (!Modifier.isAbstract(collectionClass.getModifiers())) {
            try {
                //noinspection unchecked
                return (Collection<?>) collectionClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        if (List.class.isAssignableFrom(collectionClass)) {
            return new ArrayList<>();
        }
        if (Set.class.isAssignableFrom(collectionClass)) {
            return new HashSet<>();
        }
        throw new UnsupportedOperationException();
    }

    public static Map<?, ?> instantiateMap(Class mapClass) {
        if (!Map.class.isAssignableFrom(mapClass)) {
            throw new IllegalArgumentException();
        }
        if (!Modifier.isAbstract(mapClass.getModifiers())) {
            try {
                //noinspection unchecked
                return (Map<?, ?>) mapClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return new HashMap<>();
    }

    public static boolean isArray(Type type) {
        if (type instanceof ParameterizedType) {
            return false;
        }
        if (type instanceof Class<?>) {
            Class clazz = (Class) type;
            return clazz.isArray();
        }
        if (type instanceof GenericArrayType) {
            return true;
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isCollection(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return Collection.class.isAssignableFrom((Class<?>) parameterizedType.getRawType());
        }
        if (type instanceof Class<?>) {
            Class clazz = (Class) type;
            return Collection.class.isAssignableFrom(clazz);
        }
        if (type instanceof GenericArrayType) {
            return false;
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isMap(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType());
        }
        if (type instanceof Class<?>) {
            Class clazz = (Class) type;
            return Map.class.isAssignableFrom(clazz);
        }
        if (type instanceof GenericArrayType) {
            return false;
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isEnum(Type type) {
        if (type instanceof ParameterizedType) {
            return false; //no such thing as Enum<Something>
        }
        if (type instanceof Class<?>) {
            Class clazz = (Class) type;
            return clazz.isEnum();
        }
        if (type instanceof GenericArrayType) {
            return false;
        }
        throw new UnsupportedOperationException();
    }

    public static PropertyType typeOf(Type type) {
        if (isArray(type)) {
            return PropertyType.ARRAY;
        }
        if (isCollection(type)) {
            return PropertyType.COLLECTION;
        }
        if (isMap(type)) {
            return PropertyType.MAP;
        }
        return PropertyType.SIMPLE;
    }

    public static Type getElementType(Type type) {
        if (isArray(type)) {
            if (type instanceof Class<?>) {
                Class clazz = (Class) type;
                return clazz.getComponentType();
            }
            if (type instanceof GenericArrayType) {
                GenericArrayType arrayType = (GenericArrayType) type;
                return arrayType.getGenericComponentType();
            }
            throw new UnsupportedOperationException();
        }
        if (isCollection(type)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length != 1) {
                    throw new UnsupportedOperationException();
                }
                return typeArguments[0];
            }
            if (type instanceof Class<?>) {
                //this is something like a plain List(), no generic information, so type unknown
                return null;
            }
            throw new UnsupportedOperationException();
        }
        if (isMap(type)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length != 2) {
                    throw new UnsupportedOperationException();
                }
                return typeArguments[1];
            }
            if (type instanceof Class<?>) {
                //this is something like a plain Map(), no generic information, so type unknown
                return null;
            }
        }
        throw new UnsupportedOperationException();
    }

    public static Type getKeyType(Type type) {
        if (isMap(type)) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length != 2) {
                    throw new UnsupportedOperationException();
                }
                return typeArguments[0];
            }
            if (type instanceof Class<?>) {
                //this is something like a plain Map(), no generic information, so type unknown
                return null;
            }
        }
        throw new UnsupportedOperationException();
    }

    public static boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }

    public static boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public static boolean isFinal(Method method) {
        return Modifier.isFinal(method.getModifiers());
    }

    public static boolean isFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    public static boolean isAssignable(Type from, Type to) {
        //TODO - be smarter about generics (List<String> is not really assignable to List<Integer> ...)
        return ClassUtils.isAssignable(erase(from), erase(to), true);
    }

    public static boolean isGetter(Method method) {
        Type returnType = method.getGenericReturnType();
        if (returnType.equals(void.class)) {
            return false; //should return something
        }
        Type[] argumentTypes = method.getGenericParameterTypes();
        if (argumentTypes != null && argumentTypes.length != 0) {
            return false; //should not accept any arguments
        }
        String name = method.getName();
        if (name.startsWith("get")) {
            if (name.length() < 4) {
                return false; //has to be getSomething
            }
            String fourthChar = name.substring(3, 4);
            return fourthChar.toUpperCase(Locale.ROOT).equals(fourthChar); //getSomething (upper case)
        } else if (name.startsWith("is")) {
            if (name.length() < 3) {
                return false; //isSomething
            }
            String thirdChar = name.substring(2, 3);
            //noinspection SimplifiableIfStatement
            if (!thirdChar.toUpperCase(Locale.ROOT).equals(thirdChar)) {
                return false; //has to start with uppercase (or something that uppercases to itself, like a number?)
            }
            return returnType.equals(boolean.class) || returnType.equals(Boolean.class);
        } else {
            return false;
        }
    }

    public static boolean isSetter(Method method) {
        Type returnType = method.getGenericReturnType();
        if (!returnType.equals(void.class)) {
            return false; //should not return anything
        }
        Type[] argumentTypes = method.getGenericParameterTypes();
        if (argumentTypes == null || argumentTypes.length != 1) {
            return false; //should accept exactly one argument
        }
        String name = method.getName();
        if (name.startsWith("set")) {
            if (name.length() < 4) {
                return false; //setSomething
            }
            String fourthChar = name.substring(3, 4);
            return fourthChar.toUpperCase(Locale.ROOT).equals(fourthChar); //setSomething (upper case)
        }
        return false;
    }

    public static String propNameFrom(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            if (methodName.length() > 4) {
                return methodName.substring(3, 4).toLowerCase(Locale.ROOT) + methodName.substring(4);
            } else {
                return methodName.substring(3, 4).toLowerCase(Locale.ROOT);
            }
        }
        if (methodName.startsWith("is")) {
            if (methodName.length() > 3) {
                return methodName.substring(2, 3).toLowerCase(Locale.ROOT) + methodName.substring(3);
            } else {
                return methodName.substring(2, 3).toLowerCase(Locale.ROOT);
            }
        }
        throw new IllegalArgumentException("method name " + methodName + " does not contain a property name");
    }

    public static Method findSetter(Class clazz, String propName) {
        String expectedName = "set" + propName.substring(0, 1).toUpperCase(Locale.ROOT) + propName.substring(1);
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(expectedName)) {
                continue;
            }
            if (!method.getReturnType().equals(void.class)) {
                continue;
            }
            Type[] argTypes = method.getGenericParameterTypes();
            if (argTypes == null || argTypes.length != 1) {
                continue;
            }
            return method;
        }
        return null;
    }

    public static Method findGetter(Class clazz, String propName) {
        Set<String> expectedNames = new HashSet<>(Arrays.asList(
                "get" + propName.substring(0, 1).toUpperCase(Locale.ROOT) + propName.substring(1),
                "is" + propName.substring(0, 1).toUpperCase(Locale.ROOT) + propName.substring(1) //bool props
        ));
        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();
            if (!expectedNames.contains(methodName)) {
                continue;
            }
            if (method.getParameterCount() != 0) {
                continue; //getters take no arguments
            }
            Type returnType = method.getGenericReturnType();
            if (returnType.equals(void.class)) {
                continue; //getters return something
            }
            if (methodName.startsWith("is") && !(returnType.equals(Boolean.class) || returnType.equals(boolean.class))) {
                continue; //isSomething() only valid for booleans
            }
            return method;
        }
        return null;
    }

    public static Field findField(Class clazz, String propName) {
        Class c = clazz;
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (!f.getName().equals(propName)) {
                    continue;
                }
                int modifiers = f.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                return f;
            }
            c = c.getSuperclass();
        }
        return null;
    }

    public static String prettyPrint(Type type) {
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return prettyPrint(genericArrayType.getGenericComponentType()) + "[]";
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return wildcardType.toString();
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return typeVariable.getName();
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            StringBuilder sb = new StringBuilder();
            sb.append(prettyPrint(parameterizedType.getRawType()));
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments != null && typeArguments.length > 0) {
                sb.append("<");
                for (Type typeArgument : typeArguments) {
                    sb.append(prettyPrint(typeArgument)).append(", ");
                }
                sb.delete(sb.length()-2, sb.length()); // last ", "
                sb.append(">");
            }
            return sb.toString();
        }
        Class clazz = (Class) type;
        if (clazz.isArray()) {
            return prettyPrint(clazz.getComponentType()) + "[]";
        }
        return clazz.getSimpleName();
    }

    public static String prettyPrint(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> declaredOn = method.getDeclaringClass();
        sb.append(prettyPrint(declaredOn)).append(".").append(method.getName()).append("(");
        if (method.getParameterCount() > 0) {
            for (Type paramType : method.getGenericParameterTypes()) {
                sb.append(prettyPrint(paramType)).append(", ");
            }
            sb.delete(sb.length()-2, sb.length());
        }
        sb.append(")");
        return sb.toString();
    }
}
