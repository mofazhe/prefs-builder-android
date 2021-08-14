package com.mfz.prefsbuilder.annotationprocessor;

import com.mfz.prefsbuilder.BasePrefsClass;
import com.mfz.prefsbuilder.DefaultValue;
import com.mfz.prefsbuilder.PrefsClass;
import com.mfz.prefsbuilder.PrefsKey;
import com.mfz.prefsbuilder.StringCodec;
import com.mfz.prefsbuilder.annotationprocessor.data.KeyParams;
import com.mfz.prefsbuilder.annotationprocessor.data.AnnotationParams;
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
    private final Map<String, List<KeyParams>> mClassKeyMap = new HashMap<>();
    private final Map<Integer, MethodInfo> mDefaultMethodMap = new HashMap<>();
    private final Map<Class<? extends Annotation>, MethodInfo> mRuleMethodMap = new HashMap<>();
    private final Map<String, PrefsClassInfo> mPrefsClassInfoMap = new HashMap<>();
    private final Map<Integer, MethodInfo> mDecodeMethodMap = new HashMap<>();
    private final Map<Integer, MethodInfo> mEncodeMethodMap = new HashMap<>();
    private final TypeName mStringTypeName;
    private ClassName mBasePrefsClassname;
    private final ProcessingEnvironment mProcessingEnv;

    public ElementHandler(Filer filer, Elements elementUtils, ProcessingEnvironment processingEnv) {
        mFiler = filer;
        mElementUtils = elementUtils;
        mProcessingEnv = processingEnv;
        mStringTypeName = TypeName.get(String.class);
    }

    public void clean() {
        mClassKeyMap.clear();
        mDefaultMethodMap.clear();
        mRuleMethodMap.clear();
        mPrefsClassInfoMap.clear();
        mDecodeMethodMap.clear();
        mEncodeMethodMap.clear();
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

    public void handlePrefsClass(RoundEnvironment roundEnv) {
        Map<String, String> options = mProcessingEnv.getOptions();
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
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }
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

    public void handleDefaultVal(RoundEnvironment roundEnv) {
        Set<? extends Element> aimElements = roundEnv.getElementsAnnotatedWith(DefaultValue.class);
        for (Element element : aimElements) {
            if (element.getKind() != ElementKind.METHOD && element.getKind() != ElementKind.FIELD) {
                continue;
            }
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
                MethodInfo info = MethodInfo.newBuilder()
                        .isMethod(true)
                        .name(executableElement.getSimpleName().toString())
                        .className(className)
                        .paramsNum(size)
                        .build();
                mDefaultMethodMap.put(key, info);
            } else if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                MethodInfo info = MethodInfo.newBuilder()
                        .isMethod(false)
                        .name(variableElement.getSimpleName().toString())
                        .className(className)
                        .build();
                mDefaultMethodMap.put(key, info);
            }
        }
    }

    public void handleRuleMethod(RoundEnvironment roundEnv) {
        for (Class<? extends Annotation> cls : AnnotationList.getRuleMethodList()) {
            Set<? extends Element> aimElements = roundEnv.getElementsAnnotatedWith(cls);
            for (Element element : aimElements) {
                if (element.getKind() != ElementKind.METHOD) {
                    continue;
                }
                MethodInfo params = getMethodInfo(element);
                mRuleMethodMap.put(cls, params);
                break;
            }
        }
    }

    public void handleCodecMethod(RoundEnvironment roundEnv) {
        Set<? extends Element> aimElements;

        aimElements = roundEnv.getElementsAnnotatedWith(StringCodec.Decode.class);
        for (Element element : aimElements) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            MethodInfo params = getMethodInfo(element);
            StringCodec.Decode annotation = element.getAnnotation(StringCodec.Decode.class);
            mDecodeMethodMap.put(annotation.id(), params);
        }

        aimElements = roundEnv.getElementsAnnotatedWith(StringCodec.Encode.class);
        for (Element element : aimElements) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            MethodInfo params = getMethodInfo(element);
            StringCodec.Encode annotation = element.getAnnotation(StringCodec.Encode.class);
            mEncodeMethodMap.put(annotation.id(), params);
        }
    }

    private MethodInfo getMethodInfo(Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        ClassName className = ClassName.bestGuess(qualifiedName);
        ExecutableElement executableElement = (ExecutableElement) element;
        int size = executableElement.getParameters().size();
        return MethodInfo.newBuilder()
                .isMethod(true)
                .name(executableElement.getSimpleName().toString())
                .className(className)
                .paramsNum(size)
                .build();
    }

    public void handlePrefsKey(RoundEnvironment roundEnv) {
        Class<? extends Annotation> cls;

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Bool.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Bool annotation = element.getAnnotation(PrefsKey.Bool.class);
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.BOOLEAN)
                    .defValue(annotation.defVal())
                    .valueName("Bool")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Byte.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Byte annotation = element.getAnnotation(PrefsKey.Byte.class);
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.BYTE)
                    .defValue(StringUtils.format("(byte)%d", annotation.defVal()))
                    .valueName("Byte")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Char.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Char annotation = element.getAnnotation(PrefsKey.Char.class);
            char defVal = annotation.defVal();
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.CHAR)
                    .defValue(Character.isDefined(defVal) ?
                            StringUtils.format("'%c'", defVal) :
                            StringUtils.format("(char)%d", (int) defVal))
                    .valueName("Char")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Short.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Short annotation = element.getAnnotation(PrefsKey.Short.class);
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.SHORT)
                    .defValue(annotation.defVal())
                    .valueName("Short")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Int.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Int annotation = element.getAnnotation(PrefsKey.Int.class);
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.INT)
                    .defValue(annotation.defVal())
                    .valueName("Int")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Long.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Long annotation = element.getAnnotation(PrefsKey.Long.class);
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.LONG)
                    .defValue(annotation.defVal() + "L")
                    .valueName("Long")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Float.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Float annotation = element.getAnnotation(PrefsKey.Float.class);
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.FLOAT)
                    .defValue(annotation.defVal() + "f")
                    .valueName("Float")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.Double.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.Double annotation = element.getAnnotation(PrefsKey.Double.class);
            addToClassMap(createKeyParams((VariableElement) element, annotation)
                    .typeName(TypeName.DOUBLE)
                    .defValue(annotation.defVal())
                    .valueName("Double")
                    .build());
        }

        for (Element element : roundEnv.getElementsAnnotatedWith(PrefsKey.String.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            PrefsKey.String annotation = element.getAnnotation(PrefsKey.String.class);
            VariableElement variableElement = (VariableElement) element;
            AnnotationParams annotationParams = AnnotationParams.create(variableElement, mElementUtils);
            addToClassMap(createKeyParamsWithout(variableElement, annotation)
                    .typeName(mStringTypeName)
                    .defValue(annotationParams.isDefNull() ? null : annotation.defVal())
                    .valueName("String")
                    .annotationParams(annotationParams)
                    .build());
        }

        cls = PrefsKey.Object.class;
        for (Element element : roundEnv.getElementsAnnotatedWith(cls)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            TypeName typeName = MethodUtils.getType(element, cls, mElementUtils);
            addToClassMap(createKeyParams((VariableElement) element, cls)
                    .typeName(typeName)
                    .keyTypeName(typeName)
                    .genericVal(true)
                    .build());
        }

        handleCollectionType(roundEnv, PrefsKey.List.class, List.class);
        handleCollectionType(roundEnv, PrefsKey.Set.class, Set.class);
        handleCollectionType(roundEnv, PrefsKey.Queue.class, Queue.class);
        handleCollectionType(roundEnv, PrefsKey.Deque.class, Deque.class);

        handleMapType(roundEnv);
    }

    public void handleCollectionType(RoundEnvironment roundEnv,
                                     Class<? extends Annotation> annotationType,
                                     Class<?> clazz) {
        for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            TypeName subTypeName = MethodUtils.getType(element, annotationType, mElementUtils);
            addToClassMap(createKeyParams((VariableElement) element, annotationType)
                    .typeName(getParameterizedTypedName(clazz, subTypeName))
                    .keyTypeName(subTypeName)
                    .genericVal(true)
                    .build());
        }
    }

    public void handleMapType(RoundEnvironment roundEnv) {
        Class<? extends Annotation> annotationType = PrefsKey.Map.class;
        for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
            if (element.getKind() != ElementKind.FIELD) {
                continue;
            }
            TypeName keyTypeName = MethodUtils.getKeyType(element, annotationType, mElementUtils);
            TypeName valTypeName = MethodUtils.getValType(element, annotationType, mElementUtils);
            addToClassMap(createKeyParams((VariableElement) element, annotationType)
                    .typeName(getParameterizedTypedName(Map.class, keyTypeName, valTypeName))
                    .keyTypeName(keyTypeName)
                    .valTypeName(valTypeName)
                    .genericVal(true)
                    .build());
        }
    }

    public void addToClassMap(KeyParams params) {
        String fullClassStr = params.getFullClassStr();
        List<KeyParams> paramsList = mClassKeyMap.get(fullClassStr);
        if (paramsList == null) {
            paramsList = new ArrayList<>();
            mClassKeyMap.put(fullClassStr, paramsList);
        }
        paramsList.add(params);
    }

    public KeyParams.Builder createKeyParams(VariableElement element, Class<? extends Annotation> annotationCls) {
        AnnotationParams annotationParams = AnnotationParams.create(element, mElementUtils);

        return createKeyParamsWithout(element, annotationCls)
                .annotationParams(annotationParams);
    }

    public KeyParams.Builder createKeyParams(VariableElement element, Annotation annotation) {
        AnnotationParams annotationParams = AnnotationParams.create(element, mElementUtils);

        return createKeyParamsWithout(element, annotation)
                .annotationParams(annotationParams);
    }

    public KeyParams.Builder createKeyParamsWithout(VariableElement element, Class<? extends Annotation> annotationCls) {
        Annotation annotation = element.getAnnotation(annotationCls);
        return createKeyParamsWithout(element, annotation);
    }

    public KeyParams.Builder createKeyParamsWithout(VariableElement element, Annotation annotation) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String fullClassStr = enclosingElement.getQualifiedName().toString();

        return KeyParams.newBuilder()
                .element((VariableElement) element)
                .annotation(annotation)
                .fullClassName(ClassName.bestGuess(fullClassStr))
                .fullClassStr(fullClassStr)
                .filedName(element.getSimpleName().toString())
                .valueName("String");
    }

    public void createJavaFiles() {
        if (mBasePrefsClassname == null) {
            throw new NullPointerException("Base prefs class is null! Please implement BasePrefsInterface!");
        }
        Set<Map.Entry<String, List<KeyParams>>> entrySet = mClassKeyMap.entrySet();
        for (Map.Entry<String, List<KeyParams>> entry : entrySet) {
            createPrefs(entry.getKey(), entry.getValue());
        }
    }

    private void createPrefs(String fullClassName, List<KeyParams> paramsList) {
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
                .addCode("super($S);\n", prefsFileName)
                .build());

        // 获取实例方法
        builder.addMethod(MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addCode("return SingletonHolder.INSTANCE;\n")
                .returns(className)
                .build());

        // get和set方法
        for (KeyParams keyParams : paramsList) {
            addMethod(builder, keyParams);
        }

        JavaFile javaFile = JavaFile.builder(className.packageName(), builder.build())
                .build();
        try {
            javaFile.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMethod(TypeSpec.Builder classBuilder, KeyParams params) {
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
        AnnotationParams annotationParams = params.getAnnotationParams();

        MethodSpec.Builder methodBuilder;
        MethodInfo methodInfo;

        // get方法
        CodeBlock.Builder codeGetBuilder = CodeBlock.builder();
        methodInfo = mDefaultMethodMap.get(annotationParams.getDefValFromId());
        if (annotationParams.getDefValFromId() > 0 && methodInfo == null) {
            canNotFindDefVal(annotationParams.getDefValFromId());
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
                        "$T defVal=$T.%s(%s)", methodInfo.getName(), annotationParams.getKeyStatement()),
                        params.getTypeName(), methodInfo.getClassName(), params.getFullClassName());
            } else {
                notSupportMethod(methodInfo, 0, 1);
            }
        } else {
            codeGetBuilder.addStatement(StringUtils.format(
                    "$T defVal=$T.%s", methodInfo.getName()), params.getTypeName(), methodInfo.getClassName());
        }
        methodInfo = mDecodeMethodMap.get(annotationParams.getCodeId());
        if (mStringTypeName != params.getTypeName() || methodInfo == null) {
            codeGetBuilder.addStatement(
                    StringUtils.format("return getInstance().get%s(%s, defVal)",
                            params.getValueName(), annotationParams.getKeyStatement()), params.getFullClassName());
        } else {
            codeGetBuilder.addStatement(
                    StringUtils.format("$T s=getInstance().get%s(%s, defVal)",
                            params.getValueName(), annotationParams.getKeyStatement()), mStringTypeName, params.getFullClassName());
            if (methodInfo.getParamsNum() == 1) {
                codeGetBuilder.addStatement(StringUtils.format("return $T.%s(s)",
                        methodInfo.getName()), methodInfo.getClassName());
            } else if (methodInfo.getParamsNum() == 2) {
                codeGetBuilder.addStatement(StringUtils.format("return $T.%s(%s, s)",
                        methodInfo.getName(), annotationParams.getKeyStatement()), methodInfo.getClassName());
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
        if (annotationParams.isHasPrefix()) {
            methodBuilder.addParameter(annotationParams.getPrefixTypeName(), annotationParams.getPrefixParamName(), Modifier.FINAL);
        }
        if (annotationParams.isHasSuffix()) {
            methodBuilder.addParameter(annotationParams.getSuffixTypeName(), annotationParams.getSuffixParamName(), Modifier.FINAL);
        }
        classBuilder.addMethod(methodBuilder.build());

        // set方法
        CodeBlock.Builder codeSetBuilder = CodeBlock.builder();
        methodInfo = mEncodeMethodMap.get(annotationParams.getCodeId());
        if (mStringTypeName != params.getTypeName() || methodInfo == null) {
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, value)", params.getValueName(), annotationParams.getKeyStatement()), params.getFullClassName());
        } else {
            if (methodInfo.getParamsNum() == 1) {
                codeSetBuilder.addStatement(StringUtils.format("$T encode=$T.%s(value)",
                        methodInfo.getName()), mStringTypeName, methodInfo.getClassName());
            } else if (methodInfo.getParamsNum() == 2) {
                codeSetBuilder.addStatement(StringUtils.format("$T encode=$T.%s(%s, value)",
                        methodInfo.getName(), annotationParams.getKeyStatement()), mStringTypeName, methodInfo.getClassName());
            } else {
                notSupportMethod(methodInfo, 1, 2);
            }
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, encode)", params.getValueName(), annotationParams.getKeyStatement()), params.getFullClassName());
        }

        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getSetMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(codeSetBuilder.build());
        if (annotationParams.isHasPrefix()) {
            methodBuilder.addParameter(annotationParams.getPrefixTypeName(), annotationParams.getPrefixParamName(), Modifier.FINAL);
        }
        if (annotationParams.isHasSuffix()) {
            methodBuilder.addParameter(annotationParams.getSuffixTypeName(), annotationParams.getSuffixParamName(), Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());

        if (annotationParams.isGenerateRemove()) {
            addRemoveMethod(classBuilder, params);
        }

        if (annotationParams.isGenerateContains()) {
            addContainsMethod(classBuilder, params);
        }
    }

    private void buildGenericMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        AnnotationParams annotationParams = params.getAnnotationParams();

        MethodSpec.Builder methodBuilder;
        String valueName = "String";
        MethodInfo serializerMethod = null;
        ClassName serializerClass = null;
        String serializerName = null;
        Class<? extends Annotation> annotationType = params.getAnnotation().annotationType();
        for (Class<? extends Annotation> cls : AnnotationList.getSerializerList(annotationType)) {
            serializerMethod = mRuleMethodMap.get(cls);
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
        for (Class<? extends Annotation> cls : AnnotationList.getDeserializerList(annotationType)) {
            deserializerMethod = mRuleMethodMap.get(cls);
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
        CodeBlock.Builder codeGetBuilder = CodeBlock.builder();
        codeGetBuilder.addStatement(StringUtils.format(
                "$T json = getInstance().get%s(%s, $S)",
                valueName, annotationParams.getKeyStatement()), mStringTypeName, params.getFullClassName(), annotationParams.getDefString());
        if (params.getValTypeName() == null) {
            if (deserializerMethod.getParamsNum() == 2) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(json,$T.class)",
                        deserializerName), params.getTypeName(), deserializerClass, params.getKeyTypeName());
            } else if (deserializerMethod.getParamsNum() == 3) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(%s,json,$T.class)",
                        deserializerName, annotationParams.getKeyStatement()), params.getTypeName(), deserializerClass,
                        params.getFullClassName(), params.getKeyTypeName());
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
                        deserializerName, annotationParams.getKeyStatement()), params.getTypeName(), deserializerClass,
                        params.getFullClassName(), params.getKeyTypeName(), params.getValTypeName());
            } else {
                notSupportMethod(serializerMethod, 3, 4);
            }
        }
        if (annotationParams.isDefNull()) {
            codeGetBuilder.addStatement("return value");
        } else if (annotationParams.isDefEmpty()) {
            codeGetBuilder.beginControlFlow("if(value==null)");
            if (params.getAnnotation() instanceof PrefsKey.List) {
                TypeName defType = TypeName.get(ArrayList.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            } else if (params.getAnnotation() instanceof PrefsKey.Set) {
                TypeName defType = TypeName.get(HashSet.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            } else if (params.getAnnotation() instanceof PrefsKey.Queue) {
                TypeName defType = TypeName.get(LinkedList.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            } else if (params.getAnnotation() instanceof PrefsKey.Deque) {
                TypeName defType = TypeName.get(LinkedList.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
                // } else if (params.getAnnotation() instanceof PrefsKey.SparseArray) {
                //     codeGetBuilder.addStatement("return new $T()", params.getTypeName());
            } else if (params.getAnnotation() instanceof PrefsKey.Map) {
                TypeName defType = TypeName.get(HashMap.class);
                codeGetBuilder.addStatement("return new $T<>()", defType);
            }
            codeGetBuilder.nextControlFlow("else")
                    .addStatement("return value")
                    .endControlFlow();
        } else {
            codeGetBuilder.beginControlFlow("if(value==null)");
            MethodInfo methodInfo = mDefaultMethodMap.get(annotationParams.getDefValFromId());
            if (methodInfo == null) {
                canNotFindDefVal(annotationParams.getDefValFromId());
            } else if (methodInfo.isMethod()) {
                if (methodInfo.getParamsNum() == 0) {
                    codeGetBuilder.addStatement(StringUtils.format(
                            "return $T.%s()", methodInfo.getName()), methodInfo.getClassName());
                } else if (methodInfo.getParamsNum() == 1) {
                    codeGetBuilder.addStatement(StringUtils.format(
                            "return $T.%s(%s)", methodInfo.getName(), annotationParams.getKeyStatement()),
                            methodInfo.getClassName(), params.getFullClassName());
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
        if (annotationParams.isHasPrefix()) {
            methodBuilder.addParameter(annotationParams.getPrefixTypeName(), annotationParams.getPrefixParamName(), Modifier.FINAL);
        }
        if (annotationParams.isHasSuffix()) {
            methodBuilder.addParameter(annotationParams.getSuffixTypeName(), annotationParams.getSuffixParamName(), Modifier.FINAL);
        }
        classBuilder.addMethod(methodBuilder.build());

        // set方法
        CodeBlock.Builder codeSetBuilder = CodeBlock.builder();
        if (serializerMethod.getParamsNum() == 1) {
            codeSetBuilder.addStatement(StringUtils.format("getInstance().set%s(%s, $T.%s(value))",
                    valueName, annotationParams.getKeyStatement(), serializerName), params.getFullClassName(), serializerClass);
        } else if (serializerMethod.getParamsNum() == 2) {
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, $T.%s(%s,value))",
                    valueName, annotationParams.getKeyStatement(), serializerName, annotationParams.getKeyStatement()),
                    params.getFullClassName(), serializerClass, params.getFullClassName());
        } else {
            notSupportMethod(serializerMethod, 1, 2);
        }

        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getSetMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(codeSetBuilder.build());
        if (annotationParams.isHasPrefix()) {
            methodBuilder.addParameter(annotationParams.getPrefixTypeName(), annotationParams.getPrefixParamName(), Modifier.FINAL);
        }
        if (annotationParams.isHasSuffix()) {
            methodBuilder.addParameter(annotationParams.getSuffixTypeName(), annotationParams.getSuffixParamName(), Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());

        if (annotationParams.isGenerateRemove()) {
            addRemoveMethod(classBuilder, params);
        }

        if (annotationParams.isGenerateContains()) {
            addContainsMethod(classBuilder, params);
        }
    }

    private void addRemoveMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        AnnotationParams annotationParams = params.getAnnotationParams();

        CodeBlock.Builder codeRemoveBuilder = CodeBlock.builder();
        codeRemoveBuilder.addStatement(StringUtils.format(
                "return getInstance().remove(%s)", annotationParams.getKeyStatement()), params.getFullClassName());

        MethodSpec.Builder methodBuilder;
        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getRemoveMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addCode(codeRemoveBuilder.build());
        if (annotationParams.isHasPrefix()) {
            methodBuilder.addParameter(annotationParams.getPrefixTypeName(), annotationParams.getPrefixParamName(), Modifier.FINAL);
        }
        if (annotationParams.isHasSuffix()) {
            methodBuilder.addParameter(annotationParams.getSuffixTypeName(), annotationParams.getSuffixParamName(), Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());
    }

    private void addContainsMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        AnnotationParams annotationParams = params.getAnnotationParams();

        CodeBlock.Builder codeRemoveBuilder = CodeBlock.builder();
        codeRemoveBuilder.addStatement(StringUtils.format(
                "return getInstance().contains(%s)", annotationParams.getKeyStatement()), params.getFullClassName());

        MethodSpec.Builder methodBuilder;
        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getContainsMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addCode(codeRemoveBuilder.build());
        if (annotationParams.isHasPrefix()) {
            methodBuilder.addParameter(annotationParams.getPrefixTypeName(), annotationParams.getPrefixParamName(), Modifier.FINAL);
        }
        if (annotationParams.isHasSuffix()) {
            methodBuilder.addParameter(annotationParams.getSuffixTypeName(), annotationParams.getSuffixParamName(), Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
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
