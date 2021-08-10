package com.mfz.prefsbuilder.annotationprocessor;

import com.mfz.prefsbuilder.BasePrefsClass;
import com.mfz.prefsbuilder.DefaultValue;
import com.mfz.prefsbuilder.PrefParams;
import com.mfz.prefsbuilder.PrefsClass;
import com.mfz.prefsbuilder.PrefsVal;
import com.mfz.prefsbuilder.StringCodec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @author mz
 * @date 2020/05/14/Thu
 * @time 11:14
 */
public class ElementHandler {
    private final Filer mFiler;
    private final Elements mElementUtils;
    private final Map<String, List<Element>> classMap = new HashMap<>();
    private final Map<Integer, MethodInfo> mUserMethodParams = new HashMap<>();
    private final Map<Integer, MethodInfo> mMethodInnerMap = new HashMap<>();
    private final Map<String, PrefsClassInfo> mPrefsClassInfoMap = new HashMap<>();
    private final Map<Integer, MethodInfo> mDecodeMethodMap = new HashMap<>();
    private final Map<Integer, MethodInfo> mEncodeMethodMap = new HashMap<>();
    private final TypeName mStringTypeName;
    private ClassName mBasePrefsClassname;

    public ElementHandler(Filer filer, Elements elementUtils) {
        mFiler = filer;
        mElementUtils = elementUtils;
        mStringTypeName = TypeName.get(String.class);
    }

    public void clean() {
        classMap.clear();
    }

