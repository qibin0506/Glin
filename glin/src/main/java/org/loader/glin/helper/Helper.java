package org.loader.glin.helper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by qibin on 2016/7/13.
 */

public class Helper {
    public static <T> Class<T> getType(Class<?> klass) {
        Type type = generateType(klass);

        if (type != null) {
            if (type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getRawType();
            }
            return (Class<T>) type;
        }
        return null;
    }

    public static <T> Class<T> getDeepType(Class<?> klass) {
        Type type = generateType(klass);
        if (type != null && type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return (Class<T>) parameterizedType.getActualTypeArguments()[0];
        }
        return null;
    }

    public static Type generateType(Class<?> klass) {
        Type type = klass.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type[] actualTypes = paramType.getActualTypeArguments();
            if (actualTypes != null && actualTypes.length > 0) {
                return actualTypes[0];
            }
        }

        return null;
    }
}
