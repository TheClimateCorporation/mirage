package com.climate.mirage;

import java.lang.reflect.Constructor;

public final class CoverageUtil {

    private CoverageUtil() {}

    public static <T> void initPrivateConstructor(Class<T> c) {
        Constructor<T> cnt;
        try {
            cnt = c.getDeclaredConstructor();
            cnt.setAccessible(true);
            cnt.newInstance();
        } catch (Exception e) {
            e.getMessage();
        }
    }

}