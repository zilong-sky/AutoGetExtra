package com.zilong.autogetextra;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

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


    public static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }
}
