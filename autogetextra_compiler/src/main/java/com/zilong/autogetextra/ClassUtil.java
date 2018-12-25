package com.zilong.autogetextra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public class ClassUtil {

    //类型
    public static TypeName getTypeName(String TypeName) {
        return getClassName(TypeName).withoutAnnotations();
    }


    //类型的名称，返回ClassName
    //如果事完整报名，拆分
    public static ClassName getClassName(String className) {

        // 基础类型描述符
        if (className.indexOf(".") <= 0) {
            switch (className) {
                case "byte":
                    return ClassName.get("java.lang", "Byte");
                case "short":
                    return ClassName.get("java.lang", "Short");
                case "int":
                    return ClassName.get("java.lang", "Integer");
                case "long":
                    return ClassName.get("java.lang", "Long");
                case "float":
                    return ClassName.get("java.lang", "Float");
                case "double":
                    return ClassName.get("java.lang", "Double");
                case "boolean":
                    return ClassName.get("java.lang", "Boolean");
                case "char":
                    return ClassName.get("java.lang", "Character");
                default:
            }
        }

        // 手动解析 例如java.lang.String，分成java.lang的包名和String的类名
        String packageD = className.substring(0, className.lastIndexOf('.'));
        String name = className.substring(className.lastIndexOf('.') + 1);
        return ClassName.get(packageD, name);
    }
}
