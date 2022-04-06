package com.cleanroommc.airlock.common.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Map;
import java.util.function.IntUnaryOperator;

/**
 * This utility class provides methods related to java's Reflection, MethodHandles and more.
 */
public class ReflectionUtil {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static Method classLoader$DefineClass;

    private static Map<Member, Field> modifiersFields;

    /**
     * @param className name of class, as represented via {@link Class#getName()}.
     *
     * @return whether the class is present or not.
     */
    public static boolean isClassPresent(String className) {
        return isClassPresent(className, ReflectionUtil.class.getClassLoader());
    }

    /**
     * @param className name of class, as represented via {@link Class#getName()}.
     *
     * @return whether the class is present or not in the specified classloader.
     */
    public static boolean isClassPresent(String className, ClassLoader classLoader) {
        try {
            Class.forName(className, false, classLoader);
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Checks if class is present and load it.
     *
     * @param className name of class, as represented via {@link Class#getName()}.
     *
     * @return the class that is being queried, null if it is not found.
     */
    public static Class<?> loadClassIfPresent(String className) {
        return loadClassIfPresent(className, ReflectionUtil.class.getClassLoader());
    }

    /**
     * Checks if class is present and load it with the specified classloader.
     *
     * @param className name of class, as represented via {@link Class#getName()}.
     *
     * @return the class that is being queried, null if it is not found.
     */
    @Nullable
    public static Class<?> loadClassIfPresent(String className, ClassLoader classLoader) {
        try {
            return Class.forName(className, true, classLoader);
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (ClassNotFoundException ignored) {
            return null;
        }
        return null;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, args);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MethodHandle getMethodHandle(Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = getMethod(clazz, methodName, args);
            if (method != null) {
                return LOOKUP.unreflect(method);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MethodHandle getSpecialMethodHandle(Class<?> caller, Class<?> clazz, String methodName, Class<?>... args) {
        try {
            Method method = getMethod(clazz, methodName, args);
            if (method != null) {
                return LOOKUP.unreflectSpecial(method, caller);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean setAccessible(AccessibleObject accessibleObject, boolean silent) {
        try {
            accessibleObject.setAccessible(true);
        } catch (SecurityException e) {
            if (!silent) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    public static boolean modifyModifiers(Member member, boolean silent, IntUnaryOperator function) {
        if (modifiersFields == null) {
            modifiersFields = new Reference2ObjectArrayMap<>(3);
        }
        Field modifiersField = modifiersFields.get(member);
        if (modifiersField == null) {
            // Every class implementing Member should have a modifiers Field.
            try {
                modifiersField = member.getClass().getDeclaredField("modifiers");
            } catch (NoSuchFieldException e) {
                e.printStackTrace(); // Abnormal. Printing this and disregarding the silent argument there.
                return false;
            }
            modifiersField.setAccessible(true);
            modifiersFields.put(member, modifiersField);
        }
        try {
            modifiersField.setInt(member, function.applyAsInt(member.getModifiers()));
        } catch (IllegalAccessException e) {
            if (!silent) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private ReflectionUtil() { }

}
