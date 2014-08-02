package com.kciray.commons.lang;

import java.lang.reflect.Field;

public class ObjectUtils {
    public static String readFields(Object obj) {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(obj.getClass().getName());
        result.append(" Object {");
        result.append(newLine);

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);//Read private fields
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                result.append(field.get(obj));
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }
}
