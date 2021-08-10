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

        KeyParams.Builder builder = KeyParams.newBuilder()
                .currentClass(ClassName.bestGuess(qualifiedName))
                .filedName(variableElement.getSimpleName().toString())
                .valueName("String")
                .typeName(mStringTypeName);

        for (Class<? extends Annotation> cls : AnnotationList.getPrefsVal()) {
            Annotation annotation = variableElement.getAnnotation(cls);
            if (annotation instanceof PrefsVal.Int) {
                PrefsVal.Int annotationObj = (PrefsVal.Int) annotation;
                builder.typeName(TypeName.INT)
                        .defValue(annotationObj.defVal())
                        .valueName("Int");
            } else if (annotation instanceof PrefsVal.Float) {
                PrefsVal.Float annotationObj = (PrefsVal.Float) annotation;
                builder.typeName(TypeName.FLOAT)
                        .defValue(annotationObj.defVal() + "f")
                        .valueName("Float");
            } else if (annotation instanceof PrefsVal.Bool) {
                PrefsVal.Bool annotationObj = (PrefsVal.Bool) annotation;
                builder.typeName(TypeName.BOOLEAN)
                        .defValue(annotationObj.defVal())
                        .valueName("Bool");
            } else if (annotation instanceof PrefsVal.Byte) {
                PrefsVal.Byte annotationObj = (PrefsVal.Byte) annotation;
                builder.typeName(TypeName.BYTE)
                        .defValue(StringUtils.format("(byte)%d", annotationObj.defVal()))
                        .valueName("Byte");
            } else if (annotation instanceof PrefsVal.Double) {
                PrefsVal.Double annotationObj = (PrefsVal.Double) annotation;
                builder.typeName(TypeName.DOUBLE)
                        .defValue(annotationObj)
                        .valueName("Double");
            } else if (annotation instanceof PrefsVal.Char) {
                PrefsVal.Char annotationObj = (PrefsVal.Char) annotation;
                char defChar = annotationObj.defVal();
                builder.typeName(TypeName.CHAR)
                        .defValue(Character.isDefined(defChar) ?
                                StringUtils.format("'%c'", defChar) :
                                StringUtils.format("(char)%d", (int) defChar))
                        .valueName("Char");
            } else if (annotation instanceof PrefsVal.Long) {
                PrefsVal.Long annotationObj = (PrefsVal.Long) annotation;
                builder.typeName(TypeName.LONG)
                        .defValue(annotationObj)
                        .valueName("Long");
            } else if (annotation instanceof PrefsVal.Short) {
                PrefsVal.Short annotationObj = (PrefsVal.Short) annotation;
                builder.typeName(TypeName.SHORT)
                        .defValue(StringUtils.format("(short)%d", annotationObj.defVal()))
                        .valueName("Short");
            } else if (annotation instanceof PrefsVal.String) {
                PrefsVal.String annotationObj = (PrefsVal.String) annotation;
                builder.typeName(mStringTypeName)
                        .defValue(annotationObj.defVal())
                        .valueName("String");
            } else if (annotation instanceof PrefsVal.Object) {
                TypeName typeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(typeName)
                        .keyTypeName(typeName)
                        .genericVal(true);
            } else if (annotation instanceof PrefsVal.List) {
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(List.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
            } else if (annotation instanceof PrefsVal.Set) {
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Set.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
            } else if (annotation instanceof PrefsVal.Queue) {
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Queue.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
            } else if (annotation instanceof PrefsVal.Deque) {
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Deque.class, subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
            } else if (annotation instanceof PrefsVal.SparseArray) {
                TypeName subTypeName = MethodUtils.getType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(ClassName.bestGuess("android.util.SparseArray"), subTypeName))
                        .keyTypeName(subTypeName)
                        .genericVal(true);
            } else if (annotation instanceof PrefsVal.Map) {
                TypeName keyTypeName = MethodUtils.getKeyType(element, cls, mElementUtils);
                TypeName valTypeName = MethodUtils.getValType(element, cls, mElementUtils);
                builder.typeName(getParameterizedTypedName(Map.class, keyTypeName, valTypeName))
                        .keyTypeName(keyTypeName)
                        .valTypeName(valTypeName)
                        .genericVal(true);
            }
            if (annotation != null) {
                builder.annotation(annotation);
                break;
            }
        }

        KeyParams params = builder
                .build();

        PrefParams prefParams = variableElement.getAnnotation(PrefParams.class);
        PrefParamsData paramsData;
        if (prefParams == null) {
            paramsData = PrefParamsData.newBuilder().build(params);
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
                    .build(params);
        }

        params.setParamsData(paramsData);

        if (mStringTypeName.equals(params.getTypeName())) {
            if (paramsData.isDefNull()) {
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
        PrefParamsData paramsData = params.getParamsData();

        MethodSpec.Builder methodBuilder;
        MethodInfo methodInfo;

        // get方法
        CodeBlock.Builder codeGetBuilder = CodeBlock.builder();
        methodInfo = mUserMethodParams.get(paramsData.getDefValFromId());
        if (paramsData.getDefValFromId() > 0 && methodInfo == null) {
            canNotFindDefVal(paramsData.getDefValFromId());
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
                        "$T defVal=$T.%s(%s)", methodInfo.getName(), paramsData.getKeyStatement()),
                        params.getTypeName(), methodInfo.getClassName(), params.getCurrentClass());
            } else {
                notSupportMethod(methodInfo, 0, 1);
            }
        } else {
            codeGetBuilder.addStatement(StringUtils.format(
                    "$T defVal=$T.%s", methodInfo.getName()), params.getTypeName(), methodInfo.getClassName());
        }
        methodInfo = mDecodeMethodMap.get(paramsData.getCodeId());
        if (methodInfo == null) {
            codeGetBuilder.addStatement(
                    StringUtils.format("return getInstance().get%s(%s, defVal)",
                            params.getValueName(), paramsData.getKeyStatement()), params.getCurrentClass());
        } else {
            codeGetBuilder.addStatement(
                    StringUtils.format("$T s=getInstance().get%s(%s, defVal)",
                            params.getValueName(), paramsData.getKeyStatement()), mStringTypeName, params.getCurrentClass());
            if (methodInfo.getParamsNum() == 1) {
                codeGetBuilder.addStatement(StringUtils.format("return $T.%s(s)",
                        methodInfo.getName()), methodInfo.getClassName());
            } else if (methodInfo.getParamsNum() == 2) {
                codeGetBuilder.addStatement(StringUtils.format("return $T.%s(%s, s)",
                        methodInfo.getName(), paramsData.getKeyStatement()), methodInfo.getClassName());
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
        if (paramsData.isHasPrefix()) {
            methodBuilder.addParameter(paramsData.getPrefixTypeName(), paramsData.getPrefixParamName(), Modifier.FINAL);
        }
        if (paramsData.isHasSuffix()) {
            methodBuilder.addParameter(paramsData.getSuffixTypeName(), paramsData.getSuffixParamName(), Modifier.FINAL);
        }
        classBuilder.addMethod(methodBuilder.build());

        // set方法
        CodeBlock.Builder codeSetBuilder = CodeBlock.builder();
        methodInfo = mEncodeMethodMap.get(paramsData.getCodeId());
        if (methodInfo == null) {
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, value)", params.getValueName(), paramsData.getKeyStatement()), params.getCurrentClass());
        } else {
            if (methodInfo.getParamsNum() == 1) {
                codeSetBuilder.addStatement(StringUtils.format("$T encode=$T.%s(value)",
                        methodInfo.getName()), mStringTypeName, methodInfo.getClassName());
            } else if (methodInfo.getParamsNum() == 2) {
                codeSetBuilder.addStatement(StringUtils.format("$T encode=$T.%s(%s, value)",
                        methodInfo.getName(), paramsData.getKeyStatement()), mStringTypeName, methodInfo.getClassName());
            } else {
                notSupportMethod(methodInfo, 1, 2);
            }
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, encode)", params.getValueName(), paramsData.getKeyStatement()), params.getCurrentClass());
        }

        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getSetMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(codeSetBuilder.build());
        if (paramsData.isHasPrefix()) {
            methodBuilder.addParameter(paramsData.getPrefixTypeName(), paramsData.getPrefixParamName(), Modifier.FINAL);
        }
        if (paramsData.isHasSuffix()) {
            methodBuilder.addParameter(paramsData.getSuffixTypeName(), paramsData.getSuffixParamName(), Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());

        if (paramsData.isGenerateRemove()) {
            addRemoveMethod(classBuilder, params);
        }

        if (paramsData.isGenerateContains()) {
            addContainsMethod(classBuilder, params);
        }
    }

    private void buildGenericMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        PrefParamsData paramsData = params.getParamsData();

        MethodSpec.Builder methodBuilder;
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
        CodeBlock.Builder codeGetBuilder = CodeBlock.builder();
        codeGetBuilder.addStatement(StringUtils.format(
                "$T json = getInstance().get%s(%s, $S)",
                valueName, paramsData.getKeyStatement()), mStringTypeName, params.getCurrentClass(), paramsData.getDefString());
        if (params.getValTypeName() == null) {
            if (deserializerMethod.getParamsNum() == 2) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(json,$T.class)",
                        deserializerName), params.getTypeName(), deserializerClass, params.getKeyTypeName());
            } else if (deserializerMethod.getParamsNum() == 3) {
                codeGetBuilder.addStatement(StringUtils.format(
                        "$T value = $T.%s(%s,json,$T.class)",
                        deserializerName, paramsData.getKeyStatement()), params.getTypeName(), deserializerClass,
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
                        deserializerName, paramsData.getKeyStatement()), params.getTypeName(), deserializerClass,
                        params.getCurrentClass(), params.getKeyTypeName(), params.getValTypeName());
            } else {
                notSupportMethod(serializerMethod, 3, 4);
            }
        }
        if (paramsData.isDefNull()) {
            codeGetBuilder.addStatement("return value");
        } else if (paramsData.isDefEmpty()) {
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
            MethodInfo methodInfo = mUserMethodParams.get(paramsData.getDefValFromId());
            if (methodInfo == null) {
                canNotFindDefVal(paramsData.getDefValFromId());
            } else if (methodInfo.isMethod()) {
                if (methodInfo.getParamsNum() == 0) {
                    codeGetBuilder.addStatement(StringUtils.format(
                            "return $T.%s()", methodInfo.getName()), methodInfo.getClassName());
                } else if (methodInfo.getParamsNum() == 1) {
                    codeGetBuilder.addStatement(StringUtils.format(
                            "return $T.%s(%s)", methodInfo.getName(), paramsData.getKeyStatement()),
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
        if (paramsData.isHasPrefix()) {
            methodBuilder.addParameter(paramsData.getPrefixTypeName(), paramsData.getPrefixParamName(), Modifier.FINAL);
        }
        if (paramsData.isHasSuffix()) {
            methodBuilder.addParameter(paramsData.getSuffixTypeName(), paramsData.getSuffixParamName(), Modifier.FINAL);
        }
        classBuilder.addMethod(methodBuilder.build());

        // set方法
        CodeBlock.Builder codeSetBuilder = CodeBlock.builder();
        if (serializerMethod.getParamsNum() == 1) {
            codeSetBuilder.addStatement(StringUtils.format("getInstance().set%s(%s, $T.%s(value))",
                    valueName, paramsData.getKeyStatement(), serializerName), params.getCurrentClass(), serializerClass);
        } else if (serializerMethod.getParamsNum() == 2) {
            codeSetBuilder.addStatement(StringUtils.format(
                    "getInstance().set%s(%s, $T.%s(%s,value))",
                    valueName, paramsData.getKeyStatement(), serializerName, paramsData.getKeyStatement()),
                    params.getCurrentClass(), serializerClass, params.getCurrentClass());
        } else {
            notSupportMethod(serializerMethod, 1, 2);
        }

        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getSetMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(codeSetBuilder.build());
        if (paramsData.isHasPrefix()) {
            methodBuilder.addParameter(paramsData.getPrefixTypeName(), paramsData.getPrefixParamName(), Modifier.FINAL);
        }
        if (paramsData.isHasSuffix()) {
            methodBuilder.addParameter(paramsData.getSuffixTypeName(), paramsData.getSuffixParamName(), Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());

        if (paramsData.isGenerateRemove()) {
            addRemoveMethod(classBuilder, params);
        }

        if (paramsData.isGenerateContains()) {
            addContainsMethod(classBuilder, params);
        }
    }

    private void addRemoveMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        PrefParamsData paramsData = params.getParamsData();

        CodeBlock.Builder codeRemoveBuilder = CodeBlock.builder();
        codeRemoveBuilder.addStatement(StringUtils.format(
                "return getInstance().remove(%s)", paramsData.getKeyStatement()), params.getCurrentClass());

        MethodSpec.Builder methodBuilder;
        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getRemoveMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addCode(codeRemoveBuilder.build());
        if (paramsData.isHasPrefix()) {
            methodBuilder.addParameter(paramsData.getPrefixTypeName(), paramsData.getPrefixParamName(), Modifier.FINAL);
        }
        if (paramsData.isHasSuffix()) {
            methodBuilder.addParameter(paramsData.getSuffixTypeName(), paramsData.getSuffixParamName(), Modifier.FINAL);
        }
        methodBuilder.addParameter(params.getTypeName(), "value", Modifier.FINAL);
        classBuilder.addMethod(methodBuilder.build());
    }

    private void addContainsMethod(TypeSpec.Builder classBuilder, KeyParams params) {
        PrefParamsData paramsData = params.getParamsData();

        CodeBlock.Builder codeRemoveBuilder = CodeBlock.builder();
        codeRemoveBuilder.addStatement(StringUtils.format(
                "return getInstance().contains(%s)", paramsData.getKeyStatement()), params.getCurrentClass());

        MethodSpec.Builder methodBuilder;
        methodBuilder = MethodSpec.methodBuilder(MethodUtils.getContainsMethodName(params.getFiledName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN)
                .addCode(codeRemoveBuilder.build());
        if (paramsData.isHasPrefix()) {
            methodBuilder.addParameter(paramsData.getPrefixTypeName(), paramsData.getPrefixParamName(), Modifier.FINAL);
        }
        if (paramsData.isHasSuffix()) {
            methodBuilder.addParameter(paramsData.getSuffixTypeName(), paramsData.getSuffixParamName(), Modifier.FINAL);
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
