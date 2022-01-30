package io.github.nbl77.lib.mock;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

public class MockProvider {
    private static final MockType[] primitiveTypeInstance = {

    };
    private static final MockType[] typeInstance = {
            new MockType(String.class.getName(), "text"),
            new MockType(byte.class.getName(), 'x'),
            new MockType(Byte.class.getName(), 'x'),
            new MockType(Integer.class.getName(), 123),
            new MockType(Boolean.class.getName(), true),
            new MockType(Long.class.getName(), 100L),
            new MockType(long.class.getName(), 100L),
            new MockType(char.class.getName(), 'a'),
            new MockType(int.class.getName(), 321),
            new MockType(boolean.class.getName(), true),
            new MockType(UUID.class.getName(), UUID.randomUUID()),
            new MockType(BigDecimal.class.getName(), BigDecimal.TEN),
            new MockType(LocalDateTime.class.getName(), LocalDateTime.now()),
            new MockType(LocalDate.class.getName(), LocalDate.now()),
            new MockType(LocalTime.class.getName(), LocalTime.now()),
            new MockType(Date.class.getName(), new Date())
    };

    public static <T> T modelProvider(Class<T> clazz) {
        try {
            if (Enum.class.isAssignableFrom(clazz)) {
                return (T) Enum.valueOf((Class<? extends Enum>) clazz, clazz.getDeclaredFields()[0].getName());
            } else {
                Field[] fields = clazz.getDeclaredFields();
                T obj = clazz.getConstructor().newInstance();
                setFields(obj, fields, clazz);
                return obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static <T, U> void setFields(T obj, Field[] fields, Class<U> clazz) throws Exception {
        for (Field field : fields) {
            field.setAccessible(true);
            field.set(obj, convertClassToLegacyClass(field, null));
        }
        if (!clazz.getSuperclass().equals(Object.class)) {
            Field[] parentFields = clazz.getSuperclass().getDeclaredFields();
            setFields(obj, parentFields, clazz.getSuperclass());
        }
    }

    static Object convertClassToLegacyClass(Field field, Class<?> clazz) throws Exception {
        if (clazz == null)
            clazz = field.getType();

        for (MockType instance : typeInstance) {
            if (clazz.getName().equals(instance.type)) {
                if (field.getName().toLowerCase().contains("phone") && instance.value instanceof String) {
                    return "081122334455";
                } else if (field.getName().toLowerCase().contains("email") && instance.value instanceof String) {
                    return (instance.value + "@email.com");
                } else if (field.getName().toLowerCase().contains("password") && instance.value instanceof String) {
                    return "numberUniq123";
                } else if (field.getName().toLowerCase().contains("url") && instance.value instanceof String) {
                    return "https://google.com";
                }
                return instance.value;
            }
            if (clazz.isInterface() && isCollections(clazz)) {
                ParameterizedType t = (ParameterizedType) field.getGenericType();
                Class<?> genericClass = (Class<?>) t.getActualTypeArguments()[0];
                return Arrays.stream(castToCollections(genericClass)).collect(Collectors.toList());
            }
        }
        return modelProvider(clazz);
    }

    static <T> boolean isCollections(Class<T> clazz) {
        return clazz.getInterfaces()[0].getName().equals(Collection.class.getName());
    }

    static Object[] castToCollections(Class<?> clazz) throws Exception {
        Object[] cList = (Object[]) Array.newInstance(clazz, 5);
        int i = 0;
        for (Object ignored : cList) {
            Array.set(cList, i, convertClassToLegacyClass(null, clazz));
            i++;
        }
        return cList;
    }
}
