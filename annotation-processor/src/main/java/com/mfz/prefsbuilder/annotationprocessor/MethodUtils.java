package com.mfz.prefsbuilder.annotationprocessor;

import com.mfz.prefsbuilder.PrefsDefVal;
import com.mfz.prefsbuilder.PrefsKeyHeadTail;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Locale;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * @author mz
 * @date 2020/05/14/Thu
 * @time 14:19
 */
public class MethodUtils {

    public static String getBoolMethodName(String filedName) {
        if (filedName.startsWith("IS_")) {
            filedName = filedName.substring(2);
        }
        return "is" + StringUtils.const2BigCamel(filedName);
    }

    public static String getGetMethodName(String filedName) {
        return "get" + StringUtils.const2BigCamel(filedName);
    }

    public static String getSetMethodName(String filedName) {
        return "set" + StringUtils.const2BigCamel(filedName);
    }

    public static String getRemoveMethodName(String filedName) {
        return "remove" + StringUtils.const2BigCamel(filedName);
    }

    public static String getContainsMethodName(String filedName) {
        return "contains" + StringUtils.const2BigCamel(filedName);
    }

    public static TypeName getType(Element element, Class<?> clazz, Elements elements) {
        return transformerClass(element, "type", clazz, elements);
    }

    public static TypeName getKeyType(Element element, Class<?> clazz, Elements elements) {
        return transformerClass(element, "keyType", clazz, elements);
    }

    public static TypeName getValType(Element element, Class<?> clazz, Elements elements) {
        return transformerClass(element, "valType", clazz, elements);
    }

    public static TypeName getPrefixType(Element element, Elements elements) {
        return transformerClass(element, "prefixType", PrefsKeyHeadTail.class, elements);
    }

    public static TypeName getSuffixType(Element element, Elements elements) {
        return transformerClass(element, "suffixType", PrefsKeyHeadTail.class, elements);
    }

    public static TypeName getEmptyType(Element element, Elements elements) {
        return transformerClass(element, "emptyType", PrefsDefVal.class, elements);
    }

    private static TypeName transformerClass(Element element, String key, Class<?> clazz, Elements elements) {
        AnnotationMirror am = getAnnotationMirror(element, clazz);
        if (am == null) {
            return null;
        }
        AnnotationValue av = getAnnotationValue(am, key);
        if (av instanceof TypeMirror) {
            Object value = av.getValue();
            if (value != null) {
                return TypeName.get((TypeMirror) value);
            }
        } else if (av != null) {
            String valString = av.getValue().toString();
            TypeElement elem = elements.getTypeElement(valString);
            if (elem == null) {
                switch (valString) {
                    case "void":
                        return TypeName.VOID;
                    case "boolean":
                        return TypeName.BOOLEAN;
                    case "byte":
                        return TypeName.BYTE;
                    case "short":
                        return TypeName.SHORT;
                    case "int":
                        return TypeName.INT;
                    case "long":
                        return TypeName.LONG;
                    case "char":
                        return TypeName.CHAR;
                    case "float":
                        return TypeName.FLOAT;
                    case "double":
                        return TypeName.DOUBLE;
                    default:
                        return null;
                }
            }
            TypeMirror typeMirror = elem.asType();
            if (typeMirror != null) {
                // mz: 2021/08/14 使用原始类型，而不是泛型
                // return TypeName.get(typeMirror);
                return ClassName.bestGuess(elem.toString());
            }
        }
        return null;
    }

    public static AnnotationMirror getAnnotationMirror(Element element, Class<?> clazz) {
        String clazzName = clazz.getCanonicalName();
        for (AnnotationMirror m : element.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    public static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static String wrapperQuotes(String s) {
        return String.format(Locale.getDefault(), "\"%s\"", s);
    }
}