    public void handleBasePrefs(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BasePrefsClass.class);
        for (Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;
                String qualifiedName = typeElement.getQualifiedName().toString();
                mBasePrefsClassname = ClassName.bestGuess(qualifiedName);
                break;
            }
        }
    }

    public void handlePrefsClass(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv) {
        Map<String, String> options = processingEnv.getOptions();
        String defPrefsPkg = options.get(Const.OptionArg.PKG);
        if (StringUtils.isEmpty(defPrefsPkg)) {
            defPrefsPkg = Const.Default.PKG;
        }
        String classPrefix = options.get(Const.OptionArg.CLASS_PREFIX);
        if (StringUtils.isEmpty(classPrefix)) {
            classPrefix = Const.Default.CLASS_PREFIX;
        }
        String classSuffix = options.get(Const.OptionArg.CLASS_SUFFIX);
        if (StringUtils.isEmpty(classSuffix)) {
            classSuffix = Const.Default.CLASS_SUFFIX;
        }
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(PrefsClass.class);
        for (Element element : elements) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement typeElement = (TypeElement) element;

                PackageElement packageElement = mElementUtils.getPackageOf(element);
                String currentPkgName = packageElement.getQualifiedName().toString();

                PrefsClass annotation = typeElement.getAnnotation(PrefsClass.class);
                String pkgName;
                if (annotation.currentPkg()) {
                    pkgName = currentPkgName;
                } else {
                    pkgName = StringUtils.isEmpty(annotation.pkgName()
                            .replace(".", "")) ?
                            defPrefsPkg : annotation.pkgName();
                    if (pkgName.endsWith(".")) {
                        pkgName = pkgName.substring(0, pkgName.length() - 1);
                    }
                }
                String classNameVal = annotation.currentClassName() ?
                        element.getSimpleName().toString() : annotation.className();
                String qualifiedName = typeElement.getQualifiedName().toString();
                if (StringUtils.isEmpty(classNameVal)) {
                    throw new NullPointerException("New class name is empty in "
                            + qualifiedName + "! Please specify the new class name!");
                }
                String className = classPrefix + classNameVal + classSuffix;
                String fileName = StringUtils.isEmpty(annotation.fileName()) ?
                        StringUtils.camel2SmallConst(classNameVal) : annotation.fileName();
                ClassName keyClassName = ClassName.bestGuess(qualifiedName);
                PrefsClassInfo prefsClassInfo = PrefsClassInfo.newBuilder()
                        .className(ClassName.get(pkgName, className))
                        .fileName(fileName)
                        .keyClassName(keyClassName)
                        .build();
                mPrefsClassInfoMap.put(qualifiedName, prefsClassInfo);
            }
        }
    }

    public void handleDefObjectVal(RoundEnvironment roundEnv) {
        Set<? extends Element> aimElements = roundEnv.getElementsAnnotatedWith(DefaultValue.class);
        for (Element element : aimElements) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            String qualifiedName = enclosingElement.getQualifiedName().toString();

            ClassName className = ClassName.bestGuess(qualifiedName);
            DefaultValue annotation = element.getAnnotation(DefaultValue.class);
            int key = annotation.id();
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) element;
                int size = executableElement.getParameters().size();
                // if (size > 1) {
                //     return;
                // }
                MethodInfo params = MethodInfo.newBuilder()
                        .isMethod(true)
                        .name(executableElement.getSimpleName().toString())
                        .className(className)
                        .paramsNum(size)
                        .build();
                mUserMethodParams.put(key, params);
            } else if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                MethodInfo params = MethodInfo.newBuilder()
                        .isMethod(false)
                        .name(variableElement.getSimpleName().toString())
                        .className(className)
                        .build();
                mUserMethodParams.put(key, params);
            }
        }
    }

    public void handleRuleMethod(RoundEnvironment roundEnv) {
        Map<Class<? extends Annotation>, Integer> map = AnnotationList.getRuleMethod();
        Set<Class<? extends Annotation>> classes = map.keySet();
        for (Class<? extends Annotation> cls : classes) {
            Set<? extends Element> aimElements = roundEnv.getElementsAnnotatedWith(cls);
            for (Element element : aimElements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                String qualifiedName = enclosingElement.getQualifiedName().toString();

                ClassName className = ClassName.bestGuess(qualifiedName);
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    int size = executableElement.getParameters().size();
                    // 序列化
                    // if (size > 2 || size < 1) {
                    //     continue;
                    // }
                    // 反序列化
                    // if (size > 3 || size < 2) {
                    //     return;
                    // }
                    MethodInfo params = MethodInfo.newBuilder()
                            .isMethod(true)
                            .name(executableElement.getSimpleName().toString())
                            .className(className)
                            .paramsNum(size)
                            .build();
                    mMethodInnerMap.put(map.get(cls), params);
                    break;
                }
            }
        }
    }

    public void handleCodecMethod(RoundEnvironment roundEnv) {
        List<Class<? extends Annotation>> classes = new ArrayList<>();
        classes.add(StringCodec.Decode.class);
        classes.add(StringCodec.Encode.class);
        for (Class<? extends Annotation> cls : classes) {
            Set<? extends Element> aimElements = roundEnv.getElementsAnnotatedWith(cls);
            for (Element element : aimElements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                String qualifiedName = enclosingElement.getQualifiedName().toString();

                ClassName className = ClassName.bestGuess(qualifiedName);
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement executableElement = (ExecutableElement) element;
                    int size = executableElement.getParameters().size();
                    MethodInfo params = MethodInfo.newBuilder()
                            .isMethod(true)
                            .name(executableElement.getSimpleName().toString())
                            .className(className)
                            .paramsNum(size)
                            .build();
                    int id;
                    Annotation annotation = element.getAnnotation(cls);
                    if (annotation instanceof StringCodec.Encode) {
                        id = ((StringCodec.Encode) annotation).id();
                        mEncodeMethodMap.put(id, params);
                    } else {
                        id = ((StringCodec.Decode) annotation).id();
                        mDecodeMethodMap.put(id, params);
                    }
                    break;
                }
            }
        }
    }

    public void handlePrefsVal(RoundEnvironment roundEnv, Class<? extends Annotation> annotation) {
        Set<? extends Element> aimElements = roundEnv.getElementsAnnotatedWith(annotation);
        for (Element element : aimElements) {
            handleElement(element);
        }
    }

    private void handleElement(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            return;
        }
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = enclosingElement.getQualifiedName().toString();
        List<Element> elementList = classMap.get(fullClassName);
        if (elementList == null) {
            elementList = new ArrayList<>();
            classMap.put(fullClassName, elementList);
        }
        elementList.add(element);
    }

    public void createJavaFiles() {
        if (mBasePrefsClassname == null) {
            throw new NullPointerException("Base prefs class is null! Please implement BasePrefsInterface!");
        }
        Set<Map.Entry<String, List<Element>>> entrySet = classMap.entrySet();
        for (Map.Entry<String, List<Element>> entry : entrySet) {
            createPrefs(entry.getKey(), entry.getValue());
        }
    }

    private void createPrefs(String fullClassName, List<Element> elementList) {
        PrefsClassInfo classInfo = mPrefsClassInfoMap.get(fullClassName);
        if (classInfo == null) {
            throw new NullPointerException("The class not statement annotation:" + fullClassName);
        }
        ClassName className = classInfo.getClassName();
        String prefsFileName = classInfo.getFileName();

        TypeSpec.Builder builder = TypeSpec.classBuilder(className.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(mBasePrefsClassname);

        // doc
        builder.addJavadoc("This class is generated by PrefsBuilder, do not edit.\n\n");
        builder.addJavadoc("Key defined at {@link $T}\n", classInfo.getKeyClassName());

        // 内部单例类
        builder.addType(TypeSpec.classBuilder("SingletonHolder")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addField(FieldSpec.builder(className, "INSTANCE")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", className)
                        .build())
                .build());

        // 构造方法
        builder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addCode(StringUtils.format("super(\"%s\");\n", prefsFileName))
                .build());

        // 获取实例方法
        builder.addMethod(MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addCode("return SingletonHolder.INSTANCE;\n")
                .returns(className)
                .build());

        // get和set方法
        for (Element element : elementList) {
            addMethod(builder, element);
        }

        JavaFile javaFile = JavaFile.builder(className.packageName(), builder.build())
                .build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMethod(TypeSpec.Builder classBuilder, Element element) {

        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        VariableElement variableElement = (VariableElement) element;
        // 注解的成员变量名
        // String filedName = variableElement.getSimpleName().toString();
        // 注解的成员变量类型
        // String filedClassType = variableElement.asType().toString();
        // 注解的成员变量类型
        // String filedValue = (String) variableElement.getConstantValue();
        // filedValue = wrapperQuotes(filedValue);

        // TypeName typeName = mStringTypeName;
        // TypeName keyTypeName = null;
        // TypeName valTypeName = null;
        // Annotation annotation = null;
        // Object defValue = null;
        // String valueName = "String";
        // ClassName currentClass = ClassName.bestGuess(qualifiedName);
        // int defValFromId = -1;
        // boolean defNull = false;
        // boolean defEmpty = false;
        // String defString = null;
        // int type = 0;
        // TypeName prefixTypeName = null;
        // TypeName suffixTypeName = null;
        // int codecId = -1;
        // boolean generateRemove = true;
        // boolean generateContains = true;
        KeyParams.Builder builder = KeyParams.newBuilder()
                .currentClass(ClassName.bestGuess(qualifiedName))
                .filedName(variableElement.getSimpleName().toString())
                .valueName("String")
                .typeName(mStringTypeName);
        for (Class<? extends Annotation> cls : AnnotationList.getPrefsVal()) {
            Annotation annotation = variableElement.getAnnotation(cls);
            builder.annotation(annotation);
            // prefixTypeName = MethodUtils.getPrefixType(element, cls, mElementUtils);
            // suffixTypeName = MethodUtils.getSuffixType(element, cls, mElementUtils);
            if (annotation instanceof PrefsVal.Int) {
                // typeName = TypeName.INT;
                PrefsVal.Int annotationObj = (PrefsVal.Int) annotation;
                // defValue = annotationObj.defVal();
                // valueName = "Int";
                builder.typeName(TypeName.INT)
                        .defValue(annotationObj.defVal())
                        .valueName("Int");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Float) {
                // typeName = TypeName.FLOAT;
                PrefsVal.Float annotationObj = (PrefsVal.Float) annotation;
                // defValue = annotationObj.defVal() + "f";
                // valueName = "Float";
                builder.typeName(TypeName.FLOAT)
                        .defValue(annotationObj.defVal() + "f")
                        .valueName("Float");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Bool) {
                // typeName = TypeName.BOOLEAN;
                PrefsVal.Bool annotationObj = (PrefsVal.Bool) annotation;
                // defValue = annotationObj.defVal();
                // valueName = "Bool";
                builder.typeName(TypeName.BOOLEAN)
                        .defValue(annotationObj.defVal())
                        .valueName("Bool");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Byte) {
                // typeName = TypeName.BYTE;
                PrefsVal.Byte annotationObj = (PrefsVal.Byte) annotation;
                // defValue = StringUtils.format("(byte)%d", annotationObj.defVal());
                // valueName = "Byte";
                builder.typeName(TypeName.BYTE)
                        .defValue(StringUtils.format("(byte)%d", annotationObj.defVal()))
                        .valueName("Byte");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Double) {
                // typeName = TypeName.DOUBLE;
                PrefsVal.Double annotationObj = (PrefsVal.Double) annotation;
                // defValue = annotationObj.defVal();
                // valueName = "Double";
                builder.typeName(TypeName.DOUBLE)
                        .defValue(annotationObj)
                        .valueName("Double");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Char) {
                // typeName = TypeName.CHAR;
                PrefsVal.Char annotationObj = (PrefsVal.Char) annotation;
                char defChar = annotationObj.defVal();
                // if (Character.isDefined(defChar)) {
                //     defValue = StringUtils.format("'%c'", defChar);
                // } else {
                //     defValue = StringUtils.format("(char)%d", (int) defChar);
                // }
                // valueName = "Char";
                builder.typeName(TypeName.CHAR)
                        .defValue(Character.isDefined(defChar) ?
                                StringUtils.format("'%c'", defChar) :
                                StringUtils.format("(char)%d", (int) defChar))
                        .valueName("Char");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Long) {
                // typeName = TypeName.LONG;
                PrefsVal.Long annotationObj = (PrefsVal.Long) annotation;
                // defValue = annotationObj.defVal();
                // valueName = "Long";
                builder.typeName(TypeName.LONG)
                        .defValue(annotationObj)
                        .valueName("Long");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Short) {
                // typeName = TypeName.SHORT;
                PrefsVal.Short annotationObj = (PrefsVal.Short) annotation;
                // defValue = StringUtils.format("(short)%d", annotationObj.defVal());
                // valueName = "Short";
                builder.typeName(TypeName.SHORT)
                        .defValue(StringUtils.format("(short)%d", annotationObj.defVal()))
                        .valueName("Short");
                // type = 1;
            } else if (annotation instanceof PrefsVal.String) {
                // typeName = mStringTypeName;
                PrefsVal.String annotationObj = (PrefsVal.String) annotation;
                // // if (annotationObj.defNull()) {
                // //     defValue = null;
                // // } else {
                // defValue = annotationObj.defVal();
                // // }
                // valueName = "String";
                builder.typeName(mStringTypeName)
                        .defValue(annotationObj.defVal())
                        .valueName("String");
                // type = 1;
            } else if (annotation instanceof PrefsVal.Object) {
                TypeName typeName = MethodUtils.getType(element, cls, mElementUtils);
                // PrefsVal.Object annotationObj = (PrefsVal.Object) annotation;
                builder.typeName(typeName)
                        .keyTypeName(typeName)
                        .genericVal(true);
                // type = 2;
            } else if (annotation instanceof PrefsVal.List) {
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(List.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
                // type = 2;
            } else if (annotation instanceof PrefsVal.Set) {
                // keyTypeName = MethodUtils.getType(element, cls, mElementUtils);
                // if (keyTypeName != null) {
                //     typeName = ParameterizedTypeName.get(ClassName.get(Set.class), keyTypeName);
                // }
                // PrefsVal.Set annotationObj = (PrefsVal.Set) annotation;
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Set.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
                // type = 2;
            } else if (annotation instanceof PrefsVal.Queue) {
                // keyTypeName = MethodUtils.getType(element, cls, mElementUtils);
                // if (keyTypeName != null) {
                //     typeName = ParameterizedTypeName.get(ClassName.get(Queue.class), keyTypeName);
                // }
                // PrefsVal.Queue annotationObj = (PrefsVal.Queue) annotation;
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Queue.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
                // type = 2;
            } else if (annotation instanceof PrefsVal.Deque) {
                // keyTypeName = MethodUtils.getType(element, cls, mElementUtils);
                // if (keyTypeName != null) {
                //     typeName = ParameterizedTypeName.get(ClassName.get(Deque.class), keyTypeName);
                // }
                // PrefsVal.Deque annotationObj = (PrefsVal.Deque) annotation;
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Deque.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
                // type = 2;
            } else if (annotation instanceof PrefsVal.SparseArray) {
                // keyTypeName = MethodUtils.getType(element, cls, mElementUtils);
                // if (keyTypeName != null) {
                //     typeName = ParameterizedTypeName.get(
                //             ClassName.bestGuess("android.util.SparseArray"), keyTypeName);
                // }
                // PrefsVal.SparseArray annotationObj = (PrefsVal.SparseArray) annotation;
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(ClassName.bestGuess("android.util.SparseArray"), subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
                // type = 2;
            } else if (annotation instanceof PrefsVal.Map) {
                // keyTypeName = MethodUtils.getKeyType(element, cls, mElementUtils);
                // valTypeName = MethodUtils.getValType(element, cls, mElementUtils);
                // if (keyTypeName != null && valTypeName != null) {
                //     typeName = ParameterizedTypeName.get(
                //             ClassName.get(Map.class), keyTypeName, valTypeName);
                // }
                // PrefsVal.Map annotationObj = (PrefsVal.Map) annotation;
                TypeName keyTypeName = MethodUtils.getKeyType(element, cls, mElementUtils);
                TypeName valTypeName = MethodUtils.getValType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Map.class, keyTypeName, valTypeName))
                        .keyTypeName(keyTypeName)
                        .valTypeName(valTypeName)
                        .genericVal(true);
                // type = 2;
            }
            if (annotation != null) {
                break;
            }
        }

        PrefParams prefParams = variableElement.getAnnotation(PrefParams.class);
        PrefParamsData paramsData;
        if (prefParams == null) {
            paramsData = PrefParamsData.newBuilder().build();
        } else {
            paramsData = PrefParamsData.newBuilder()
                    .prefixTypeName(MethodUtils.getPrefixType(element, PrefParams.class, mElementUtils))
                    .suffixTypeName(MethodUtils.getSuffixType(element, PrefParams.class, mElementUtils))
                    .defValFromId(prefParams.defValFromId())
                    .generateRemove(prefParams.generateRemove())
                    .generateContains(prefParams.generateContains())
                    .codeId(prefParams.codecId())
                    .defNull(prefParams.defNull())
                    .defEmpty(prefParams.defEmpty())
                    .defString(prefParams.defString())
                    .build();
        }

        KeyParams params = builder
                .paramsData(paramsData)
                .build();
        if (mStringTypeName.equals(params.getTypeName())) {
            if (paramsData.isDefNull()) {
                // defValue = null;
                params.setDefValue(null);
            }
        }

        if (params.isGenericVal()) {
            buildGenericMethod(classBuilder, params);
        } else {
            buildBasicMethod(classBuilder, params);
        }
    }

    private ParameterizedTypeName getParameterizedTypedName(Class<?> clazz, TypeName... subTypeName) {
        ClassName rawType = ClassName.get(clazz);
        return getParameterizedTypedName(rawType, subTypeName);
    }

    private ParameterizedTypeName getParameterizedTypedName(ClassName rawType, TypeName... subTypeName) {
        ParameterizedTypeName typeName;
        if (subTypeName != null) {
            typeName = ParameterizedTypeName.get(rawType, subTypeName);
        } else {
            typeName = ParameterizedTypeName.get(rawType);
        }
        return typeName;
    }

    private void buildBasicMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        MethodSpec.Builder methodBuilder;
        CodeBlock.Builder codeGetBuilder = CodeBlock.builder();
        MethodInfo methodInfo;
        boolean hasPrefix = params.getParamsData().getPrefixTypeName() != null
                && params.getParamsData().getPrefixTypeName() != TypeName.VOID;
        boolean hasSuffix = params.getParamsData().getSuffixTypeName() != null
                && params.getParamsData().getSuffixTypeName() != TypeName.VOID;
        String prefixTxt = "prefix";
        String suffixTxt = "suffix";

        String keyStatement;
        if (hasPrefix && hasSuffix) {
            keyStatement = prefixTxt + "+$T.%s+" + suffixTxt;
        } else if (hasPrefix) {
            keyStatement = prefixTxt + "+$T.%s";
        } else if (hasSuffix) {
            keyStatement = "$T.%s+" + suffixTxt;
        } else {
            keyStatement = "$T.%s";
        }
        keyStatement = StringUtils.format(keyStatement, params.getFiledName());

        // get方法
        methodInfo = mUserMethodParams.get(params.getParamsData().getDefValFromId());
        if (params.getParamsData().getDefValFromId() > 0 && methodInfo == null) {
            canNotFindDefVal(params.getParamsData().getDefValFromId());
        }
        if (methodInfo == null) {
            // string类型非空时需要处理特殊字符
            if (mStringTypeName.equals(params.getTypeName()) && params.getDefValue() != null) {
                codeGetBuilder.addStatement("$T defVal=$S", params.getTypeName(), String.valueOf(params.getDefValue()));
            } else {
                codeGetBuilder.addStatement(
                        StringUtils.format("$T defVal=%s", String.valueOf(params.getDefValue())), params.getTypeName());
            }
        } else if (methodInfo.isMethod()) {
            if (methodInfo.getParamsNum() == 0) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T defVal=$T.%s()", methodInfo.getName()), params.getTypeName(), methodInfo.getClassName());
            } else if (methodInfo.getParamsNum() == 1) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T defVal=$T.%s(%s)", methodInfo.getName(), keyStatement),
                        params.getTypeName(), methodInfo.getClassName(), params.getCurrentClass());
            } else {
                notSupportMethod(methodInfo, 0, 1);
            }
        } else {
            codeGetBuilder.addStatement(StringUtils.format(
                    "$T defVal=$T.%s", methodInfo.getName()), params.getTypeName(), methodInfo.getClassName());
        }
        methodInfo = mDecodeMethodMap.get(params.getParamsData().getCodeId());
        if (methodInfo == null) {
            codeGetBuilder.addStatement(
                    StringUtils.format("return getInstance().get%s(%s, defVal)",
                            params.getValueName(), keyStatement), params.getCurrentClass());
        } else {
            codeGetBuilder.addStatement(
                    StringUtils.format("$T s=getInstance().get%s(%s, defVal)",
                            params.getValueName(), keyStatement), mStringTypeName, params.getCurrentClass());
            if (methodInfo.getParamsNum() == 1) {
                codeGetBuilder.addStatement(StringUtils.format("return $T.%s(s)",
                        methodInfo.getName()), methodInfo.getClassName());
            } else if (methodInfo.getParamsNum() == 2) {
                codeGetBuilder.addStatement(StringUtils.format("return $T.%s(%s, s)",
                        methodInfo.getName(), keyStatement), methodInfo.getClassName());
            } else {
                notSupportMethod(methodInfo, 1, 2);
            }
        }

        String getMethodName;
        if (params.getTypeName() == TypeName.BOOLEAN) {
            getMethodName = MethodUtils.getBoolMethodName(params.getFiledName());
        } else {
            getMethodName = MethodUtils.getGetMethodName(params.getFiledName());
        }
        methodBuilder = MethodSpec.methodBuilder(getMethodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(params.getTypeName())
                .addCode(codeGetBuilder.build());
        if (hasPrefix) {
            methodBuilder.addParameter(params.getParamsData().getPrefixTypeName(), prefixTxt, Modifier.FINAL);
        }
        if (hasSuffix) {
            methodBuilder.addParameter(params.getParamsData().getSuffixTypeName(), suffixTxt, Modifier.FINAL);
        }
        classBuilder.addMethod(methodBuilder.build());

        // set方法
        CodeBlock.Builder codeSetBuilder = CodeBlock.builder();
        methodInfo = mEncodeMethodMap.get(params.getParamsData().getCodeId());
        if (methodInfo == null) {
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, value)", params.getValueName(), keyStatement), params.getCurrentClass());
        } else {
            if (methodInfo.getParamsNum() == 1) {
                codeSetBuilder.addStatement(StringUtils.format("$T encode=$T.%s(value)",
                        methodInfo.getName()), mStringTypeName, methodInfo.getClassName());
            } else if (methodInfo.getParamsNum() == 2) {
                codeSetBuilder.addStatement(StringUtils.format("$T encode=$T.%s(%s, value)",
                        methodInfo.getName(), keyStatement), mStringTypeName, methodInfo.getClassName());
            } else {
                notSupportMethod(methodInfo, 1, 2);
            }
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, encode)", params.getValueName(), keyStatement), params.getCurrentClass());
        }

        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getSetMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(codeSetBuilder.build());
        if (hasPrefix) {
            methodBuilder.addParameter(params.getParamsData().getPrefixTypeName(), prefixTxt, Modifier.FINAL);
        }
        if (hasSuffix) {
            methodBuilder.addParameter(params.getParamsData().getSuffixTypeName(), suffixTxt, Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());

        // remove方法
        CodeBlock.Builder codeRemoveBuilder = CodeBlock.builder();
        codeRemoveBuilder.addStatement(StringUtils.format(
                "return getInstance().remove(%s)", keyStatement), params.getCurrentClass());

        if (params.getParamsData().isGenerateRemove()) {
            addRemoveMethod(classBuilder,
                    keyStatement,
                    params.getCurrentClass(),
                    params.getFiledName(),
                    params.getTypeName(),
                    params.getParamsData().getPrefixTypeName(),
                    params.getParamsData().getSuffixTypeName(),
                    hasPrefix,
                    hasSuffix,
                    prefixTxt,
                    suffixTxt);
        }

        if (params.getParamsData().isGenerateContains()) {
            addContainsMethod(classBuilder,
                    keyStatement,
                    params.getCurrentClass(),
                    params.getFiledName(),
                    params.getTypeName(),
                    params.getParamsData().getPrefixTypeName(),
                    params.getParamsData().getSuffixTypeName(),
                    hasPrefix,
                    hasSuffix,
                    prefixTxt,
                    suffixTxt);
        }
    }

    private void buildGenericMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        CodeBlock.Builder codeGetBuilder = CodeBlock.builder();
        CodeBlock.Builder codeSetBuilder = CodeBlock.builder();
        boolean hasPrefix = params.getParamsData().getPrefixTypeName() != null
                && params.getParamsData().getPrefixTypeName() != TypeName.VOID;
        boolean hasSuffix = params.getParamsData().getSuffixTypeName() != null
                && params.getParamsData().getSuffixTypeName() != TypeName.VOID;
        String prefixTxt = "prefix";
        String suffixTxt = "suffix";
        MethodSpec.Builder methodBuilder;

        String keyStatement;
        if (hasPrefix && hasSuffix) {
            keyStatement = prefixTxt + "+$T.%s+" + suffixTxt;
        } else if (hasPrefix) {
            keyStatement = prefixTxt + "+$T.%s";
        } else if (hasSuffix) {
            keyStatement = "$T.%s+" + suffixTxt;
        } else {
            keyStatement = "$T.%s";
        }
        keyStatement = StringUtils.format(keyStatement, params.getFiledName());

        String valueName = "String";
        MethodInfo serializerMethod = null;
        ClassName serializerClass = null;
        String serializerName = null;
        Class<? extends Annotation> annotationType = params.getAnnotation().annotationType();
        for (Integer integer : AnnotationList.getSerializerByVal().get(annotationType)) {
            serializerMethod = mMethodInnerMap.get(integer);
            if (serializerMethod == null) {
                continue;
            }
            serializerClass = serializerMethod.getClassName();
            serializerName = serializerMethod.getName();
            break;
        }
        if (serializerMethod == null) {
            canNotFindMethod(annotationType, true);
        }

        MethodInfo deserializerMethod = null;
        ClassName deserializerClass = null;
        String deserializerName = null;
        for (Integer integer : AnnotationList.getDeserializerByVal().get(annotationType)) {
            deserializerMethod = mMethodInnerMap.get(integer);
            if (deserializerMethod == null) {
                continue;
            }
            deserializerClass = deserializerMethod.getClassName();
            deserializerName = deserializerMethod.getName();
            break;
        }
        if (deserializerMethod == null) {
            canNotFindMethod(annotationType, false);
        }

        // get方法
        codeGetBuilder.addStatement(StringUtils.format(
                "$T json = getInstance().get%s(%s, $S)",
                valueName, keyStatement), mStringTypeName, params.getCurrentClass(), params.getParamsData().getDefString());
        if (params.getValTypeName() == null) {
            if (deserializerMethod.getParamsNum() == 2) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(json,$T.class)",
                        deserializerName), params.getTypeName(), deserializerClass, params.getKeyTypeName());
            } else if (deserializerMethod.getParamsNum() == 3) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(%s,json,$T.class)",
                        deserializerName, keyStatement), params.getTypeName(), deserializerClass,
                        params.getCurrentClass(), params.getKeyTypeName());
            } else {
                notSupportMethod(serializerMethod, 2, 3);
            }
        } else {
            if (deserializerMethod.getParamsNum() == 3) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(json,$T.class,$T.class)",
                        deserializerName), params.getTypeName(), deserializerClass, params.getKeyTypeName(), params.getValTypeName());
            } else if (deserializerMethod.getParamsNum() == 4) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(%s,json,$T.class,$T.class)",
                        deserializerName, keyStatement), params.getTypeName(), deserializerClass,
                        params.getCurrentClass(), params.getKeyTypeName(), params.getValTypeName());
            } else {
                notSupportMethod(serializerMethod, 3, 4);
            }
        }
        if (params.getParamsData().isDefNull()) {
            codeGetBuilder.addStatement("return value");
        } else if (params.getParamsData().isDefEmpty()) {
            codeGetBuilder.beginControlFlow("if(value==null)");
            if (params.getAnnotation() instanceof PrefsVal.List) {
                TypeName defType = TypeName.get(ArrayList.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            } else if (params.getAnnotation() instanceof PrefsVal.Set) {
                TypeName defType = TypeName.get(HashSet.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            } else if (params.getAnnotation() instanceof PrefsVal.Queue) {
                TypeName defType = TypeName.get(LinkedList.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            } else if (params.getAnnotation() instanceof PrefsVal.Deque) {
                TypeName defType = TypeName.get(LinkedList.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            } else if (params.getAnnotation() instanceof PrefsVal.SparseArray) {
                codeGetBuilder.addStatement("return new $T()", params.getTypeName());
            } else if (params.getAnnotation() instanceof PrefsVal.Map) {
                TypeName defType = TypeName.get(HashMap.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            }
            codeGetBuilder.nextControlFlow("else")
                    .addStatement("return value")
                    .endControlFlow();
        } else {
            codeGetBuilder.beginControlFlow("if(value==null)");
            MethodInfo methodInfo = mUserMethodParams.get(params.getParamsData().getDefValFromId());
            if (methodInfo == null) {
                canNotFindDefVal(params.getParamsData().getDefValFromId());
            } else if (methodInfo.isMethod()) {
                if (methodInfo.getParamsNum() == 0) {
                    codeGetBuilder.addStatement(StringUtils.format(
                            "return $T.%s()", methodInfo.getName()), methodInfo.getClassName());
                } else if (methodInfo.getParamsNum() == 1) {
                    codeGetBuilder.addStatement(StringUtils.format(
                            "return $T.%s(%s)", methodInfo.getName(), keyStatement),
                            methodInfo.getClassName(), params.getCurrentClass());
                } else {
                    notSupportMethod(serializerMethod, 0, 1);
                }
            } else {
                codeGetBuilder.addStatement(StringUtils.format(
                        "return $T.%s", methodInfo.getName()), methodInfo.getClassName());
            }

            codeGetBuilder.nextControlFlow("else")
                    .addStatement("return value")
                    .endControlFlow();
        }

        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getGetMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(params.getTypeName())
                .addCode(codeGetBuilder.build());
        if (hasPrefix) {
            methodBuilder.addParameter(params.getParamsData().getPrefixTypeName(), prefixTxt, Modifier.FINAL);
        }
        if (hasSuffix) {
            methodBuilder.addParameter(params.getParamsData().getSuffixTypeName(), suffixTxt, Modifier.FINAL);
        }
        classBuilder.addMethod(methodBuilder.build());

        // set方法
        if (serializerMethod.getParamsNum() == 1) {
            codeSetBuilder.addStatement(StringUtils.format("getInstance().set%s(%s, $T.%s(value))",
                    valueName, keyStatement, serializerName), params.getCurrentClass(), serializerClass);
        } else if (serializerMethod.getParamsNum() == 2) {
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, $T.%s(%s,value))",
                    valueName, keyStatement, serializerName, keyStatement),
                    params.getCurrentClass(), serializerClass, params.getCurrentClass());
        } else {
            notSupportMethod(serializerMethod, 1, 2);
        }

        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getSetMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(codeSetBuilder.build());
        if (hasPrefix) {
            methodBuilder.addParameter(params.getParamsData().getPrefixTypeName(), prefixTxt, Modifier.FINAL);
        }
        if (hasSuffix) {
            methodBuilder.addParameter(params.getParamsData().getSuffixTypeName(), suffixTxt, Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());

        if (params.getParamsData().isGenerateRemove()) {
            addRemoveMethod(classBuilder,
                    keyStatement,
                    params.getCurrentClass(),
                    params.getFiledName(),
                    params.getTypeName(),
                    params.getParamsData().getPrefixTypeName(),
                    params.getParamsData().getSuffixTypeName(),
                    hasPrefix,
                    hasSuffix,
                    prefixTxt,
                    suffixTxt);
        }

        if (params.getParamsData().isGenerateContains()) {
            addContainsMethod(classBuilder,
                    keyStatement,
                    params.getCurrentClass(),
                    params.getFiledName(),
                    params.getTypeName(),
                    params.getParamsData().getPrefixTypeName(),
                    params.getParamsData().getSuffixTypeName(),
                    hasPrefix,
                    hasSuffix,
                    prefixTxt,
                    suffixTxt);
        }
    }

    private void addRemoveMethod(TypeSpec.Builder classBuilder,
                                 String keyStatement,
                                 ClassName currentClass,
                                 String filedName,
                                 TypeName typeName,
                                 TypeName prefixTypeName,
                                 TypeName suffixTypeName,
                                 boolean hasPrefix,
                                 boolean hasSuffix,
                                 String prefixTxt,
                                 String suffixTxt) {
        CodeBlock.Builder codeRemoveBuilder = CodeBlock.builder();
        codeRemoveBuilder.addStatement(StringUtils.format(
                "return getInstance().remove(%s)", keyStatement), currentClass);

        MethodSpec.Builder methodBuilder;
        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getRemoveMethodName(filedName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addCode(codeRemoveBuilder.build());
        if (hasPrefix) {
            methodBuilder.addParameter(prefixTypeName, prefixTxt, Modifier.FINAL);
        }
        if (hasSuffix) {
            methodBuilder.addParameter(suffixTypeName, suffixTxt, Modifier.FINAL);
        }
        methodBuilder.addParameter(typeName, "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());
    }

    private void addContainsMethod(TypeSpec.Builder classBuilder,
                                   String keyStatement,
                                   ClassName currentClass,
                                   String filedName,
                                   TypeName typeName,
                                   TypeName prefixTypeName,
                                   TypeName suffixTypeName,
                                   boolean hasPrefix,
                                   boolean hasSuffix,
                                   String prefixTxt,
                                   String suffixTxt) {
        CodeBlock.Builder codeRemoveBuilder = CodeBlock.builder();
        codeRemoveBuilder.addStatement(StringUtils.format(
                "return getInstance().contains(%s)", keyStatement), currentClass);

        MethodSpec.Builder methodBuilder;
        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getContainsMethodName(filedName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addCode(codeRemoveBuilder.build());
        if (hasPrefix) {
            methodBuilder.addParameter(prefixTypeName, prefixTxt, Modifier.FINAL);
        }
        if (hasSuffix) {
            methodBuilder.addParameter(suffixTypeName, suffixTxt, Modifier.FINAL);
        }
        methodBuilder.addParameter(typeName, "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());
    }

    private static void notSupportMethod(MethodInfo methodInfo, int num1, int num2) {
        throw new IllegalArgumentException("PrefBuilder: Only support param num " + num1 + " or " + num2 + ":"
                + methodInfo.getClassName() + "." + methodInfo.getClassName() + "!");
    }

    private static void canNotFindMethod(Class<? extends Annotation> cls, boolean isSerializer) {
        throw new IllegalArgumentException("PrefBuilder: Can not find " +
                (isSerializer ? "serializer" : "deserializer") + " method of " + cls + "!");
    }

    private static void canNotFindDefVal(int id) {
        throw new IllegalArgumentException("PrefBuilder: Can not find default value from id = " + id + " !");
    }

}
